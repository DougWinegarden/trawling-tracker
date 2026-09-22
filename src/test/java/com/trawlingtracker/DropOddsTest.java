package com.trawlingtracker;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DropOddsTest
{
	@Test
	public void singleRate()
	{
		DropOdds odds = new DropOdds().add(180_000, 1.0 / 360_000);
		assertEquals(0.5, odds.timesDropRate(), 1e-12);
		// 1 - (1 - 1/360,000)^180,000
		assertEquals(0.39347, odds.chanceByNow(), 1e-5);
	}

	@Test
	public void noRolls()
	{
		DropOdds odds = new DropOdds();
		assertEquals(0.0, odds.timesDropRate(), 0);
		assertEquals(0.0, odds.chanceByNow(), 0);
	}

	@Test
	public void atTheDropRateItsAboutOneMinusOneOverE()
	{
		assertEquals(0.63212, new DropOdds().add(360_000, 1.0 / 360_000).chanceByNow(), 1e-5);
		assertEquals(0.63215, new DropOdds().add(6_000, 1.0 / 6_000).chanceByNow(), 1e-5);
		assertEquals(0.86469, new DropOdds().add(12_000, 1.0 / 6_000).chanceByNow(), 1e-5);
	}

	@Test
	public void mixedRatesCombine()
	{
		// 3,000 marlin shoal catches at 1/6,000 and 20,000 glistening catches at 1/40,000
		DropOdds odds = new DropOdds().add(3_000, 1.0 / 6_000).add(20_000, 1.0 / 40_000);
		assertEquals(1.0, odds.timesDropRate(), 1e-12);
		double expected = 1 - Math.pow(1 - 1.0 / 6_000, 3_000) * Math.pow(1 - 1.0 / 40_000, 20_000);
		assertEquals(expected, odds.chanceByNow(), 1e-12);
	}
}
