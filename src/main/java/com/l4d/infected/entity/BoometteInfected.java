package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class BoometteInfected extends BloaterBase {
    public BoometteInfected(EntityType<? extends BoometteInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return bloaterAttrs(0.21); }
    @Override protected float voicePitch() { return 0.85f + random.nextFloat() * 0.15f; }
}
