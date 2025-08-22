package com.gielinorgains;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gielinorgains")
public interface GielinorGainsConfig extends Config
{
	@ConfigItem(
		keyName = "refreshInterval",
		name = "Refresh Interval",
		description = "How often to refresh data from Gielinor Gains API (seconds)"
	)
	default int refreshInterval()
	{
		return 90;
	}

	@ConfigItem(
		keyName = "itemLimit",
		name = "Items to Display",
		description = "Maximum number of items to display in the table"
	)
	default int itemLimit()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "minScore",
		name = "Minimum Score",
		description = "Only show items with score >= this value (0-5)"
	)
	default double minScore()
	{
		return 0.0;
	}

	@ConfigItem(
		keyName = "showIcons",
		name = "Show Item Icons",
		description = "Display item icons in the table"
	)
	default boolean showIcons()
	{
		return true;
	}
}
