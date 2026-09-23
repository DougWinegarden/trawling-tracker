package com.trawlingtracker;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

/**
 * The Sailing XP drop sizes that count as one of the player's own trawling attempts.
 * <p>
 * Sizes are matched exactly rather than as a range, because sizes in between belong to crewmates.
 * With a cotton net a roll gives the player 15 XP at the right depth and 7.5 at the wrong one,
 * while a crewmate's roll gives about 10.5.
 * <p>
 * The game keeps experience to one decimal place and shows it rounded down, so an award of 7.5
 * arrives as 7 or 8 depending on the decimal already banked, while a whole 15 always arrives as 15.
 * Awards landing on the same tick are added together before that rounding.
 */
class CountedXpDrops
{
	/**
	 * A cotton net with no Keg of Horizon's Lure: 15 at the right depth, 7.5 at the wrong depth
	 * (landing as 7 or 8), and the player plus a crewmate on the same tick, which is 25.5 at the
	 * right depth and 18 at the wrong one.
	 */
	static final String DEFAULT = "7, 8, 15, 18, 25, 26";

	/**
	 * The same net with a Keg of Horizon's Lure aboard. Its 2.5% bonus is rounded down to one
	 * decimal, so the player's 15 becomes 15.3 and arrives as 15 or 16, the wrong depth's 7.5
	 * becomes 7.6 and arrives as 7 or 8, and a crewmate's 10.5 becomes 10.7, still arriving as
	 * 10 or 11. Together they make 15.3 + 10.7 = 26.0, which always arrives as 26, or 18.3 at the
	 * wrong depth, arriving as 18 or 19.
	 */
	static final String WITH_HORIZONS_LURE = "7, 8, 15, 16, 18, 19, 26";

	@Getter
	private final Set<Integer> sizes;

	private CountedXpDrops(Set<Integer> sizes)
	{
		this.sizes = Collections.unmodifiableSet(sizes);
	}

	/**
	 * Reads a comma separated list of sizes, ignoring anything that isn't a number.
	 *
	 * @return the sizes listed, or the default ones if the setting holds no usable number
	 */
	static CountedXpDrops parse(String setting)
	{
		Set<Integer> sizes = new LinkedHashSet<>();
		if (setting != null)
		{
			for (String part : setting.split("\\D+"))
			{
				if (part.isEmpty())
				{
					continue;
				}

				try
				{
					sizes.add(Integer.parseInt(part));
				}
				catch (NumberFormatException ignored)
				{
					// Too long to be an XP drop
				}
			}
		}
		return sizes.isEmpty() ? parse(DEFAULT) : new CountedXpDrops(sizes);
	}

	boolean counts(int drop)
	{
		return sizes.contains(drop);
	}
}
