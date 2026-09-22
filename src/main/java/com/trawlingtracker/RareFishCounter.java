package com.trawlingtracker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Counts successful shoal catches, by the player or a crewmate, toward each rare fish.
 */
class RareFishCounter
{
	/**
	 * All-time catches per rare fish, split by the shoal type they were rolled at, since each
	 * shoal type rolls at its own rate. Saved to config by the plugin.
	 */
	private final Map<RareFish, Map<Shoal, Long>> catches = new EnumMap<>(RareFish.class);

	/**
	 * The shoal caught from most recently.
	 */
	@Getter
	private Shoal current;

	/**
	 * The rare fish of the last regular shoal fished, listed first while at a mixed shoal.
	 */
	private RareFish homeFish;

	@Getter
	private Instant lastCatch;

	/**
	 * Counts one catch toward every rare fish the shoal rolls for.
	 */
	void recordCatch(Shoal shoal, Instant now)
	{
		for (Shoal.Roll roll : shoal.getRolls())
		{
			catches.computeIfAbsent(roll.getFish(), fish -> new EnumMap<>(Shoal.class))
				.merge(shoal, 1L, Long::sum);
		}

		if (!shoal.isMixed())
		{
			homeFish = shoal.getRolls().get(0).getFish();
		}
		current = shoal;
		lastCatch = now;
	}

	long getCatches(RareFish fish, Shoal shoal)
	{
		return catches.getOrDefault(fish, Collections.emptyMap()).getOrDefault(shoal, 0L);
	}

	void setCatches(RareFish fish, Shoal shoal, long count)
	{
		catches.computeIfAbsent(fish, f -> new EnumMap<>(Shoal.class)).put(shoal, count);
	}

	/**
	 * Sets a fish's total, e.g. to carry over a count kept by hand. Catches at mixed shoals rolled
	 * at other rates, so they're kept if they fit in the new total, and the rest are put down to
	 * the fish's regular shoal.
	 */
	void setTotalCatches(RareFish fish, long total)
	{
		Map<Shoal, Long> byShoal = catches.computeIfAbsent(fish, f -> new EnumMap<>(Shoal.class));
		long mixed = 0;
		for (Map.Entry<Shoal, Long> entry : byShoal.entrySet())
		{
			if (entry.getKey().isMixed())
			{
				mixed += entry.getValue();
			}
		}

		if (mixed > total)
		{
			byShoal.replaceAll((shoal, count) -> 0L);
			mixed = 0;
		}
		byShoal.put(Shoal.regularShoalOf(fish), total - mixed);
	}

	/**
	 * Catches that rolled for this fish, at any shoal.
	 */
	long getTotalCatches(RareFish fish)
	{
		long total = 0;
		for (long count : catches.getOrDefault(fish, Collections.emptyMap()).values())
		{
			total += count;
		}
		return total;
	}

	DropOdds getOdds(RareFish fish)
	{
		DropOdds odds = new DropOdds();
		for (Map.Entry<Shoal, Long> entry : catches.getOrDefault(fish, Collections.emptyMap()).entrySet())
		{
			odds.add(entry.getValue(), entry.getKey().rollFor(fish).chance());
		}
		return odds;
	}

	void reset(RareFish fish)
	{
		catches.remove(fish);
	}

	/**
	 * The rare fish the current shoal rolls for, with the last regular shoal's fish first.
	 */
	List<RareFish> getCurrentFish()
	{
		if (current == null)
		{
			return Collections.emptyList();
		}

		List<RareFish> fish = new ArrayList<>();
		for (Shoal.Roll roll : current.getRolls())
		{
			fish.add(roll.getFish());
		}
		if (homeFish != null && fish.remove(homeFish))
		{
			fish.add(0, homeFish);
		}
		return fish;
	}
}
