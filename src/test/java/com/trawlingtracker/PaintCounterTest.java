package com.trawlingtracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.time.Instant;
import org.junit.Test;

public class PaintCounterTest
{
	private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

	@Test
	public void rateDependsOnShoalType()
	{
		assertEquals(18_000, PaintCounter.oneIn(Shoal.MARLIN));
		assertEquals(16_200, PaintCounter.oneIn(Shoal.VIBRANT));
	}

	@Test
	public void countsRegularAndMixedCatchesSeparately()
	{
		PaintCounter counter = new PaintCounter();
		counter.recordCatch(Shoal.MARLIN, T0);
		counter.recordCatch(Shoal.MARLIN, T0);
		counter.recordCatch(Shoal.VIBRANT, T0.plusSeconds(5));
		assertEquals(2, counter.getRegularCatches());
		assertEquals(1, counter.getMixedCatches());
		assertEquals(3, counter.getTotalCatches());
		assertEquals(T0.plusSeconds(5), counter.getLastCatch());
	}

	@Test
	public void oddsUseEachRate()
	{
		PaintCounter counter = new PaintCounter();
		counter.setRegularCatches(9_000);
		counter.setMixedCatches(8_100);
		// 9,000/18,000 + 8,100/16,200
		assertEquals(1.0, counter.getOdds().timesDropRate(), 1e-12);
	}

	@Test
	public void reset()
	{
		PaintCounter counter = new PaintCounter();
		counter.recordCatch(Shoal.MARLIN, T0);
		counter.recordCatch(Shoal.GLISTENING, T0);
		counter.reset();
		assertEquals(0, counter.getTotalCatches());
		assertNull(counter.getLastCatch());
	}

	@Test
	public void setTotalKeepsMixedCatchesThatFit()
	{
		PaintCounter counter = new PaintCounter();
		counter.setMixedCatches(200);
		counter.setTotalCatches(1_000);
		assertEquals(800, counter.getRegularCatches());
		assertEquals(200, counter.getMixedCatches());

		counter.setTotalCatches(100);
		assertEquals(100, counter.getRegularCatches());
		assertEquals(0, counter.getMixedCatches());
	}
}
