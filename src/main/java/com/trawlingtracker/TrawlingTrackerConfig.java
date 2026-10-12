package com.trawlingtracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(TrawlingTrackerConfig.GROUP)
public interface TrawlingTrackerConfig extends Config
{
	/**
	 * Kept from when this plugin was Soup Pet Tracker, so saved counts and settings carry over.
	 */
	String GROUP = "soup-pet-tracker";

	String COUNT_DESCRIPTION = "As of when you opened these settings. Change it while logged in to set"
		+ " the count on that account.";

	@ConfigItem(
		keyName = "compactView",
		name = "Compact view",
		description = "Trim the overlay to two lines each: the name and count on one, the chance by now"
			+ " under it. Attempts/hr, the drop rate multiple and the rate each roll is at are left out.",
		position = 0
	)
	default boolean compactView()
	{
		return false;
	}

	@ConfigSection(
		name = "How it works",
		description = "What gets counted, and how the stats are worked out",
		position = 1,
		closedByDefault = true
	)
	String infoSection = "info";

	@ConfigSection(
		name = "Soup (pet)",
		description = "Soup rolls once per Sailing XP drop from your own trawling net",
		position = 2
	)
	String soupSection = "soup";

	@ConfigSection(
		name = "Rare fish",
		description = "Rare fish roll once per successful catch, by you or a crewmate",
		position = 3
	)
	String rareFishSection = "rareFish";

	@ConfigSection(
		name = "Angler's paint",
		description = "Angler's paint rolls once per successful catch by you, never by a crewmate",
		position = 4
	)
	String paintSection = "paint";

	@ConfigSection(
		name = "Your counts",
		description = "Your counts on the account you're logged into. Change one to set it, e.g. to carry"
			+ " over a total you kept before installing this plugin.",
		position = 5,
		closedByDefault = true
	)
	String countsSection = "counts";

	@ConfigItem(
		keyName = "infoSoup",
		name = "Soup",
		description = "What counts as a Soup attempt",
		position = 0,
		section = infoSection
	)
	default String infoSoup()
	{
		return HowItWorks.SOUP;
	}

	@ConfigItem(
		keyName = "infoRareFish",
		name = "Rare fish",
		description = "What counts as a rare fish catch",
		position = 1,
		section = infoSection
	)
	default String infoRareFish()
	{
		return HowItWorks.RARE_FISH;
	}

	@ConfigItem(
		keyName = "infoMixedShoals",
		name = "Mixed shoals",
		description = "How shimmering, glistening and vibrant shoals are handled",
		position = 2,
		section = infoSection
	)
	default String infoMixedShoals()
	{
		return HowItWorks.MIXED_SHOALS;
	}

	@ConfigItem(
		keyName = "infoPaint",
		name = "Angler's paint",
		description = "What counts as an angler's paint catch",
		position = 3,
		section = infoSection
	)
	default String infoPaint()
	{
		return HowItWorks.PAINT;
	}

	@ConfigItem(
		keyName = "infoStats",
		name = "The stats",
		description = "How drop rate and chance by now are worked out",
		position = 4,
		section = infoSection
	)
	default String infoStats()
	{
		return HowItWorks.STATS;
	}

	@ConfigItem(
		keyName = "infoSaving",
		name = "Saving and resetting",
		description = "Where counts are kept and how to reset them",
		position = 5,
		section = infoSection
	)
	default String infoSaving()
	{
		return HowItWorks.SAVING;
	}

	@ConfigItem(
		keyName = "showSoup",
		name = "Show Soup",
		description = "Show your Soup attempts in the overlay. They're counted either way.",
		position = 0,
		section = soupSection
	)
	default boolean showSoup()
	{
		return true;
	}

	@ConfigItem(
		keyName = "horizonsLure",
		name = "Horizon's Lure aboard",
		description = "Fills Counted XP drops below with the sizes for a Keg of Horizon's Lure, whose"
			+ " 2.5% Sailing XP bonus shifts every size, or back with the sizes for no keg. Switching"
			+ " this replaces whatever the list holds; you can edit it afterwards.",
		position = 1,
		section = soupSection
	)
	default boolean horizonsLure()
	{
		return false;
	}

	@ConfigItem(
		keyName = "countedXpDrops",
		name = "Counted XP drops",
		description = "Sailing XP drop sizes, comma separated, that count as one of your own trawling"
			+ " attempts. The defaults suit a cotton net with no Keg of Horizon's Lure: 15 at the right"
			+ " depth, 7-8 at the wrong depth, and you plus a crewmate on the same tick, which is 25-26"
			+ " at the right depth and 18 at the wrong one. Crew-only drops, about 10-11, are left out."
			+ " Empty goes back to the defaults.",
		position = 2,
		section = soupSection
	)
	default String countedXpDrops()
	{
		return CountedXpDrops.DEFAULT;
	}

	@Range(min = 2)
	@ConfigItem(
		keyName = "dropRate",
		name = "Pet drop rate (1 in)",
		description = "Soup's drop rate per deep sea trawling attempt",
		position = 3,
		section = soupSection
	)
	default int dropRate()
	{
		return 360_000;
	}

	@ConfigItem(
		keyName = "showRareFish",
		name = "Show rare fish",
		description = "Show your catches toward the rare fish of the shoal you're fishing, e.g. swift"
			+ " marlin at a marlin shoal. They're counted either way.",
		position = 0,
		section = rareFishSection
	)
	default boolean showRareFish()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGiantBlueKrill",
		name = "Giant blue krill",
		description = "Show giant blue krill in the overlay",
		position = 1,
		section = rareFishSection
	)
	default boolean showGiantBlueKrill()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showGoldenHaddock",
		name = "Golden haddock",
		description = "Show golden haddock in the overlay",
		position = 2,
		section = rareFishSection
	)
	default boolean showGoldenHaddock()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOrangefin",
		name = "Orangefin",
		description = "Show orangefin in the overlay",
		position = 3,
		section = rareFishSection
	)
	default boolean showOrangefin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showHugeHalibut",
		name = "Huge halibut",
		description = "Show huge halibut in the overlay",
		position = 4,
		section = rareFishSection
	)
	default boolean showHugeHalibut()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPurplefin",
		name = "Purplefin",
		description = "Show purplefin in the overlay",
		position = 5,
		section = rareFishSection
	)
	default boolean showPurplefin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSwiftMarlin",
		name = "Swift marlin",
		description = "Show swift marlin in the overlay",
		position = 6,
		section = rareFishSection
	)
	default boolean showSwiftMarlin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showAnglersPaint",
		name = "Show angler's paint",
		description = "Show your catches toward angler's paint in the overlay. They're counted either way.",
		position = 0,
		section = paintSection
	)
	default boolean showAnglersPaint()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countSoup",
		name = "Soup attempts",
		description = COUNT_DESCRIPTION,
		position = 0,
		section = countsSection
	)
	default int countSoup()
	{
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countPaint",
		name = "Angler's paint catches",
		description = COUNT_DESCRIPTION,
		position = 1,
		section = countsSection
	)
	default int countPaint()
	{
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countGiantBlueKrill",
		name = "Giant blue krill catches",
		description = COUNT_DESCRIPTION,
		position = 2,
		section = countsSection
	)
	default int countGiantBlueKrill()
	{
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countGoldenHaddock",
		name = "Golden haddock catches",
		description = COUNT_DESCRIPTION,
		position = 3,
		section = countsSection
	)
	default int countGoldenHaddock()
	{
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countOrangefin",
		name = "Orangefin catches",
		description = COUNT_DESCRIPTION,
		position = 4,
		section = countsSection
	)
	default int countOrangefin()
	{
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countHugeHalibut",
		name = "Huge halibut catches",
		description = COUNT_DESCRIPTION,
		position = 5,
		section = countsSection
	)
	default int countHugeHalibut()
	{
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countPurplefin",
		name = "Purplefin catches",
		description = COUNT_DESCRIPTION,
		position = 6,
		section = countsSection
	)
	default int countPurplefin()
	{
		return 0;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "countSwiftMarlin",
		name = "Swift marlin catches",
		description = COUNT_DESCRIPTION,
		position = 7,
		section = countsSection
	)
	default int countSwiftMarlin()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "showXpDropsInChat",
		name = "Show counting in chat",
		description = "Print every Sailing XP drop and whether it counted as a Soup attempt, and every"
			+ " catch with the shoal, rare fish and paint it counted toward",
		position = 5
	)
	default boolean showXpDropsInChat()
	{
		return false;
	}
}
