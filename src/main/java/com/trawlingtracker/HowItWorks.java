package com.trawlingtracker;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Text for the "How it works" section of the settings page. RuneLite's settings page can only
 * show text as an editable config value, so the plugin writes these in on startup and puts them
 * back whenever they're edited.
 */
final class HowItWorks
{
	static final String SOUP = "Soup rolls once per Sailing XP drop from your own trawling net: every 3 ticks,"
		+ " or every 2 if you click the net every tick. Crewmates' drops never roll it.\n"
		+ "\n"
		+ "RuneLite only sees your Sailing XP total, so a drop counts as an attempt when it's between"
		+ " Min and Max XP per attempt (15 and 30 by default):\n"
		+ "- 15: you, cotton net at the right depth\n"
		+ "- 25-26: you and a crewmate on the same tick (counts once)\n"
		+ "- about 10-11: a crewmate on their own (not counted)\n"
		+ "- 7-8: you, net at the wrong depth (not counted, though it does roll)\n"
		+ "- over 30: other Sailing activities (not counted)\n"
		+ "\n"
		+ "A drop that comes with \"You gain some experience by watching your crew work\" is never counted.";

	static final String RARE_FISH = "Every successful catch, by you or a crewmate, rolls for the shoal's rare"
		+ " fish, whether or not you get Fishing XP for it. Each catch posts a chat message (\"You catch 3"
		+ " marlin!\", \"Jolly Jim catches 2 marlin!\") and counts once.\n"
		+ "\n"
		+ "Regular shoals roll for one rare fish:\n"
		+ "- Giant krill: giant blue krill 1/2,000\n"
		+ "- Haddock: golden haddock 1/2,000\n"
		+ "- Yellowfin: orangefin 1/4,000\n"
		+ "- Halibut: huge halibut 1/4,000\n"
		+ "- Bluefin: purplefin 1/6,000\n"
		+ "- Marlin: swift marlin 1/6,000\n"
		+ "\n"
		+ "Catches made while your net is full don't post a catch message, so they aren't counted.";

	static final String MIXED_SHOALS = "A regular shoal has a 10% chance to turn into a mixed shoal for two"
		+ " stops before changing back. A mixed shoal rolls for several rare fish on every catch, each at"
		+ " its own rate:\n"
		+ "- Shimmering (from krill or haddock): giant blue krill 9/20,000, golden haddock 1/2,500,"
		+ " orangefin 1/10,000, huge halibut 1/20,000\n"
		+ "- Glistening (from yellowfin or halibut): orangefin 9/40,000, huge halibut 1/5,000,"
		+ " purplefin 1/20,000, swift marlin 1/40,000\n"
		+ "- Vibrant (from bluefin or marlin): purplefin 1/6,000, swift marlin 1/6,000\n"
		+ "\n"
		+ "At a mixed shoal the overlay adds a section for each of its rare fish, with your usual one on"
		+ " top, and drops the extras once you're back at a regular shoal. The plugin goes by the shoal"
		+ " nearest your boat.";

	static final String PAINT = "Angler's paint rolls on every successful catch you make with your own trawling"
		+ " net: 1/18,000 at regular shoals and 1/16,200 at mixed shoals. Crewmates' catches never roll it,"
		+ " so only your own \"You catch ...\" messages count.\n"
		+ "\n"
		+ "Rod fishing at a shoal doesn't roll it either, but its catch messages look the same, so rod"
		+ " catches would be counted.";

	static final String STATS = "Attempts / Catches / Your catches: how many times you've rolled for it, at"
		+ " any shoal.\n"
		+ "\n"
		+ "Drop rate: how many drops you'd expect by now. Each roll adds its chance, so 3,000 catches at"
		+ " 1/6,000 is 0.500x. At 1.000x you've rolled as many times as the rate's denominator; past"
		+ " that you're dry.\n"
		+ "\n"
		+ "Chance by now: the chance you'd have had at least one by now. For n rolls at chance p it's"
		+ " 1 - (1 - p)^n; rolls at different rates multiply their (1 - p)^n together first. It's 63.2%"
		+ " at 1x, 86.5% at 2x and 95.0% at 3x.\n"
		+ "\n"
		+ "Attempts/hr: Soup attempts per hour this trip. A trip ends after 5 minutes without one.\n"
		+ "\n"
		+ "The title reads NOT trawling when no Sailing XP has come in for 6 seconds.";

	static final String SAVING = "Counts are saved per account and survive logging out. Right-click the"
		+ " overlay to reset Soup, angler's paint, or one of the rare fish it's showing. Hiding something"
		+ " in these settings doesn't stop it being counted.\n"
		+ "\n"
		+ "Your counts, below, shows every count on the account you're logged into, as of when you opened"
		+ " these settings. Change one to set it, e.g. to carry over a total you kept before installing"
		+ " this plugin. A number you type in counts at the regular shoal's rate; catches the plugin has"
		+ " already counted at mixed shoals keep their own rates as long as they fit in the new total.";

	/**
	 * Config key to text.
	 */
	static final Map<String, String> BY_KEY = new LinkedHashMap<>();

	static
	{
		BY_KEY.put("infoSoup", SOUP);
		BY_KEY.put("infoRareFish", RARE_FISH);
		BY_KEY.put("infoMixedShoals", MIXED_SHOALS);
		BY_KEY.put("infoPaint", PAINT);
		BY_KEY.put("infoStats", STATS);
		BY_KEY.put("infoSaving", SAVING);
	}

	private HowItWorks()
	{
	}
}
