package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class TankInfected extends Infected {
    public TankInfected(EntityType<? extends TankInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(1000, 16, 0.30); }
}
