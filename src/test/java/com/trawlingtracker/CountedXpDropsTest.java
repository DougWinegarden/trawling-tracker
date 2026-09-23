package com.trawlingtracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.junit.Test;

public class CountedXpDropsTest
{
	@Test
	public void defaultsCountYoursAndNotTheCrewsWithACottonNet()
	{
		CountedXpDrops drops = CountedXpDrops.parse(CountedXpDrops.DEFAULT);
		// You at the right depth, you at the wrong depth, and either plus a crewmate
		assertTrue(drops.counts(15));
		assertTrue(drops.counts(7));
		assertTrue(drops.counts(8));
		assertTrue(drops.counts(25));
		assertTrue(drops.counts(26));
		assertTrue(drops.counts(18));
		// A crewmate alone, and two crewmates
		assertFalse(drops.counts(10));
		assertFalse(drops.counts(11));
		assertFalse(drops.counts(21));
		// Other Sailing activities
		assertFalse(drops.counts(31));
		assertFalse(drops.counts(1_200));
	}

	@Test
	public void horizonsLureShiftsEverySizeButNotTheCrewsOwn()
	{
		CountedXpDrops drops = CountedXpDrops.parse(CountedXpDrops.WITH_HORIZONS_LURE);
		// 15.3 arrives as 15 or 16, the wrong depth's 7.6 as 7 or 8
		assertTrue(drops.counts(15));
		assertTrue(drops.counts(16));
		assertTrue(drops.counts(7));
		assertTrue(drops.counts(8));
		// You and a crewmate: 15.3 + 10.7 is a whole 26, and 18.3 at the wrong depth
		assertTrue(drops.counts(26));
		assertTrue(drops.counts(18));
		assertTrue(drops.counts(19));
		assertFalse(drops.counts(25));
		assertFalse(drops.counts(27));
		// A crewmate's 10.7 alone, and two crewmates' 21.4 together
		assertFalse(drops.counts(10));
		assertFalse(drops.counts(11));
		assertFalse(drops.counts(21));
		assertFalse(drops.counts(22));
	}

	@Test
	public void readsAListInAnyReasonableShape()
	{
		assertEquals(new LinkedHashSet<>(Arrays.asList(7, 8, 15)), CountedXpDrops.parse(" 7,8 , 15 ").getSizes());
		assertEquals(new LinkedHashSet<>(Arrays.asList(7, 8, 15)), CountedXpDrops.parse("7 8 15").getSizes());
		assertEquals(new LinkedHashSet<>(Arrays.asList(15, 16)), CountedXpDrops.parse("15-16").getSizes());
		assertEquals(new LinkedHashSet<>(Arrays.asList(15)), CountedXpDrops.parse("15, 15").getSizes());
	}

	@Test
	public void emptyOrUnreadableGoesBackToTheDefaults()
	{
		CountedXpDrops fallback = CountedXpDrops.parse(CountedXpDrops.DEFAULT);
		assertEquals(fallback.getSizes(), CountedXpDrops.parse("").getSizes());
		assertEquals(fallback.getSizes(), CountedXpDrops.parse("   ").getSizes());
		assertEquals(fallback.getSizes(), CountedXpDrops.parse("none").getSizes());
		assertEquals(fallback.getSizes(), CountedXpDrops.parse(null).getSizes());
	}

	@Test
	public void ignoresNumbersTooLargeToBeAnXpDrop()
	{
		assertEquals(new LinkedHashSet<>(Arrays.asList(15)), CountedXpDrops.parse("15, 99999999999999").getSizes());
	}
}
