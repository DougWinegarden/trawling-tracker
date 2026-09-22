package com.trawlingtracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.runelite.api.gameval.ObjectID;
import org.junit.Test;

public class ShoalTest
{
	@Test
	public void catchMessagesNameTheirRegularShoal()
	{
		assertEquals(Shoal.GIANT_KRILL, Shoal.regularFromCatch("4 giant krill"));
		assertEquals(Shoal.GIANT_KRILL, Shoal.regularFromCatch("a giant blue krill"));
		assertEquals(Shoal.HADDOCK, Shoal.regularFromCatch("2 haddock"));
		assertEquals(Shoal.YELLOWFIN, Shoal.regularFromCatch("a yellowfin"));
		assertEquals(Shoal.YELLOWFIN, Shoal.regularFromCatch("an orangefin"));
		assertEquals(Shoal.HALIBUT, Shoal.regularFromCatch("3 halibut"));
		assertEquals(Shoal.BLUEFIN, Shoal.regularFromCatch("a purplefin"));
		assertEquals(Shoal.MARLIN, Shoal.regularFromCatch("a swift marlin"));
		assertNull(Shoal.regularFromCatch("a shark"));
	}

	@Test
	public void objectIds()
	{
		assertEquals(Shoal.MARLIN, Shoal.fromObjectId(ObjectID.SAILING_SHOAL_CLICKBOX_MARLIN));
		assertEquals(Shoal.VIBRANT, Shoal.fromObjectId(ObjectID.SAILING_SHOAL_CLICKBOX_VIBRANT));
		assertNull(Shoal.fromObjectId(ObjectID.SAILING_COTTON_TRAWLING_NET));
	}

	@Test
	public void mixedShoalsYieldTheirRegularShoalsFish()
	{
		assertTrue(Shoal.VIBRANT.yields(Shoal.MARLIN));
		assertTrue(Shoal.VIBRANT.yields(Shoal.BLUEFIN));
		assertFalse(Shoal.VIBRANT.yields(Shoal.HALIBUT));
		assertTrue(Shoal.GLISTENING.yields(Shoal.MARLIN));
		assertTrue(Shoal.MARLIN.yields(Shoal.MARLIN));
		assertFalse(Shoal.MARLIN.yields(Shoal.BLUEFIN));
	}

	@Test
	public void mixedShoalRatesAddUpToTheirRareTable()
	{
		// The wiki gives each mixed shoal's overall rare table chance, then how it's split
		assertEquals(1.0 / 1_000, totalChance(Shoal.SHIMMERING), 1e-15);
		assertEquals(1.0 / 2_000, totalChance(Shoal.GLISTENING), 1e-15);
		assertEquals(1.0 / 3_000, totalChance(Shoal.VIBRANT), 1e-15);
	}

	@Test
	public void rateText()
	{
		assertEquals("1/6,000", Shoal.MARLIN.rollFor(RareFish.SWIFT_MARLIN).rateText());
		assertEquals("9/20,000", Shoal.SHIMMERING.rollFor(RareFish.GIANT_BLUE_KRILL).rateText());
		assertNull(Shoal.MARLIN.rollFor(RareFish.PURPLEFIN));
	}

	private static double totalChance(Shoal shoal)
	{
		double total = 0;
		for (Shoal.Roll roll : shoal.getRolls())
		{
			total += roll.chance();
		}
		return total;
	}

	@Test
	public void everyRareFishHasOneRegularShoal()
	{
		assertEquals(Shoal.MARLIN, Shoal.regularShoalOf(RareFish.SWIFT_MARLIN));
		assertEquals(Shoal.YELLOWFIN, Shoal.regularShoalOf(RareFish.ORANGEFIN));
		for (RareFish fish : RareFish.values())
		{
			assertFalse(Shoal.regularShoalOf(fish).isMixed());
		}
	}
}
