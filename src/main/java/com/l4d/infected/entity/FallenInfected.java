package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Fallen Soldier: slow, heavily kitted; rifle-butt thrust; drops field supplies. */
public class FallenInfected extends CommonInfected {
    public static final int BUTT = 60;
    public FallenInfected(EntityType<? extends FallenInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(60, 4, 0.25).add(Attributes.KNOCKBACK_RESISTANCE, 0.6); }
    @Override protected boolean runs() { return false; }
    @Override protected float stumbleChance() { return 0; }
    @Override protected int chooseAttack(LivingEntity t, double d) { return inReach(t) && random.nextFloat() < 0.5f ? BUTT : NONE; }
    @Override protected int actionLength(int id) { return id == BUTT ? 18 : super.actionLength(id); }
    @Override
    protected void onActionTick(int id, int t) {
        if (id != BUTT) { super.onActionTick(id, t); return; }
        if (t == 8 && hitTarget(1.5f)) { shove(getTarget(), 0.7, 0.2); playSound(SoundEvents.SHIELD_BLOCK, 0.8f, 0.6f); }
    }
    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource s, int looting, boolean recent) {
        super.dropCustomDeathLoot(s, looting, recent);
        ItemStack[] pool = {new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.BREAD, 2 + random.nextInt(3)), new ItemStack(Items.ARROW, 4 + random.nextInt(9)),
                new ItemStack(Items.IRON_NUGGET, 3 + random.nextInt(6)), new ItemStack(Items.GUNPOWDER, 1 + random.nextInt(3))};
        for (int i = 0; i < 2; i++) if (random.nextFloat() < 0.7f) spawnAtLocation(pool[random.nextInt(pool.length)].copy());
    }
}
