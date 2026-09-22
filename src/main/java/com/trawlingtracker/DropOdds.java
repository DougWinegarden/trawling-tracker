package com.trawlingtracker;

/**
 * Adds up rolls, possibly made at different chances, into the overlay's two stats.
 */
final class DropOdds
{
	private double expectedDrops;
	private double logChanceAllMissed;

	DropOdds add(long rolls, double chance)
	{
		expectedDrops += rolls * chance;
		logChanceAllMissed += rolls * Math.log1p(-chance);
		return this;
	}

	/**
	 * Drops you'd expect by now. 1.0 means as many rolls as the drop rate's denominator.
	 */
	double timesDropRate()
	{
		return expectedDrops;
	}

	/**
	 * Chance of at least one drop by now, from 0 to 1: 1 - (1 - p1)^n1 * (1 - p2)^n2 * ...,
	 * summed as logs so it stays accurate when the chances are tiny.
	 */
	double chanceByNow()
	{
		return -Math.expm1(logChanceAllMissed);
	}
}
