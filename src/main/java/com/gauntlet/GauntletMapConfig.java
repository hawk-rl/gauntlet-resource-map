package com.gauntlet;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gauntletMap")
public interface GauntletMapConfig extends Config
{
	@ConfigItem(
			keyName = "displayLargerIcons",
			name = "Larger icons",
			description = "Enable for larger icons"
	)
	default boolean displayLargerIcons()
	{
		return false;
	}

	@ConfigItem(
		keyName = "displayFishingSpots",
		name = "Display fishing spots",
		description = "Enable for fishing spots to appear on the map"
	)
	default boolean displayFishingSpots()
	{
		return true;
	}

	@ConfigItem(
			keyName = "displayGrymRoot",
			name = "Display grym root",
			description = "Enable for grym root nodes to appear on the map"
	)
	default boolean displayGrymRoot()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayLinumTirinum",
			name = "Display linum tirinum",
			description = "Enable for linum tirinum nodes to appear on the map"
	)
	default boolean displayLinumTirinum()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayOreDeposit",
			name = "Display ore deposits",
			description = "Enable for ore deposits to appear on the map"
	)
	default boolean displayOreDeposit()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayPhrenRoot",
			name = "Display phren root",
			description = "Enable for phren root nodes to appear on the map"
	)
	default boolean displayPhrenRoot()
	{
		return false;
	}
}
