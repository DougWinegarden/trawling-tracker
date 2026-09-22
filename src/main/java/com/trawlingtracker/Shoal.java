package com.trawlingtracker;

import static com.trawlingtracker.RareFish.GIANT_BLUE_KRILL;
import static com.trawlingtracker.RareFish.GOLDEN_HADDOCK;
import static com.trawlingtracker.RareFish.HUGE_HALIBUT;
import static com.trawlingtracker.RareFish.ORANGEFIN;
import static com.trawlingtracker.RareFish.PURPLEFIN;
import static com.trawlingtracker.RareFish.SWIFT_MARLIN;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.util.QuantityFormatter;

/**
 * Shoal types and the rare fish each rolls for on every successful catch, by the player or a
 * crewmate, with the effective rates from the OSRS wiki.
 * <p>
 * A regular shoal has a 10% chance to turn into a mixed shoal for two stops. Mixed shoals roll
 * for several rare fish at once and yield the fish of several regular shoals.
 */
@Getter
enum Shoal
{
	GIANT_KRILL("Giant krill", ObjectID.SAILING_SHOAL_CLICKBOX_GIANT_KRILL, new String[]{"krill"},
		new Roll(GIANT_BLUE_KRILL, 1, 2_000)),
	HADDOCK("Haddock", ObjectID.SAILING_SHOAL_CLICKBOX_HADDOCK, new String[]{"haddock"},
		new Roll(GOLDEN_HADDOCK, 1, 2_000)),
	YELLOWFIN("Yellowfin", ObjectID.SAILING_SHOAL_CLICKBOX_YELLOWFIN, new String[]{"yellowfin", "orangefin"},
		new Roll(ORANGEFIN, 1, 4_000)),
	HALIBUT("Halibut", ObjectID.SAILING_SHOAL_CLICKBOX_HALIBUT, new String[]{"halibut"},
		new Roll(HUGE_HALIBUT, 1, 4_000)),
	BLUEFIN("Bluefin", ObjectID.SAILING_SHOAL_CLICKBOX_BLUEFIN, new String[]{"bluefin", "purplefin"},
		new Roll(PURPLEFIN, 1, 6_000)),
	MARLIN("Marlin", ObjectID.SAILING_SHOAL_CLICKBOX_MARLIN, new String[]{"marlin"},
		new Roll(SWIFT_MARLIN, 1, 6_000)),

	SHIMMERING("Shimmering", ObjectID.SAILING_SHOAL_CLICKBOX_SHIMMERING,
		new Shoal[]{GIANT_KRILL, HADDOCK, YELLOWFIN, HALIBUT},
		new Roll(GIANT_BLUE_KRILL, 9, 20_000),
		new Roll(GOLDEN_HADDOCK, 1, 2_500),
		new Roll(ORANGEFIN, 1, 10_000),
		new Roll(HUGE_HALIBUT, 1, 20_000)),
	GLISTENING("Glistening", ObjectID.SAILING_SHOAL_CLICKBOX_GLISTENING,
		new Shoal[]{YELLOWFIN, HALIBUT, BLUEFIN, MARLIN},
		new Roll(ORANGEFIN, 9, 40_000),
		new Roll(HUGE_HALIBUT, 1, 5_000),
		new Roll(PURPLEFIN, 1, 20_000),
		new Roll(SWIFT_MARLIN, 1, 40_000)),
	VIBRANT("Vibrant", ObjectID.SAILING_SHOAL_CLICKBOX_VIBRANT,
		new Shoal[]{BLUEFIN, MARLIN},
		new Roll(PURPLEFIN, 1, 6_000),
		new Roll(SWIFT_MARLIN, 1, 6_000));

	private final String displayName;

	/**
	 * The clickbox object the shoal shows up as in the scene.
	 */
	private final int objectId;

	/**
	 * Regular shoals: words in a catch message that name their fish, regular or rare.
	 */
	private final List<String> keywords;

	/**
	 * Mixed shoals: the regular shoals whose fish they yield.
	 */
	private final List<Shoal> mixes;

	private final List<Roll> rolls;

	Shoal(String displayName, int objectId, String[] keywords, Roll roll)
	{
		this(displayName, objectId, Arrays.asList(keywords), Collections.emptyList(), Collections.singletonList(roll));
	}

	Shoal(String displayName, int objectId, Shoal[] mixes, Roll... rolls)
	{
		this(displayName, objectId, Collections.emptyList(), Arrays.asList(mixes), Arrays.asList(rolls));
	}

	Shoal(String displayName, int objectId, List<String> keywords, List<Shoal> mixes, List<Roll> rolls)
	{
		this.displayName = displayName;
		this.objectId = objectId;
		this.keywords = keywords;
		this.mixes = mixes;
		this.rolls = rolls;
	}

	boolean isMixed()
	{
		return !mixes.isEmpty();
	}

	/**
	 * Whether the fish of the given regular shoal can be caught here.
	 */
	boolean yields(Shoal regular)
	{
		return this == regular || mixes.contains(regular);
	}

	Roll rollFor(RareFish fish)
	{
		for (Roll roll : rolls)
		{
			if (roll.getFish() == fish)
			{
				return roll;
			}
		}
		return null;
	}

	/**
	 * The one regular shoal that rolls for this fish.
	 */
	static Shoal regularShoalOf(RareFish fish)
	{
		for (Shoal shoal : values())
		{
			if (!shoal.isMixed() && shoal.rollFor(fish) != null)
			{
				return shoal;
			}
		}
		throw new IllegalArgumentException("No regular shoal rolls for " + fish);
	}

	static Shoal fromObjectId(int objectId)
	{
		for (Shoal shoal : values())
		{
			if (shoal.objectId == objectId)
			{
				return shoal;
			}
		}
		return null;
	}

	/**
	 * @param caught what a catch message says was caught, e.g. "3 marlin" or "a swift marlin"
	 * @return the regular shoal those fish belong to, or null if they aren't shoal fish
	 */
	static Shoal regularFromCatch(String caught)
	{
		String lower = caught.toLowerCase();
		for (Shoal shoal : values())
		{
			for (String keyword : shoal.keywords)
			{
				if (lower.contains(keyword))
				{
					return shoal;
				}
			}
		}
		return null;
	}

	@Value
	static class Roll
	{
		RareFish fish;
		int numerator;
		int denominator;

		double chance()
		{
			return (double) numerator / denominator;
		}

		/**
		 * e.g. "1/6,000" or "9/20,000".
		 */
		String rateText()
		{
			return numerator + "/" + QuantityFormatter.formatNumber(denominator);
		}
	}
}
