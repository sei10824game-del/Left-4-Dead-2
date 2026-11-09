package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HunterInfected extends Infected {
    public HunterInfected(EntityType<? extends HunterInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(40, 5, 0.36); }
}
