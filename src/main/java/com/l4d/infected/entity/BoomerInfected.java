package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class BoomerInfected extends BloaterBase {
    public BoomerInfected(EntityType<? extends BoomerInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return bloaterAttrs(0.20); }
}
