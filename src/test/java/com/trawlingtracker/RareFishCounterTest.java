package com.trawlingtracker;

import static org.junit.Assert.assertEquals;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class RareFishCounterTest
{
	private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

	@Test
	public void regularShoalCatchCountsForItsOneFish()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.recordCatch(Shoal.MARLIN, T0);
		counter.recordCatch(Shoal.MARLIN, T0);
		assertEquals(2, counter.getTotalCatches(RareFish.SWIFT_MARLIN));
		assertEquals(0, counter.getTotalCatches(RareFish.PURPLEFIN));
		assertEquals(Collections.singletonList(RareFish.SWIFT_MARLIN), counter.getCurrentFish());
	}

	@Test
	public void mixedShoalCatchCountsForEveryFishItRollsFor()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.recordCatch(Shoal.GLISTENING, T0);
		for (RareFish fish : Arrays.asList(RareFish.ORANGEFIN, RareFish.HUGE_HALIBUT, RareFish.PURPLEFIN, RareFish.SWIFT_MARLIN))
		{
			assertEquals(1, counter.getTotalCatches(fish));
		}
		assertEquals(0, counter.getTotalCatches(RareFish.GOLDEN_HADDOCK));
	}

	@Test
	public void homeFishStaysOnTopAtAMixedShoalAndExtrasGoAwayAfter()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.recordCatch(Shoal.MARLIN, T0);
		counter.recordCatch(Shoal.VIBRANT, T0);
		assertEquals(Arrays.asList(RareFish.SWIFT_MARLIN, RareFish.PURPLEFIN), counter.getCurrentFish());

		counter.recordCatch(Shoal.GLISTENING, T0);
		assertEquals(Arrays.asList(RareFish.SWIFT_MARLIN, RareFish.ORANGEFIN, RareFish.HUGE_HALIBUT, RareFish.PURPLEFIN),
			counter.getCurrentFish());

		counter.recordCatch(Shoal.MARLIN, T0);
		assertEquals(Collections.singletonList(RareFish.SWIFT_MARLIN), counter.getCurrentFish());
	}

	@Test
	public void mixedShoalWithNoRegularShoalYetKeepsTableOrder()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.recordCatch(Shoal.VIBRANT, T0);
		assertEquals(Arrays.asList(RareFish.PURPLEFIN, RareFish.SWIFT_MARLIN), counter.getCurrentFish());
	}

	@Test
	public void oddsUseEachShoalsRate()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.setCatches(RareFish.SWIFT_MARLIN, Shoal.MARLIN, 3_000);
		counter.setCatches(RareFish.SWIFT_MARLIN, Shoal.GLISTENING, 20_000);
		counter.setCatches(RareFish.SWIFT_MARLIN, Shoal.VIBRANT, 3_000);
		assertEquals(26_000, counter.getTotalCatches(RareFish.SWIFT_MARLIN));
		// 3,000/6,000 + 20,000/40,000 + 3,000/6,000
		assertEquals(1.5, counter.getOdds(RareFish.SWIFT_MARLIN).timesDropRate(), 1e-12);
	}

	@Test
	public void resetClearsOneFishAtEveryShoal()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.recordCatch(Shoal.VIBRANT, T0);
		counter.recordCatch(Shoal.MARLIN, T0);
		counter.reset(RareFish.SWIFT_MARLIN);
		assertEquals(0, counter.getTotalCatches(RareFish.SWIFT_MARLIN));
		assertEquals(1, counter.getTotalCatches(RareFish.PURPLEFIN));
	}

	@Test
	public void tracksLastCatch()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.recordCatch(Shoal.HALIBUT, T0.plusSeconds(60));
		assertEquals(Shoal.HALIBUT, counter.getCurrent());
		assertEquals(T0.plusSeconds(60), counter.getLastCatch());
	}

	@Test
	public void setTotalPutsCarriedOverCatchesAtTheRegularShoal()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.setTotalCatches(RareFish.SWIFT_MARLIN, 4_500);
		assertEquals(4_500, counter.getCatches(RareFish.SWIFT_MARLIN, Shoal.MARLIN));
		assertEquals(0.75, counter.getOdds(RareFish.SWIFT_MARLIN).timesDropRate(), 1e-12);
	}

	@Test
	public void setTotalKeepsMixedShoalCatchesThatFit()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.setCatches(RareFish.SWIFT_MARLIN, Shoal.MARLIN, 1_000);
		counter.setCatches(RareFish.SWIFT_MARLIN, Shoal.GLISTENING, 400);
		counter.setTotalCatches(RareFish.SWIFT_MARLIN, 5_000);
		assertEquals(4_600, counter.getCatches(RareFish.SWIFT_MARLIN, Shoal.MARLIN));
		assertEquals(400, counter.getCatches(RareFish.SWIFT_MARLIN, Shoal.GLISTENING));
		assertEquals(5_000, counter.getTotalCatches(RareFish.SWIFT_MARLIN));
	}

	@Test
	public void setTotalBelowMixedShoalCatchesPutsAllAtTheRegularShoal()
	{
		RareFishCounter counter = new RareFishCounter();
		counter.setCatches(RareFish.SWIFT_MARLIN, Shoal.VIBRANT, 800);
		counter.setTotalCatches(RareFish.SWIFT_MARLIN, 300);
		assertEquals(300, counter.getCatches(RareFish.SWIFT_MARLIN, Shoal.MARLIN));
		assertEquals(0, counter.getCatches(RareFish.SWIFT_MARLIN, Shoal.VIBRANT));
		assertEquals(300, counter.getTotalCatches(RareFish.SWIFT_MARLIN));
	}
}
