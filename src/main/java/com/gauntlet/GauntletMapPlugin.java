package com.gauntlet;

import com.gauntlet.resources.*;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Map"
)
public class GauntletMapPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	@Getter
	private GauntletMapConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GauntletMapOverlay mapOverlay;

	private static final int CRYSTAL_GAUNTLET_REGION_ID = 7512;
	private static final int CORRUPTED_GAUNTLET_REGION_ID = 7768;
	public static final int GAUNTLET_MAP_WIDGET_ID = 638;
	private static final String ENTER_MESSAGE = "You enter the Gauntlet.";
	private static final String LEAVE_MESSAGE = "You leave the Gauntlet.";
	private static final String DEATH_MESSAGE = "Oh dear, you are dead!";

	@Getter
	private boolean isMapOpen;
	@Getter
	private Widget gauntletMapWidget;
	@Getter
	private final Set<ResourceNode> foundNodes = new HashSet<>();
	@Getter
	private WorldPoint gridOffset;

	private static final Set<Integer> RESOURCE_NODE_IDS = ImmutableSet.of(
			ObjectID.CRYSTAL_DEPOSIT,
			ObjectID.CORRUPT_DEPOSIT,
			ObjectID.PHREN_ROOTS,
			ObjectID.PHREN_ROOTS_36066,
			ObjectID.LINUM_TIRINUM,
			ObjectID.LINUM_TIRINUM_36072,
			ObjectID.GRYM_ROOT,
			ObjectID.GRYM_ROOT_36070,
			ObjectID.FISHING_SPOT_36068,
			ObjectID.FISHING_SPOT_35971
	);

	private static final Set<Integer> DEPLETED_NODE_IDS = ImmutableSet.of(
			ObjectID.CRYSTAL_DEPOSIT_DEPLETED,
			ObjectID.CORRUPT_DEPOSIT_DEPLETED,
			ObjectID.PHREN_ROOTS_DEPLETED,
			ObjectID.PHREN_ROOTS_DEPLETED_36067,
			ObjectID.LINUM_TIRINUM_DEPLETED,
			ObjectID.LINUM_TIRINUM_DEPLETED_36073,
			ObjectID.GRYM_ROOT_DEPLETED,
			ObjectID.GRYM_ROOT_DEPLETED_36071,
			ObjectID.FISHING_SPOT_DEPLETED,
			ObjectID.FISHING_SPOT_DEPLETED_36069
	);

	@Provides
	GauntletMapConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletMapConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		foundNodes.clear();
		overlayManager.add(mapOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		foundNodes.clear();
		overlayManager.remove(mapOverlay);
		isMapOpen = false;
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType().equals(ChatMessageType.GAMEMESSAGE)) {
			if (chatMessage.getMessage().equals(ENTER_MESSAGE)
					|| chatMessage.getMessage().equals(LEAVE_MESSAGE)
					|| chatMessage.getMessage().equals(DEATH_MESSAGE)) {
				foundNodes.clear();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("gauntletMap"))
		{
			if (configChanged.getKey().equals("displayLargerIcons"))
			{
				boolean newValue = Boolean.parseBoolean(configChanged.getNewValue());
				// Update resource node images
				for (ResourceNode node : foundNodes)
				{
					node.setImage(newValue);
				}
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (!isInGauntlet())
			return;

		GameObject object = gameObjectSpawned.getGameObject();

		// Check for new resources
		if (RESOURCE_NODE_IDS.contains(object.getId()))
		{
			ResourceNode node = gameObjectToResource(object);

			if (node != null && !hasAlreadyFoundNode(node))
				foundNodes.add(node);
		}

		// Check for depleted resources
		if (DEPLETED_NODE_IDS.contains(object.getId()))
		{
			// If this depleted node is a found node, remove it
			WorldPoint objectLocation = object.getWorldLocation();
			ResourceNode depletedNode = null;
			for (ResourceNode node : foundNodes)
			{
				if (node.getX() == objectLocation.getX() && node.getY() == objectLocation.getY())
				{
					depletedNode = node;
					break; // Only one node should be matching
				}
			}

			if (depletedNode != null)
				foundNodes.remove(depletedNode);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (npcSpawned.getNpc().getId() == NpcID.CRYSTALLINE_HUNLLEF
				|| npcSpawned.getNpc().getId() == NpcID.CORRUPTED_HUNLLEF)
		{
			// Hunllef always spawns in same spot
			WorldPoint hunllefSpawn = npcSpawned.getNpc().getWorldLocation();
			WorldPoint playerSpawn = client.getLocalPlayer().getWorldLocation();

			int xDiff = Math.abs(playerSpawn.getX() - hunllefSpawn.getX());
			int yDiff = Math.abs(playerSpawn.getY() - hunllefSpawn.getY());

			// North diff from boss spawn = 5, 20
			if (xDiff == 5 && yDiff == 20)
				gridOffset = new WorldPoint(playerSpawn.getX() - 59, playerSpawn.getY() - 74, 1);

			// West diff = 11, 4
			if (xDiff == 11 && yDiff == 4)
				gridOffset = new WorldPoint(playerSpawn.getX() - 43, playerSpawn.getY() - 59, 1);

			// South diff = 5, 12
			if (xDiff == 5 && yDiff == 12)
				gridOffset = new WorldPoint(playerSpawn.getX() - 59, playerSpawn.getY() - 42, 1);

			// East diff = 21, 4
			if (xDiff == 21 && yDiff == 4)
				gridOffset = new WorldPoint(playerSpawn.getX() - 75, playerSpawn.getY() - 58, 1);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (!isInGauntlet())
			return;

		isMapOpen = widgetLoaded.getGroupId() == GAUNTLET_MAP_WIDGET_ID;

		if (isMapOpen)
			gauntletMapWidget = client.getWidget(GAUNTLET_MAP_WIDGET_ID, 3);
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if (!isInGauntlet())
			return;

		if (widgetClosed.getGroupId() == GAUNTLET_MAP_WIDGET_ID)
		{
			isMapOpen = false;
			gauntletMapWidget = null;
		}
	}

	private ResourceNode gameObjectToResource(GameObject gameObject)
	{
		switch (gameObject.getId())
		{
			case ObjectID.CRYSTAL_DEPOSIT:
			case ObjectID.CORRUPT_DEPOSIT:
				return new OreDeposit(gameObject);
			case ObjectID.PHREN_ROOTS:
			case ObjectID.PHREN_ROOTS_36066:
				return new PhrenRoot(gameObject);
			case ObjectID.LINUM_TIRINUM:
			case ObjectID.LINUM_TIRINUM_36072:
				return new LinumTirinum(gameObject);
			case ObjectID.GRYM_ROOT:
			case ObjectID.GRYM_ROOT_36070:
				return new GrymRoot(gameObject);
			case ObjectID.FISHING_SPOT_36068:
			case ObjectID.FISHING_SPOT_35971:
				return new FishingSpot(gameObject);
			default:
				return null;
		}
	}

	public boolean isInNormal()
	{
		if (client.getLocalPlayer() == null)
			return false;

		return client.getMapRegions()[0] == CRYSTAL_GAUNTLET_REGION_ID;
	}

	public boolean isInCorrupted()
	{
		if (client.getLocalPlayer() == null)
			return false;

		return client.getMapRegions()[0] == CORRUPTED_GAUNTLET_REGION_ID;
	}

	public boolean isInGauntlet()
	{
		return isInNormal() || isInCorrupted();
	}

	private boolean hasAlreadyFoundNode(ResourceNode resourceNode)
	{
		WorldPoint objectLocation = resourceNode.getGameObject().getWorldLocation();
		for (ResourceNode foundNode : foundNodes)
		{
			if (foundNode.getX() == objectLocation.getX() && foundNode.getY() == objectLocation.getY())
				return true;
		}
		return false;
	}
}
