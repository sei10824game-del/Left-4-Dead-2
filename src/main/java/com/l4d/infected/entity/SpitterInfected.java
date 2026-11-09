package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class SpitterInfected extends Infected {
    public SpitterInfected(EntityType<? extends SpitterInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(100, 3, 0.28); }
}
