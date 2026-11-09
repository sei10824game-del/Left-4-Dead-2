package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Meat Wall: a slow, wide mass of fused flesh. Wide two-fist SMASH (area, launches) and a body CRUSH shove. */
public class MeatWallInfected extends Infected {
    public static final int SMASH = 2, CRUSH = 3;
    public MeatWallInfected(EntityType<? extends MeatWallInfected> t, Level l) { super(t, l); setMaxUpStep(1f); }
    public static AttributeSupplier.Builder attrs() {
        return baseAttrs(400, 10, 0.13).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ARMOR, 6);
    }
    @Override public double reach() { return 3.0; }
    @Override protected int clawLength() { return 22; }
    @Override protected float voicePitch() { return 0.5f + random.nextFloat() * 0.1f; }

    @Override
    protected void registerActionGoals() {
        goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.Goal() {
            int id;
            @Override public boolean canUse() {
                LivingEntity t = getTarget();
                if (t == null || busy() || clawCooldown > 0 || !inReach(t)) return false;
                id = random.nextFloat() < 0.6f ? SMASH : CRUSH; return true;
            }
            @Override public void start() { getNavigation().stop(); startAction(id, id == SMASH ? 30 : 24); clawCooldown = 60; }
        });
    }

    @Override
    protected void onActionTick(int id, int t) {
        if (id == SMASH) {
            if (t == 4) playSound(SoundEvents.RAVAGER_ROAR, 1f, 0.5f);
            if (t == 17) {                                   // fists land: shockwave ring
                playSound(SoundEvents.GENERIC_EXPLODE, 0.8f, 0.6f);
                for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.2, 0.5, 3.2), Infected::isEnemy)) {
                    e.invulnerableTime = 0;
                    if (e.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                        Vec3 d = e.position().subtract(position()).multiply(1, 0, 1).normalize();
                        e.setDeltaMovement(d.x * 0.9, 0.55, d.z * 0.9); e.hurtMarked = true;
                    }
                }
                if (level() instanceof net.minecraft.server.level.ServerLevel sl)
                    sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.1, getZ(), 30, 1.8, 0.05, 1.8, 0.05);
            }
        } else if (id == CRUSH) {
            if (t == 6) { Vec3 f = Vec3.directionFromRotation(0, yBodyRot); setDeltaMovement(f.x * 0.5, 0.1, f.z * 0.5); hasImpulse = true; }
            if (t >= 8 && t <= 14 && t % 3 == 2)
                for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.4), Infected::isEnemy)) {
                    e.invulnerableTime = 0;
                    if (e.hurt(damageSources().mobAttack(this), 6f)) { Vec3 f = Vec3.directionFromRotation(0, yBodyRot); e.setDeltaMovement(f.x * 1.2, 0.3, f.z * 1.2); e.hurtMarked = true; playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1f, 0.5f); }
                }
        } else super.onActionTick(id, t);
    }
}
