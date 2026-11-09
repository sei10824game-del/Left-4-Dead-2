package com.l4d.infected.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/**
 * Base of every infected.
 * <p>Animation contract: server calls {@link #startAction(int, int)}; the id + length are synced, and both sides
 * count {@link #actionTick}. Models read {@link #action()} and {@link #actionTime(float)} /
 * {@link #actionProgress(float)} so the swing visible on screen IS the swing that deals damage
 * ({@link #onActionTick} fires the hit on a specific frame).
 */
public abstract class Infected extends Monster {
    public static final int NONE = 0, CLAW = 1; // subclasses use 2+
    static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);
    static final EntityDataAccessor<Integer> ACTION_LEN = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);
    static final EntityDataAccessor<Boolean> ALT = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.BOOLEAN);
    static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Infected.class, EntityDataSerializers.INT);

    public int actionTick;
    protected int clawCooldown;

    protected Infected(EntityType<? extends Infected> t, Level l) {
        super(t, l);
        xpReward = 10;
        if (getNavigation() instanceof GroundPathNavigation g) g.setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder baseAttrs(double hp, double dmg, double speed) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, hp).add(Attributes.ATTACK_DAMAGE, dmg)
                .add(Attributes.MOVEMENT_SPEED, speed).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(Attributes.ATTACK_KNOCKBACK, 0.4);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACTION, 0);
        entityData.define(ACTION_LEN, 0);
        entityData.define(ALT, false);
        entityData.define(VARIANT, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == ACTION) actionTick = 0;
    }

    // ---------- action API ----------
    public int action() { return entityData.get(ACTION); }
    public int actionLen() { return entityData.get(ACTION_LEN); }
    /** true when the claw uses the left hand. */
    public boolean alt() { return entityData.get(ALT); }
    public int variant() { return entityData.get(VARIANT); }
    public void setVariant(int v) { entityData.set(VARIANT, v); }
    /** ticks since the action began (smooth). */
    public float actionTime(float pt) { return actionTick + pt; }
    /** 0..1 over the action length (clamped; 0 when len unlimited). */
    public float actionProgress(float pt) {
        int len = actionLen();
        return len <= 0 ? 0 : Math.min(1f, (actionTick + pt) / len);
    }

    /** len <= 0: open ended, ends via {@link #endAction()}. */
    public void startAction(int id, int len) {
        entityData.set(ACTION_LEN, len);
        if (action() == id) entityData.set(ACTION, 0); // force a sync edge for repeated actions
        entityData.set(ACTION, id);
        actionTick = 0;
    }
    public void endAction() { if (action() != NONE) { entityData.set(ACTION, NONE); actionTick = 0; } }
    public boolean busy() { return action() != NONE; }

    /** server: called every tick of an action BEFORE actionTick increments. */
    protected void onActionTick(int id, int t) {
        if (id == CLAW) {
            int len = actionLen();
            if (t == len * 5 / 12) hitTarget(1f);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (clawCooldown > 0) clawCooldown--;
        int a = action();
        if (a != NONE) {
            if (!level().isClientSide) {
                onActionTick(a, actionTick);
                int len = actionLen();
                if (len > 0 && actionTick >= len) { endAction(); return; }
            }
            actionTick++;
        }
    }

    // ---------- combat helpers ----------
    public double reach() { return 2.0 + getBbWidth() * 0.5; }

    public boolean inReach(LivingEntity t) {
        return distanceToSqr(t.getX(), t.getY(), t.getZ()) <= Math.pow(reach() + t.getBbWidth() * 0.5, 2)
                && Math.abs(getY() - t.getY()) < 2.2 + t.getBbHeight() * 0.5;
    }

    /** Standard melee hit used at the strike frame; returns true on damage. */
    public boolean hitTarget(float mult) {
        LivingEntity t = getTarget();
        if (t == null || !t.isAlive() || !inReach(t)) return false;
        float dmg = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * mult;
        boolean ok = t.hurt(damageSources().mobAttack(this), dmg);
        if (ok) {
            double kb = getAttributeValue(Attributes.ATTACK_KNOCKBACK) * mult;
            t.knockback(kb, Math.sin(getYRot() * Math.PI / 180), -Math.cos(getYRot() * Math.PI / 180));
            onHitEffect(t);
        }
        return ok;
    }
    protected void onHitEffect(LivingEntity t) {}

    /** infected never harm / target each other. */
    public static boolean isEnemy(LivingEntity e) {
        return !(e instanceof Infected) && e.isAlive() && !(e instanceof Player p && (p.isCreative() || p.isSpectator()));
    }

    @Override
    public boolean canAttack(LivingEntity e) { return !(e instanceof Infected) && super.canAttack(e); }

    @Override
    public boolean hurt(DamageSource s, float a) {
        if (s.getEntity() instanceof Infected) return false;
        return super.hurt(s, a);
    }

    @Override
    public void setTarget(LivingEntity t) {
        boolean fresh = t != null && getTarget() != t;
        super.setTarget(t);
        if (fresh && !level().isClientSide && alertsHorde()) alertNearby(t);
    }
    protected boolean alertsHorde() { return true; }

    /** horde instinct: infected nearby join in on the same target. */
    protected void alertNearby(LivingEntity t) {
        List<Infected> l = level().getEntitiesOfClass(Infected.class, getBoundingBox().inflate(18, 8, 18), i -> i != this && i.getTarget() == null && !(i instanceof WitchInfected));
        for (Infected i : l) if (random.nextFloat() < 0.8f) i.setTarget(t);
    }

    // ---------- AI ----------
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new BreakDoorGoal(this, d -> true));
        registerActionGoals();
        goalSelector.addGoal(4, new InfectedMelee(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, Infected.class).setAlertOthers());
        registerTargets();
    }

    /** specials add their attack goals (priority 2/3) here. */
    protected void registerActionGoals() {}

    protected void registerTargets() {
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, false,
                e -> e instanceof Mob || e instanceof Player ? isEnemy(e) : false));
    }

    /** Move towards target, then play the claw action. Windup is visible and the hit lands on the strike frame. */
    public static class InfectedMelee extends Goal {
        final Infected m;
        int repath;
        public InfectedMelee(Infected m) { this.m = m; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { LivingEntity t = m.getTarget(); return t != null && t.isAlive(); }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void start() { m.setAggressive(true); }
        @Override public void stop() { m.setAggressive(false); m.getNavigation().stop(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override
        public void tick() {
            LivingEntity t = m.getTarget();
            if (t == null) return;
            m.getLookControl().setLookAt(t, 30, 30);
            if (m.action() == CLAW) { m.getNavigation().stop(); return; }
            if (m.busy()) return;
            if (--repath <= 0) { repath = 4 + m.random.nextInt(4); m.getNavigation().moveTo(t, m.moveSpeedMul()); }
            if (m.clawCooldown <= 0 && m.inReach(t)) {
                m.entityData.set(ALT, !m.alt());
                m.startAction(CLAW, m.clawLength());
                m.clawCooldown = m.clawLength() + 6;
                m.playSound(m.getAttackSwingSound(), 1f, 0.9f + m.random.nextFloat() * 0.2f);
            }
        }
    }
    protected double moveSpeedMul() { return 1.15; }
    protected int clawLength() { return 14; }
    protected SoundEvent getAttackSwingSound() { return SoundEvents.PLAYER_ATTACK_SWEEP; }

    // ---------- sounds (vanilla based, pitched per type) ----------
    protected float voicePitch() { return 0.9f + random.nextFloat() * 0.2f; }
    @Override public float getVoicePitch() { return voicePitch(); }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ZOMBIE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
    @Override protected void playStepSound(BlockPos p, BlockState b) { playSound(SoundEvents.ZOMBIE_STEP, 0.15f, 1f); }

    @Override
    public void addAdditionalSaveData(CompoundTag t) { super.addAdditionalSaveData(t); t.putInt("Variant", variant()); }
    @Override
    public void readAdditionalSaveData(CompoundTag t) { super.readAdditionalSaveData(t); setVariant(t.getInt("Variant")); }
    @Override public boolean removeWhenFarAway(double d) { return !hasCustomName() && super.removeWhenFarAway(d); }
}
