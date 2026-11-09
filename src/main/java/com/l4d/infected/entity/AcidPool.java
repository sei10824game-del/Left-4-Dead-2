package com.l4d.infected.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class AcidPool extends Entity {
    public AcidPool(EntityType<? extends AcidPool> t, Level l) { super(t, l); }
    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag t) {}
    @Override protected void addAdditionalSaveData(CompoundTag t) {}
    @Override public void tick() { super.tick(); if (!level().isClientSide && tickCount > 200) discard(); }
}
