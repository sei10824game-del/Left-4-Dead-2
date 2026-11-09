package com.l4d.infected.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Charger: roaring wind-up, then a locked-direction charge that scoops up the first enemy and bowls the rest aside.
 * A wall stuns it; otherwise the carried victim is pounded into the ground until someone dies or a heavy hit staggers it.
 */
public class ChargerInfected extends Infected {
    public static final int WINDUP = 2, CHARGE = 3, STUN = 4, POUND = 5, SKID = 6;
    public static final int WINDUP_LEN = 20, STUN_LEN = 40, SKID_LEN = 12, POUND_PERIOD = 16, POUND_HIT = 11;
    static final double SPEED = 0.5, MAX_DIST = 25;
    static final EntityDataAccessor<Integer> VICTIM = SynchedEntityData.defineId(ChargerInfected.class, EntityDataSerializers.INT);

    int chargeCooldown = 60, carryTicks;
    float lockedYaw;
    Vec3 chargeStart = Vec3.ZERO;
    final Set<Integer> bowled = new HashSet<>();

    public ChargerInfected(EntityType<? extends ChargerInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() {
        return baseAttrs(600, 8, 0.28).add(Attributes.KNOCKBACK_RESISTANCE, 0.8).add(Attributes.ATTACK_KNOCKBACK, 1.4);
    }

    @Override
    protected void defineSynchedData() { super.defineSynchedData(); entityData.define(VICTIM, -1); }
    public boolean carrying() { return entityData.get(VICTIM) >= 0; }
    void setVictim(int id) { entityData.set(VICTIM, id); }

    /** height of the fist (0 ground .. 1 overhead) over one pound cycle u in [0,1); shared by server + model. */
    public static float poundLift(float u) {
        if (u < 0.55f) { float k = u / 0.55f; return 0.15f + 0.85f * k * k * (3 - 2 * k); }
        if (u < 0.69f) { float k = (u - 0.55f) / 0.14f; return 1 - k * k; }
        return 0.15f * Math.min(1f, (u - 0.69f) / 0.3f);
    }

    @Override
    protected void registerActionGoals() { goalSelector.addGoal(2, new ChargeGoal()); }

    Vec3 dir() { return Vec3.directionFromRotation(0, lockedYaw); }
    void lockYaw(float yaw) { lockedYaw = yaw; setYRot(yaw); yBodyRot = yaw; yHeadRot = yaw; }
    float yawTo(LivingEntity e) { return (float) (Mth.atan2(e.getZ() - getZ(), e.getX() - getX()) * Mth.RAD_TO_DEG) - 90f; }
    float dmg() { return (float) getAttributeValue(Attributes.ATTACK_DAMAGE); }

    @Override
    protected void onActionTick(int id, int t) {
        super.onActionTick(id, t);
        if (id < WINDUP) return;
        getNavigation().stop();
        LivingEntity v = Grip.resolve(this, entityData.get(VICTIM));
        if (v == null && carrying()) setVictim(-1);
        if (id != CHARGE) setDeltaMovement(getDeltaMovement().multiply(id == SKID ? 0.6 : 0, 1, id == SKID ? 0.6 : 0));
        switch (id) {
            case WINDUP -> {
                LivingEntity tg = getTarget();
                lockYaw(tg != null && t < WINDUP_LEN - 3 ? yawTo(tg) : lockedYaw);
                if (t == 0) playSound(SoundEvents.RAVAGER_ROAR, 1.6f, 1.15f);
                if (t == 9 || t == 13) dust(position().add(dir().scale(-0.4)), 10, 0.3);   // paws the ground
                if (t == WINDUP_LEN) {
                    chargeStart = position(); bowled.clear(); carryTicks = 0;
                    startAction(CHARGE, 0);
                }
            }
            case CHARGE -> charge(v, t);
            case STUN -> {
                if (v != null) Grip.place(v, holdPoint(1.3, 0));
                if (t == actionLen()) { if (v != null) startAction(POUND, 0); }
            }
            case POUND -> {
                if (v == null || !v.isAlive()) { setVictim(-1); endAction(); return; }
                lockYaw(lockedYaw);
                int c = t % POUND_PERIOD;
                Grip.place(v, holdPoint(1.4, poundLift(c / (float) POUND_PERIOD) * 1.8));
                if (c == 3) playSound(SoundEvents.RAVAGER_ATTACK, 1f, 0.7f);
                if (c == POUND_HIT) {
                    v.hurt(damageSources().mobAttack(this), dmg() * 0.5f);
                    playSound(SoundEvents.GENERIC_BIG_FALL, 1.5f, 0.6f);
                    playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 0.8f, 0.5f);
                    dust(v.position(), 24, 0.6);
                }
            }
            default -> {}
        }
    }

    /** locked-direction run: carries the first enemy it meets, bowls the rest, stops at walls / range. */
    void charge(LivingEntity v, int t) {
        LivingEntity tg = getTarget();
        if (v == null && tg != null) lockedYaw = Mth.approachDegrees(lockedYaw, yawTo(tg), 1.2f);   // only slight steering
        lockYaw(lockedYaw);
        Vec3 d = dir();
        setDeltaMovement(d.x * SPEED, getDeltaMovement().y, d.z * SPEED);
        if (t % 2 == 0) dust(position(), 4, 0.4);
        if (t % 6 == 0) playSound(SoundEvents.RAVAGER_STEP, 1f, 0.9f);
        if (v != null) {
            Grip.place(v, holdPoint(0.9, 0.5));
            if (++carryTicks > 30) { startAction(POUND, 0); return; }
        }
        final LivingEntity vv = v;
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.3).move(d.scale(0.7)),
                e -> e != this && e != vv && isEnemy(e) && !bowled.contains(e.getId()))) {
            bowled.add(e.getId());
            if (v == null && Grip.grabbable(e)) {   // scoop up the first one
                v = e;
                setVictim(e.getId());
                e.hurt(damageSources().mobAttack(this), dmg() * 0.75f);
                playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.5f, 0.6f);
            } else if (e.hurt(damageSources().mobAttack(this), dmg() * 0.6f)) {   // bowl the rest aside
                Vec3 side = new Vec3(-d.z, 0, d.x);
                Vec3 push = d.add(side.scale(Math.signum(e.position().subtract(position()).dot(side)) + 0.01)).normalize();
                e.knockback(1.6, -push.x, -push.z);
                e.setDeltaMovement(e.getDeltaMovement().add(0, 0.35, 0));
                e.hurtMarked = true;
            }
        }
        if (horizontalCollision && t > 2) {   // slammed into a wall
            playSound(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 1.5f, 0.5f);
            playSound(SoundEvents.RAVAGER_STUNNED, 1.5f, 0.9f);
            dust(position().add(d.scale(0.6)), 30, 0.6);
            if (v != null) v.hurt(damageSources().mobAttack(this), dmg() * 0.5f);
            setDeltaMovement(-d.x * 0.2, 0.1, -d.z * 0.2);
            startAction(STUN, STUN_LEN);
        } else if (position().distanceTo(chargeStart) > MAX_DIST || isInWater() || t > 80) {
            if (v != null) startAction(POUND, 0); else startAction(SKID, SKID_LEN);
        }
    }

    Vec3 holdPoint(double fwd, double up) { return position().add(dir().scale(fwd)).add(0, up, 0); }

    void dust(Vec3 p, int n, double spread) {
        if (!(level() instanceof ServerLevel sl)) return;
        BlockState b = level().getBlockState(net.minecraft.core.BlockPos.containing(p.x, p.y - 0.2, p.z));
        if (!b.isAir()) sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, b), p.x, p.y + 0.1, p.z, n, spread, 0.1, spread, 0.15);
        sl.sendParticles(ParticleTypes.POOF, p.x, p.y + 0.2, p.z, n / 4 + 1, spread, 0.1, spread, 0.02);
    }

    @Override
    public boolean hurt(DamageSource s, float a) {
        if (action() == CHARGE && s.getSourcePosition() != null) {   // shoulder-first: frontal hits are halved
            Vec3 to = s.getSourcePosition().subtract(position());
            if (to.horizontalDistanceSqr() > 1e-4 && new Vec3(to.x, 0, to.z).normalize().dot(dir()) > 0.3) a *= 0.5f;
        }
        boolean ok = super.hurt(s, a);
        if (ok && isAlive() && action() == POUND && a >= 8 && s.getEntity() != null) {   // heavy blow staggers it off the victim
            setVictim(-1);
            startAction(STUN, 30);
        }
        return ok;
    }

    @Override
    public void tick() {
        super.tick();
        if (chargeCooldown > 0) chargeCooldown--;
    }

    @Override
    public void die(DamageSource s) { super.die(s); setVictim(-1); }

    @Override protected float voicePitch() { return 0.55f + random.nextFloat() * 0.1f; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.RAVAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.RAVAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override protected double moveSpeedMul() { return 1.0; }
    @Override protected int clawLength() { return 18; }
    @Override protected SoundEvent getAttackSwingSound() { return SoundEvents.PLAYER_ATTACK_STRONG; }

    /** Starts the wind-up when the target is in a clear lane, then keeps MOVE/LOOK while the move-set plays out. */
    class ChargeGoal extends Goal {
        ChargeGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            if (action() >= WINDUP) return true;
            LivingEntity t = getTarget();
            if (t == null || !isEnemy(t) || busy() || chargeCooldown > 0) return false;
            double d = distanceTo(t);
            return d > 4 && d < 20 && Math.abs(t.getY() - getY()) < 2.5 && getSensing().hasLineOfSight(t);
        }
        @Override public boolean canContinueToUse() { return action() >= WINDUP; }
        @Override public void start() {
            if (busy()) return;
            chargeCooldown = 200 + random.nextInt(60);
            LivingEntity t = getTarget();
            if (t != null) lockYaw(yawTo(t));
            startAction(WINDUP, WINDUP_LEN);
        }
        @Override public void stop() { getNavigation().stop(); }
    }
}
