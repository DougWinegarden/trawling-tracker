package com.trawlingtracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;

/**
 * A shoal catch announced in chat: "You catch 3 marlin!" for the player, "Jolly Jim catches a
 * swift marlin!" for a crewmate.
 */
@Value
class CatchMessage
{
	private static final String PLAYER = "You catch";
	private static final Pattern PATTERN = Pattern.compile("(" + PLAYER + "|.+ catches) (.+)!");

	boolean byPlayer;

	/**
	 * What was caught, e.g. "3 marlin" or "a swift marlin".
	 */
	String caught;

	/**
	 * @return the catch, or null if the message isn't a catch message
	 */
	static CatchMessage parse(String message)
	{
		Matcher matcher = PATTERN.matcher(message);
		if (!matcher.matches())
		{
			return null;
		}
		return new CatchMessage(matcher.group(1).equals(PLAYER), matcher.group(2));
	}
}
