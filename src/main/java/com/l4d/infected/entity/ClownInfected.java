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

/** Clown: bouncy and loud. Its horn draws every infected within 40 blocks onto its target. */
public class ClownInfected extends CommonInfected {
    public static final int HONK = 70;
    private int honkCd = 100;
    public ClownInfected(EntityType<? extends ClownInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(30, 3, 0.32); }
    @Override protected int actionLength(int id) { return id == HONK ? 22 : super.actionLength(id); }
    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (honkCd > 0) honkCd--;
        LivingEntity t = getTarget();
        if (honkCd == 0 && t != null && !busy() && isEnemy(t)) { startAction(HONK, 22); honkCd = 240 + random.nextInt(200); }
    }
    @Override
    protected void onActionTick(int id, int t) {
        if (id != HONK) { super.onActionTick(id, t); return; }
        getNavigation().stop();
        if (t == 10) {
            playSound(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2).get(), 4f, 1.5f);
            LivingEntity tg = getTarget();
            if (tg != null) for (Infected i : level().getEntitiesOfClass(Infected.class, getBoundingBox().inflate(40, 20, 40), i -> i != this && !(i instanceof WitchInfected) && i.getTarget() == null)) i.setTarget(tg);
            if (level() instanceof net.minecraft.server.level.ServerLevel sl) sl.sendParticles(ParticleTypes.NOTE, getX(), getY() + 2.3, getZ(), 6, 0.5, 0.3, 0.5, 1);
        }
    }
}
