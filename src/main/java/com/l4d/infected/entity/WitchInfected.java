package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * Witch: sits sobbing and ignores everything until startled. Closeness, held light and sprinting fill a synced
 * startle meter (her head lifts, she shakes and growls); at 1.0 - or when hurt - she screams, rises and hunts the
 * provoker alone with a flurry of claw rakes. Once it is gone she wanders off crying and sits back down.
 * Variant 0 = patient, 1 = bride (same mechanics).
 */
public class WitchInfected extends Infected {
    public static final double HEALTH = 1000;
    public static final int SITTING = 0, ENRAGED = 1, LEAVING = 2;
    public static final int RISE = 2, RAKE = 3, RISE_LEN = 16, RAKE_LEN = 9, RAKE_HIT = 5;
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(WitchInfected.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> STARTLE = SynchedEntityData.defineId(WitchInfected.class, EntityDataSerializers.FLOAT);
    private static final Set<Item> LIGHTS = Set.of(Items.TORCH, Items.SOUL_TORCH, Items.LANTERN, Items.SOUL_LANTERN, Items.GLOWSTONE,
            Items.JACK_O_LANTERN, Items.SHROOMLIGHT, Items.SEA_LANTERN, Items.REDSTONE_TORCH);

    private int calmTimer, growlCd;
    private Vec3 scene = Vec3.ZERO;

    public WitchInfected(EntityType<? extends WitchInfected> t, Level l) { super(t, l); xpReward = 50; }
    public static AttributeSupplier.Builder attrs() {
        return baseAttrs(HEALTH, 18, 0.42).add(Attributes.ATTACK_KNOCKBACK, 0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MODE, SITTING);
        entityData.define(STARTLE, 0f);
    }
    public int mode() { return entityData.get(MODE); }
    public float startle() { return entityData.get(STARTLE); }

    @Nullable @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor w, DifficultyInstance d, MobSpawnType r, @Nullable SpawnGroupData g, @Nullable CompoundTag n) {
        setVariant(random.nextInt(4) == 0 ? 1 : 0);
        return super.finalizeSpawn(w, d, r, g, n);
    }

    /** no horde targeting, no strolling: her own goal decides everything. */
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new WitchGoal());
    }
    @Override protected boolean alertsHorde() { return false; }

    @Override
    public boolean hurt(DamageSource s, float a) {
        boolean ok = super.hurt(s, a);
        if (ok && !level().isClientSide && isAlive()) {
            LivingEntity cur = getTarget();
            if (mode() != ENRAGED || cur == null || !cur.isAlive()) {
                LivingEntity src = s.getEntity() instanceof LivingEntity le ? (isEnemy(le) ? le : null) : nearestEnemy(16);
                if (src != null) enrage(src);
                else entityData.set(STARTLE, Math.min(0.9f, startle() + 0.5f));
            }
        }
        return ok;
    }

    private void enrage(LivingEntity who) {
        setTarget(who);
        entityData.set(MODE, ENRAGED);
        entityData.set(STARTLE, 1f);
        startAction(RISE, RISE_LEN);
        playSound(SoundEvents.GHAST_SCREAM, 4f, 0.75f);
        playSound(SoundEvents.ENDERMAN_SCREAM, 3f, 0.55f);
    }

    @Nullable
    private LivingEntity nearestEnemy(double r) {
        return level().getNearestEntity(LivingEntity.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat().range(r)
                .selector(e -> isEnemy(e) && (e instanceof Player || e instanceof Mob)), this, getX(), getY(), getZ(), getBoundingBox().inflate(r));
    }

    /** startle meter: proximity, light held in sight, sprinting nearby. */
    private void tickStartle() {
        float add = 0;
        LivingEntity worst = null;
        float worstAdd = 0;
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(9, 4, 9),
                e -> isEnemy(e) && (e instanceof Player || e instanceof Mob))) {
            double d = distanceTo(e);
            float a = 0;
            if (d < 2.2) a = 1;
            else if (d < 4) a = 0.05f;
            if (d < 8 && hasLineOfSight(e)) {
                if (LIGHTS.contains(e.getMainHandItem().getItem()) || LIGHTS.contains(e.getOffhandItem().getItem())) a += 0.014f * (float) (9 - d) / 4;
                if (e.isSprinting()) a += 0.01f;
            }
            if (a > worstAdd) { worstAdd = a; worst = e; }
            add += a;
        }
        float s = Math.max(0, Math.min(1, startle() + (add > 0 ? add : -0.004f)));
        entityData.set(STARTLE, s);
        if (s > 0.3f && --growlCd <= 0) {
            growlCd = 30 + (int) ((1 - s) * 60);
            playSound(SoundEvents.WOLF_GROWL, 0.6f + s, 0.55f + s * 0.2f);
        }
        if (s >= 1 && worst != null) enrage(worst);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (mode() != ENRAGED && random.nextInt(14) == 0) // tears
                level().addParticle(ParticleTypes.FALLING_WATER, getX() + (random.nextDouble() - 0.5) * 0.3, getY() + (mode() == SITTING ? 0.8 : 1.3), getZ() + (random.nextDouble() - 0.5) * 0.3, 0, 0, 0);
            return;
        }
        if (mode() == SITTING) tickStartle();
    }

    class WitchGoal extends Goal {
        int repath;
        WitchGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return true; }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            switch (mode()) {
                case SITTING -> { getNavigation().stop(); if (t != null) setTarget(null); }
                case ENRAGED -> {
                    if (t == null || !t.isAlive() || !isEnemy(t) || distanceToSqr(t) > 48 * 48) { leave(); return; }
                    getLookControl().setLookAt(t, 40, 40);
                    if (action() == RISE) { getNavigation().stop(); return; }
                    if (action() == RAKE) { getNavigation().stop(); return; }
                    if (inReach(t)) {
                        entityData.set(ALT, !alt());
                        startAction(RAKE, RAKE_LEN);
                        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 1.3f + random.nextFloat() * 0.3f);
                    } else if (--repath <= 0) { repath = 3; getNavigation().moveTo(t, 1.2); }
                    if (tickCount % 30 == 0) playSound(SoundEvents.GHAST_HURT, 1.5f, 0.6f);
                }
                case LEAVING -> {
                    if (--calmTimer <= 0) { entityData.set(MODE, SITTING); entityData.set(STARTLE, 0f); getNavigation().stop(); return; }
                    if (getNavigation().isDone() && calmTimer > 40) {
                        Vec3 p = DefaultRandomPos.getPosAway(WitchInfected.this, 14, 5, scene);
                        if (p != null) getNavigation().moveTo(p.x, p.y, p.z, 0.4);
                    }
                }
                default -> {}
            }
        }
    }

    private void leave() {
        scene = getTarget() != null ? getTarget().position() : position();
        setTarget(null);
        endAction();
        entityData.set(MODE, LEAVING);
        calmTimer = 200;
        getNavigation().stop();
    }

    @Override
    protected void onActionTick(int id, int t) {
        if (id == RAKE && t == RAKE_HIT) hitTarget(1f);
    }

    @Override protected float voicePitch() { return 1.1f + random.nextFloat() * 0.2f; }
    @Override protected SoundEvent getAmbientSound() { return mode() == ENRAGED ? SoundEvents.GHAST_HURT : SoundEvents.GHAST_AMBIENT; }
    @Override public int getAmbientSoundInterval() { return 50; }
    @Override protected float getSoundVolume() { return mode() == ENRAGED ? 1.5f : 0.55f; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.GHAST_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GHAST_DEATH; }

    @Override public void addAdditionalSaveData(CompoundTag t) { super.addAdditionalSaveData(t); t.putInt("Mode", mode() == ENRAGED ? LEAVING : mode()); }
    @Override public void readAdditionalSaveData(CompoundTag t) { super.readAdditionalSaveData(t); entityData.set(MODE, t.getInt("Mode")); calmTimer = 200; scene = position(); }
}
