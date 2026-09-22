package com.trawlingtracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TrawlingTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TrawlingTrackerPlugin.class);
		RuneLite.main(args);
	}
}
