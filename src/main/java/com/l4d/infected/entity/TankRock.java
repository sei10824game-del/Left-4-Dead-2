package com.l4d.infected.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class TankRock extends ThrowableItemProjectile {
    public TankRock(EntityType<? extends TankRock> t, Level l) { super(t, l); }
    @Override protected Item getDefaultItem() { return Items.COBBLESTONE; }
    @Override protected void onHit(HitResult r) { if (!level().isClientSide) discard(); }
}
