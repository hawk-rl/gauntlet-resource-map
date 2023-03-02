package com.gauntlet.resources;

import net.runelite.api.GameObject;
import net.runelite.api.Skill;

public class GrymRoot extends ResourceNode {

    public GrymRoot(GameObject gameObject) {
        super(gameObject, Skill.HERBLORE);
    }
}
