package com.l4d.infected.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * The common infected (variant picks one of several skins) and the base of every uncommon.
 * Behaviour on top of {@link Infected}: sprints when it sees its target, vaults 1-block ledges, sometimes stumbles,
 * and mixes two crafted attacks into the claw: a two-handed overhead {@link #SLAM} and a short {@link #LUNGE} leap.
 * Uncommons add signature attacks (ids 20+) via {@link #chooseAttack} / {@link #actionLength} / {@link #onActionTick}.
 */
public class CommonInfected extends Infected {
    public static final int VARIANTS = 6;
    public static final int SLAM = 10, LUNGE = 11, STUMBLE = 12;
    protected int specialCooldown = 40;
    private boolean leapHit;
    /** client: smoothed 0..1 "aggro run" blend for the model. */
    public float run, runO;

    public CommonInfected(EntityType<? extends CommonInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(30, 3, 0.31); }

    @Nullable @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor w, DifficultyInstance d, MobSpawnType r, @Nullable SpawnGroupData g, @Nullable net.minecraft.nbt.CompoundTag n) {
        setVariant(random.nextInt(VARIANTS));
        return super.finalizeSpawn(w, d, r, g, n);
    }

    // ---------- tuning hooks for uncommons ----------
    protected boolean runs() { return true; }
    protected float stumbleChance() { return 1 / 500f; }
    /** deaf infected ignore aggro they cannot see (horde alerts, noise) unless hurt by it or very close. */
    protected boolean deaf() { return false; }

    /** pick a special attack, or NONE to leave it to the claw. dist = distance to target. */
    protected int chooseAttack(LivingEntity t, double dist) {
        if (inReach(t) && random.nextFloat() < 0.3f) return SLAM;
        if (dist > 3 && dist < 6.5 && onGround() && hasLineOfSight(t) && random.nextFloat() < 0.35f) return LUNGE;
        return NONE;
    }
    protected int actionLength(int id) { return id == SLAM ? 22 : id == STUMBLE ? 18 : 22; }
    protected int attackCooldown(int id) { return 30 + random.nextInt(40); }

    @Override
    protected void onActionTick(int id, int t) {
        switch (id) {
            case SLAM -> {
                if (t == 0) playSound(SoundEvents.ZOMBIE_AMBIENT, 1f, 0.7f);
                if (t == 11 && hitTarget(1.6f)) playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 0.7f, 0.8f);
            }
            case LUNGE -> leap(t, 7, 0.42, 1.3f);
            case STUMBLE -> {
                if (t == 0) { getNavigation().stop(); setDeltaMovement(getDeltaMovement().scale(0.4)); }
                if (t == 5) playSound(SoundEvents.ZOMBIE_STEP, 0.8f, 0.6f);
            }
            default -> super.onActionTick(id, t);
        }
    }

    /** leap at the target on frame {@code launch}, then deal one hit on first contact while airborne. */
    protected void leap(int t, int launch, double up, float mult) {
        LivingEntity tg = getTarget();
        if (t == 0) leapHit = false;
        if (tg == null) return;
        if (t == launch) {
            Vec3 d = new Vec3(tg.getX() - getX(), 0, tg.getZ() - getZ());
            double len = d.length();
            if (len > 0.01) d = d.scale(Math.max(0.2, Math.min(0.95, (len - 1.0) / 6.5)) / len);
            setDeltaMovement(d.x, up, d.z);
            hasImpulse = true;
            playSound(SoundEvents.ZOMBIE_INFECT, 0.8f, 1.2f);
        }
        if (t > launch && t < launch + 10 && !leapHit && inReach(tg) && distanceToSqr(tg) < 4.5) leapHit = hitTarget(mult);
    }

    /** forward vector of the body (horizontal). */
    protected Vec3 facing() {
        double r = Math.toRadians(yBodyRot);
        return new Vec3(-Math.sin(r), 0, Math.cos(r));
    }

    /** extra knockback along our facing (players need hurtMarked to receive it). */
    protected void shove(@Nullable LivingEntity t, double h, double up) {
        if (t == null) return;
        Vec3 f = facing();
        t.push(f.x * h, up, f.z * h);
        t.hurtMarked = true;
    }

    @Override
    public void setTarget(@Nullable LivingEntity t) {
        if (deaf() && t != null && getTarget() == null && t != getLastHurtByMob() && distanceToSqr(t) > 25 && !hasLineOfSight(t)) return;
        super.setTarget(t);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            runO = run;
            run += ((isAggressive() || isSprinting() ? 1 : 0) - run) * 0.15f;
            clientFx();
            return;
        }
        if (specialCooldown > 0) specialCooldown--;
        LivingEntity t = getTarget();
        boolean sprint = runs() && t != null && !busy() && isEnemy(t) && getSensing().hasLineOfSight(t);
        if (sprint != isSprinting()) setSprinting(sprint);
        if (sprint && onGround() && random.nextFloat() < stumbleChance() && distanceToSqr(t) > 25) startAction(STUMBLE, actionLength(STUMBLE));
        // hop up 1-block ledges instead of grinding against them
        if (horizontalCollision && onGround() && !busy() && !getNavigation().isDone()) getJumpControl().jump();
    }

    /** client-side particles etc. */
    protected void clientFx() {}

    @Override
    protected void registerActionGoals() { goalSelector.addGoal(3, new SpecialAttack(this)); }

    /** Runs the special attacks: picks one, faces the target and holds still (the action itself moves the body). */
    static class SpecialAttack extends Goal {
        final CommonInfected m;
        int id;
        SpecialAttack(CommonInfected m) { this.m = m; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            LivingEntity t = m.getTarget();
            if (t == null || !isEnemy(t) || m.busy() || m.specialCooldown > 0) return false;
            id = m.chooseAttack(t, m.distanceTo(t));
            if (id == NONE) { m.specialCooldown = 10; return false; }
            return true;
        }
        @Override public boolean canContinueToUse() { return m.action() == id && m.isAlive(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override
        public void start() {
            m.setAggressive(true);
            m.getNavigation().stop();
            m.startAction(id, m.actionLength(id));
            m.specialCooldown = m.actionLength(id) + m.attackCooldown(id);
        }
        @Override
        public void tick() {
            LivingEntity t = m.getTarget();
            if (t != null) m.getLookControl().setLookAt(t, 30, 30);
            m.getNavigation().stop();
        }
    }
}
