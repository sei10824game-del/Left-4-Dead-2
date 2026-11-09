package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Keeps its distance, lobs acid globs that leave burning pools; claws only when cornered, then backs off. */
public class SpitterInfected extends Infected {
    public static final int SPIT = 2;
    /** wind-up (neck arches, sac swells) until SPIT_RELEASE, head whips forward, recovery. */
    public static final int SPIT_LEN = 32, SPIT_RELEASE = 18;
    int spitCooldown = 30, retreat;

    public SpitterInfected(EntityType<? extends SpitterInfected> t, Level l) { super(t, l); xpReward = 15; }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(100, 3, 0.28); }

    @Override
    protected void registerActionGoals() { goalSelector.addGoal(2, new KeepAwayGoal()); }

    @Override
    protected void onActionTick(int id, int t) {
        if (id == SPIT) {
            LivingEntity tg = getTarget();
            if (tg != null) getLookControl().setLookAt(tg, 30, 30);
            if (t == 0) playSound(SoundEvents.WITCH_DRINK, 1f, 0.55f);
            if (t == 10) playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1f, 1.4f);
            if (t == SPIT_RELEASE && tg != null) spit(tg);
            return;
        }
        super.onActionTick(id, t);
        if (id == CLAW && t == actionLen() - 1) retreat = 50; // hit, then scramble away
    }

    private void spit(LivingEntity tg) {
        Vec3 m = getEyePosition().add(getViewVector(1f).scale(0.7));
        Vec3 aim = tg.position().add(tg.getDeltaMovement().multiply(1, 0, 1).scale(18)); // lead a moving target
        double dx = aim.x - m.x, dz = aim.z - m.z, h = Math.sqrt(dx * dx + dz * dz);
        AcidSpit s = new AcidSpit(level(), this);
        s.setPos(m.x, m.y, m.z);
        // fixed lob elevation; speed fitted to the projectile's gravity + drag so it lands ~h away
        s.shoot(dx, aim.y - m.y + h * 0.6, dz, (float) (0.135 * Math.pow(Math.max(h, 1), 0.616)), 2.5f);
        level().addFreshEntity(s);
        playSound(SoundEvents.LLAMA_SPIT, 1.6f, 0.55f);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (isAlive() && random.nextInt(14) == 0) { // acid dribbling from the jaw
                Vec3 m = getEyePosition().add(getViewVector(1f).scale(0.5));
                level().addParticle(ParticleTypes.ITEM_SLIME, m.x, m.y - 0.2, m.z, 0, -0.05, 0);
            }
            return;
        }
        if (spitCooldown > 0) spitCooldown--;
        if (retreat > 0) retreat--;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (dead && !level().isClientSide) level().addFreshEntity(new AcidPool(level(), getX(), getY(), getZ(), 1.6f, 100, this));
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.DROWNED_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.DROWNED_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.DROWNED_DEATH; }
    @Override protected float voicePitch() { return 1.15f + random.nextFloat() * 0.15f; }

    /** kite: back off inside ~9 blocks, close in beyond ~15 or without line of sight, spit when ready. */
    class KeepAwayGoal extends Goal {
        int repath;
        KeepAwayGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            LivingEntity t = getTarget();
            return t != null && isEnemy(t) && (distanceToSqr(t) > 3.2 * 3.2 || retreat > 0); // cornered -> melee goal takes over
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void stop() { getNavigation().stop(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 30, 30);
            if (busy()) { getNavigation().stop(); return; }
            double d = distanceTo(t);
            boolean sees = getSensing().hasLineOfSight(t);
            if (--repath <= 0 || getNavigation().isDone()) {
                repath = 10;
                if (d < 9 || retreat > 0) {
                    Vec3 away = DefaultRandomPos.getPosAway(SpitterInfected.this, 14, 6, t.position());
                    if (away != null) getNavigation().moveTo(away.x, away.y, away.z, 1.35);
                } else if (d > 15 || !sees) getNavigation().moveTo(t, 1.0);
                else getNavigation().stop();
            }
            if (spitCooldown <= 0 && sees && d > 3.5 && d < 22) {
                startAction(SPIT, SPIT_LEN);
                spitCooldown = 90 + random.nextInt(50);
                getNavigation().stop();
            }
        }
    }
}
