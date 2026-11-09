package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class RoadcrewInfected extends CommonInfected {
    public RoadcrewInfected(EntityType<? extends RoadcrewInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(40, 3, 0.30); }
}
