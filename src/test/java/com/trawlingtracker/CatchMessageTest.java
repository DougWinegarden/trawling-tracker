package com.trawlingtracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CatchMessageTest
{
	@Test
	public void playerCatches()
	{
		CatchMessage single = CatchMessage.parse("You catch a marlin!");
		assertTrue(single.isByPlayer());
		assertEquals("a marlin", single.getCaught());

		CatchMessage several = CatchMessage.parse("You catch 3 marlin!");
		assertTrue(several.isByPlayer());
		assertEquals("3 marlin", several.getCaught());
	}

	@Test
	public void crewmateCatches()
	{
		CatchMessage crew = CatchMessage.parse("Jolly Jim catches a swift marlin!");
		assertFalse(crew.isByPlayer());
		assertEquals("a swift marlin", crew.getCaught());
	}

	@Test
	public void ignoresOtherMessages()
	{
		// Full nets, the crew XP message, the paint drop, and this plugin's own chat lines
		assertNull(CatchMessage.parse("Your net has no more space for any swift marlin."));
		assertNull(CatchMessage.parse("Your nets are too full to hold any more marlin."));
		assertNull(CatchMessage.parse("You gain some experience by watching your crew work."));
		assertNull(CatchMessage.parse("You've received some paint!"));
		assertNull(CatchMessage.parse("Catch at vibrant shoal counted toward purplefin, swift marlin, angler's paint"));
		assertNull(CatchMessage.parse("Sailing XP drop: 15 (Soup attempt)"));
	}
}
