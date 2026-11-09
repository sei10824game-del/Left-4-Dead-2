package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Smoker: lashes its tongue at a victim up to {@link #RANGE} blocks away, reels them in and chokes them.
 * The tongue snaps on a heavy melee hit, death, or 2s without line of sight. Dies in a blinding smoke cloud.
 */
public class SmokerInfected extends Infected {
    public static final int TONGUE = 2, PULL = 3, CHOKE = 4, RETRACT = 5, RECOIL = 6;
    /** tick of the TONGUE action on which the tongue arrives (model + renderer use the same frame). */
    public static final int TONGUE_LEN = 18, TONGUE_HIT = 11, CHOKE_PERIOD = 20;
    static final double RANGE = 15;
    static final EntityDataAccessor<Integer> VICTIM = SynchedEntityData.defineId(SmokerInfected.class, EntityDataSerializers.INT);

    int tongueCooldown = 40, noSight;

    public SmokerInfected(EntityType<? extends SmokerInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(250, 4, 0.25); }

    @Override
    protected void defineSynchedData() { super.defineSynchedData(); entityData.define(VICTIM, -1); }
    /** entity the tongue is attached to / flying at (client: may be null if not loaded). */
    public Entity victim() { int id = entityData.get(VICTIM); return id < 0 ? null : level().getEntity(id); }
    void setVictim(int id) { entityData.set(VICTIM, id); }
    boolean tethered() { return action() == PULL || action() == CHOKE; }

    @Override
    protected void registerActionGoals() { goalSelector.addGoal(2, new TongueGoal()); }

    @Override
    protected void onActionTick(int id, int t) {
        super.onActionTick(id, t);
        if (id < TONGUE) return;
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        LivingEntity v = Grip.resolve(this, entityData.get(VICTIM));
        if (v != null) face(v);
        switch (id) {
            case TONGUE -> {
                if (t == 2) playSound(SoundEvents.ZOMBIE_VILLAGER_AMBIENT, 1.2f, 0.5f);
                if (t == TONGUE_HIT - 3) playSound(SoundEvents.FISHING_BOBBER_THROW, 1.5f, 0.5f);
                if (t != TONGUE_HIT) return;
                if (v != null && distanceTo(v) <= RANGE + 1.5 && hasLineOfSight(v)) {
                    v.hurt(damageSources().mobAttack(this), 2f);
                    playSound(SoundEvents.SLIME_ATTACK, 1.2f, 0.6f);
                    noSight = 0;
                    startAction(PULL, 0);
                } else {
                    tongueCooldown = 80;
                    startAction(RETRACT, 8);
                }
            }
            case PULL -> {
                if (v == null || distanceTo(v) > RANGE + 6 || (noSight = hasLineOfSight(v) ? 0 : noSight + 1) > 40) { release(); return; }
                if (distanceTo(v) < 1.8) { startAction(CHOKE, 0); playSound(SoundEvents.SLIME_SQUISH, 1f, 0.5f); return; }
                Vec3 d = position().subtract(v.position());
                Vec3 h = new Vec3(d.x, 0, d.z).normalize().scale(0.24);
                double dy = v.horizontalCollision ? 0.42 : d.y > 1 ? 0.2 : v.getDeltaMovement().y;
                Grip.push(v, new Vec3(h.x, dy, h.z));
                Grip.subdue(v, 3);
                if (t % 12 == 0) playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.8f, 0.6f);
            }
            case CHOKE -> {
                if (v == null || distanceTo(v) > 4) { release(); return; }
                Vec3 fwd = Vec3.directionFromRotation(0, yBodyRot);
                Grip.place(v, position().add(fwd.scale(0.85)).add(0, 0.45, 0));
                if (t % CHOKE_PERIOD == CHOKE_PERIOD / 2) {   // the squeeze frame of the model's grip cycle
                    v.hurt(damageSources().mobAttack(this), 3f);
                    playSound(SoundEvents.SLIME_SQUISH, 1f, 0.4f);
                }
            }
            case RETRACT -> { if (t >= 7) setVictim(-1); }
            default -> {}
        }
    }

    void face(Entity e) {
        float yaw = (float) (Mth.atan2(e.getZ() - getZ(), e.getX() - getX()) * Mth.RAD_TO_DEG) - 90f;
        setYRot(yaw); yBodyRot = yaw; yHeadRot = yaw;
        getLookControl().setLookAt(e, 30, 30);
    }

    void release() {
        setVictim(-1);
        tongueCooldown = 160;
        playSound(SoundEvents.SLIME_HURT, 1.2f, 0.5f);
        startAction(RECOIL, 14);
    }

    @Override
    public boolean hurt(DamageSource s, float a) {
        boolean ok = super.hurt(s, a);
        // a heavy melee blow (direct attacker, not a projectile) snaps the tongue
        if (ok && isAlive() && tethered() && a >= 6 && s.getEntity() instanceof LivingEntity && s.getDirectEntity() == s.getEntity()) release();
        return ok;
    }

    @Override
    public void tick() {
        super.tick();
        if (tongueCooldown > 0) tongueCooldown--;
        if (!level().isClientSide) return;
        // smoke seeping from the mouth (more while walking / choking) and the smouldering back tumours
        Vec3 f = Vec3.directionFromRotation(0, yBodyRot);
        boolean moving = getDeltaMovement().horizontalDistanceSqr() > 1e-3;
        if (random.nextFloat() < (moving || tethered() ? 0.5f : 0.12f))
            level().addParticle(ParticleTypes.SMOKE, getX() + f.x * 0.4, getY() + 1.9, getZ() + f.z * 0.4, f.x * 0.02, 0.01, f.z * 0.02);
        if (random.nextFloat() < 0.08f)
            level().addParticle(ParticleTypes.LARGE_SMOKE, getX() - f.x * 0.35, getY() + 1.7, getZ() - f.z * 0.35, 0, 0.02, 0);
        if (action() == TONGUE && actionTick == TONGUE_HIT - 3)
            for (int i = 0; i < 8; i++)
                level().addParticle(ParticleTypes.SMOKE, getX() + f.x * 0.6, getY() + 1.8, getZ() + f.z * 0.6, f.x * 0.15 + random.nextGaussian() * 0.03, 0.02, f.z * 0.15 + random.nextGaussian() * 0.03);
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        setVictim(-1);
        if (!(level() instanceof ServerLevel sl)) return;
        AreaEffectCloud c = new AreaEffectCloud(sl, getX(), getY() + 0.2, getZ());
        c.setRadius(4f);
        c.setDuration(200);
        c.setWaitTime(0);
        c.setRadiusPerTick(-0.008f);
        c.setParticle(ParticleTypes.LARGE_SMOKE);
        c.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50));
        c.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50));
        sl.addFreshEntity(c);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 80, 1.6, 0.9, 1.6, 0.02);
        playSound(SoundEvents.FIRE_EXTINGUISH, 1.5f, 0.5f);
    }

    @Override protected float voicePitch() { return 0.6f + random.nextFloat() * 0.15f; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_VILLAGER_AMBIENT; }
    @Override protected double moveSpeedMul() { return 1.0; }
    @Override protected int clawLength() { return 16; }

    /** Tongue first; while it recharges keep 8-12 blocks away, clawing only when cornered. */
    class TongueGoal extends Goal {
        int repath;
        TongueGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            if (action() >= TONGUE) return true;
            LivingEntity t = getTarget();
            if (t == null || !isEnemy(t) || action() == CLAW || !Grip.grabbable(t)) return false;   // bosses: just claw
            return tongueCooldown <= 0 || distanceToSqr(t) > 9;
        }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void stop() { getNavigation().stop(); }
        @Override public void tick() {
            LivingEntity t = getTarget();
            if (action() >= TONGUE || t == null) return;
            getLookControl().setLookAt(t, 30, 30);
            double d = distanceTo(t);
            boolean sight = getSensing().hasLineOfSight(t);
            if (tongueCooldown <= 0 && sight && d <= RANGE && Grip.grabbable(t)) {
                setVictim(t.getId());
                startAction(TONGUE, TONGUE_LEN);
                return;
            }
            if (--repath > 0) return;
            repath = 8;
            if (tongueCooldown > 0 && d < 8) {
                Vec3 away = DefaultRandomPos.getPosAway(SmokerInfected.this, 12, 6, t.position());
                if (away != null) getNavigation().moveTo(away.x, away.y, away.z, 1.25);
            } else if (!sight || d > (tongueCooldown <= 0 ? RANGE - 1 : 12)) getNavigation().moveTo(t, 1.0);
            else getNavigation().stop();
        }
    }
}
