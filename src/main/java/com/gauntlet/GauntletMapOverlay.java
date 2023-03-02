package com.gauntlet;

import com.gauntlet.resources.ResourceNode;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class GauntletMapOverlay extends Overlay {

    private final GauntletMapPlugin plugin;

    @Inject
    private GauntletMapOverlay(GauntletMapPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.isMapOpen())
        {
            if (plugin.getGauntletMapWidget() == null)
                return null;

            for (ResourceNode node : plugin.getFoundNodes())
            {
                if (!plugin.isMapOpen())
                    break;

                if (node.getSkill().equals(Skill.FISHING) && !plugin.getConfig().displayFishingSpots())
                    continue;

                if (node.getSkill().equals(Skill.HERBLORE) && !plugin.getConfig().displayGrymRoot())
                    continue;

                if (node.getSkill().equals(Skill.FARMING) && !plugin.getConfig().displayLinumTirinum())
                    continue;

                if (node.getSkill().equals(Skill.MINING) && !plugin.getConfig().displayOreDeposit())
                    continue;

                if (node.getSkill().equals(Skill.WOODCUTTING) && !plugin.getConfig().displayPhrenRoot())
                    continue;

                Point mapCanvasLocation = node.getMapCanvasLocation(plugin.getGauntletMapWidget(), plugin.getGridOffset());
                OverlayUtil.renderImageLocation(graphics, mapCanvasLocation, node.getImage());
            }
        }
        return null;
    }
}
