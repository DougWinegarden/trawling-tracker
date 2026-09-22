package com.trawlingtracker;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Counts the player's own successful net catches toward angler's paint. Crewmates' catches and
 * rod fishing never roll for it.
 */
class PaintCounter
{
	static final int REGULAR_ONE_IN = 18_000;
	static final int MIXED_ONE_IN = 16_200;

	/**
	 * All-time catches at regular and at mixed shoals. Saved to config by the plugin.
	 */
	@Getter
	@Setter
	private long regularCatches;

	@Getter
	@Setter
	private long mixedCatches;

	@Getter
	private Instant lastCatch;

	static int oneIn(Shoal shoal)
	{
		return shoal.isMixed() ? MIXED_ONE_IN : REGULAR_ONE_IN;
	}

	void recordCatch(Shoal shoal, Instant now)
	{
		if (shoal.isMixed())
		{
			mixedCatches++;
		}
		else
		{
			regularCatches++;
		}
		lastCatch = now;
	}

	long getTotalCatches()
	{
		return regularCatches + mixedCatches;
	}

	/**
	 * Sets the total, e.g. to carry over a count kept by hand. Mixed shoal catches rolled at another
	 * rate, so they're kept if they fit in the new total, and the rest count as regular.
	 */
	void setTotalCatches(long total)
	{
		if (mixedCatches > total)
		{
			mixedCatches = 0;
		}
		regularCatches = total - mixedCatches;
	}

	DropOdds getOdds()
	{
		return new DropOdds()
			.add(regularCatches, 1.0 / REGULAR_ONE_IN)
			.add(mixedCatches, 1.0 / MIXED_ONE_IN);
	}

	void reset()
	{
		regularCatches = 0;
		mixedCatches = 0;
		lastCatch = null;
	}
}
