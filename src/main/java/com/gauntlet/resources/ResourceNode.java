package com.gauntlet.resources;

import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SkillIconManager;

import java.awt.image.BufferedImage;

public abstract class ResourceNode {

    private final GameObject gameObject;
    private final Skill skill;
    private BufferedImage image;
    private final int x;
    private final int y;

    protected ResourceNode(GameObject gameObject, Skill skill)
    {
        this.gameObject = gameObject;
        this.skill = skill;
        this.image = new SkillIconManager().getSkillImage(skill, true);
        this.x = gameObject.getWorldLocation().getX();
        this.y = gameObject.getWorldLocation().getY();
    }

    public GameObject getGameObject()
    {
        return gameObject;
    }

    public Skill getSkill()
    {
        return skill;
    }

    public BufferedImage getImage()
    {
        return image;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public void setImage(boolean large)
    {
        this.image = new SkillIconManager().getSkillImage(skill, !large);
    }

    public Point getMapCanvasLocation(Widget gauntletMapWidget, WorldPoint gridOffset)
    {
        if (gauntletMapWidget == null)
            return null;

        int gridX = x - gridOffset.getX();
        int gridY = y - gridOffset.getY();
        int bottomLeftX = gauntletMapWidget.getCanvasLocation().getX() + 5;
        int bottomLeftY = gauntletMapWidget.getCanvasLocation().getY() + gauntletMapWidget.getHeight() - 1;
        int mapX = bottomLeftX + gridX * 2 - 2;
        int mapY = bottomLeftY - (int)(gridY * 2.325) - 2;
        return new Point(mapX, mapY);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ResourceNode))
            return false;

        if (this == o)
            return true;

        return gameObject.equals(((ResourceNode) o).getGameObject());
    }

    @Override
    public int hashCode()
    {
        return gameObject.hashCode();
    }
}
