package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class SmokerInfected extends Infected {
    public SmokerInfected(EntityType<? extends SmokerInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(250, 4, 0.27); }
}
