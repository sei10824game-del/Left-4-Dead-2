package com.l4d.infected.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Tank: colossal brute. Alternating haymaker / hook punches, rips up and hurls rocks at range, slams the ground in a
 * shockwave when crowded and hops up onto ledges. Fire enrages it (faster) without making it immune. Boss bar.
 */
public class TankInfected extends Infected {
    public static final double HEALTH = 1000;
    /** action ids + lengths; strike frames are where damage lands (the models key their poses off the same numbers). */
    public static final int HAYMAKER = 2, HOOK = 3, THROW = 4, SLAM = 5, LEAP = 6;
    public static final int HAY_LEN = 30, HAY_HIT = 17, HOOK_LEN = 16, HOOK_HIT = 7, THROW_LEN = 52, THROW_REL = 38,
            SLAM_LEN = 36, SLAM_HIT = 19, LEAP_LEN = 18, LEAP_GO = 7, DEATH_LEN = 50;
    private static final UUID RAGE_ID = UUID.fromString("5c1f2d0e-7a8b-4e43-9b2a-3f6d1c0e7a11");

    private final ServerBossEvent bar = new ServerBossEvent(Component.literal("Tank"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private int punchCd, throwCd = 60, slamCd = 100, leapCd, rage;
    private boolean nextHook;

    public TankInfected(EntityType<? extends TankInfected> t, Level l) { super(t, l); xpReward = 100; setMaxUpStep(1.5f); }

    public static AttributeSupplier.Builder attrs() {
        return baseAttrs(HEALTH, 18, 0.32).add(Attributes.KNOCKBACK_RESISTANCE, 0.95).add(Attributes.ATTACK_KNOCKBACK, 2.5)
                .add(Attributes.FOLLOW_RANGE, 48).add(Attributes.ARMOR, 6);
    }

    @Override protected void registerActionGoals() { goalSelector.addGoal(2, new TankFight()); }
    @Override public double reach() { return 3.4; }

    // ---------------- actions ----------------
    @Override
    protected void onActionTick(int id, int t) {
        LivingEntity tg = getTarget();
        switch (id) {
            case HAYMAKER -> {
                if (t == 4) playSound(SoundEvents.RAVAGER_ROAR, 1.6f, 0.65f);
                if (t == HAY_HIT) {
                    playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 2f, 0.5f);
                    if (tg != null && hitTarget(1f)) launch(tg, 1.9, 0.85);
                    groundBurst(getX() + forward().x * 2.2, getZ() + forward().z * 2.2, 1.5, 12);
                }
            }
            case HOOK -> { if (t == HOOK_HIT) { playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.5f, 0.55f); if (tg != null && hitTarget(0.6f)) launch(tg, 0.9, 0.35); } }
            case THROW -> {
                if (t == 6) playSound(SoundEvents.STONE_BREAK, 2f, 0.5f);
                if (t >= 6 && t <= 16 && t % 2 == 0) groundBurst(getX() + forward().x * 1.6, getZ() + forward().z * 1.6, 0.8, 6);
                if (t == 22) playSound(SoundEvents.RAVAGER_ROAR, 1.8f, 0.6f);
                if (t == THROW_REL) throwRock(tg);
            }
            case SLAM -> {
                if (t == 6) playSound(SoundEvents.RAVAGER_ROAR, 2f, 0.55f);
                if (t == SLAM_HIT) shockwave();
                if (t > SLAM_HIT && t <= SLAM_HIT + 5) ring(t - SLAM_HIT + 0.5);
            }
            case LEAP -> {
                if (t == LEAP_GO) {
                    Vec3 f = tg != null ? new Vec3(tg.getX() - getX(), 0, tg.getZ() - getZ()).normalize() : forward();
                    setDeltaMovement(f.x * 0.55, 0.85, f.z * 0.55);
                    hasImpulse = true;
                    playSound(SoundEvents.RAVAGER_STEP, 2f, 0.5f);
                    groundBurst(getX(), getZ(), 1.2, 16);
                }
            }
            default -> {}
        }
    }

    private Vec3 forward() { float r = getYRot() * Mth.DEG_TO_RAD; return new Vec3(-Mth.sin(r), 0, Mth.cos(r)); }

    /** big up+away knock that also reaches players (hurtMarked syncs their velocity). */
    private void launch(LivingEntity t, double h, double up) {
        AttributeInstance kr = t.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        double res = 1 - (kr == null ? 0 : Math.min(1, kr.getValue()));
        Vec3 d = new Vec3(t.getX() - getX(), 0, t.getZ() - getZ()).normalize().scale(h * res);
        t.setDeltaMovement(d.x, up * res + 0.1, d.z);
        t.hurtMarked = true;
    }

    private void throwRock(LivingEntity tg) {
        Vec3 f = forward();
        TankRock r = new TankRock(level(), this, getX() + f.x * 0.8, getY() + 3.4, getZ() + f.z * 0.8);
        Vec3 aim = tg != null && tg.isAlive() ? tg.getEyePosition().subtract(0, 0.4, 0) : position().add(f.scale(20));
        double dx = aim.x - r.getX(), dz = aim.z - r.getZ(), dist = Math.sqrt(dx * dx + dz * dz), ticks = dist / TankRock.SPEED;
        double dy = aim.y - r.getY() + 0.5 * TankRock.GRAVITY * ticks * ticks; // lead the arc
        r.shoot(dx, dy, dz, TankRock.SPEED, 1.5f);
        level().addFreshEntity(r);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2f, 0.4f);
    }

    private void shockwave() {
        playSound(SoundEvents.GENERIC_EXPLODE, 2f, 0.55f);
        playSound(SoundEvents.ANVIL_LAND, 1.2f, 0.45f);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5, 2, 5), e -> e != this && isEnemy(e))) {
            double d = distanceTo(e);
            if (d > 5.5) continue;
            float k = (float) (1 - d / 6.5);
            if (e.hurt(damageSources().mobAttack(this), 8 * k + 2)) {
                launch(e, 1.2 * k + 0.3, 0.55 * k + 0.2);
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3), this); // stagger
            }
        }
        ring(0.8);
    }

    private void ring(double r) {
        if (!(level() instanceof ServerLevel sl)) return;
        BlockState b = groundState();
        int n = (int) (10 + r * 9);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n, x = getX() + Math.cos(a) * r, z = getZ() + Math.sin(a) * r;
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, b), x, getY() + 0.1, z, 3, 0.1, 0.05, 0.1, 0.2);
            if (i % 3 == 0) sl.sendParticles(ParticleTypes.POOF, x, getY() + 0.2, z, 1, 0, 0.05, 0, 0.04);
        }
    }

    private void groundBurst(double x, double z, double spread, int n) {
        if (level() instanceof ServerLevel sl)
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState()), x, getY() + 0.1, z, n, spread * 0.4, 0.1, spread * 0.4, 0.25);
    }

    private BlockState groundState() {
        BlockState b = level().getBlockState(BlockPos.containing(getX(), getY() - 0.2, getZ()));
        return b.isAir() ? Blocks.DIRT.defaultBlockState() : b;
    }

    // ---------------- per tick: boss bar, rage, cooldowns ----------------
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (punchCd > 0) punchCd--;
        if (throwCd > 0) throwCd--;
        if (slamCd > 0) slamCd--;
        if (leapCd > 0) leapCd--;
        bar.setProgress(getHealth() / getMaxHealth());
        if (tickCount % 10 == 0) {
            for (ServerPlayer p : ((ServerLevel) level()).players()) {
                boolean near = p.distanceToSqr(this) < 48 * 48;
                if (near && !bar.getPlayers().contains(p)) bar.addPlayer(p);
                else if (!near && bar.getPlayers().contains(p)) bar.removePlayer(p);
            }
        }
        // burning sends it into a frenzy: faster for 10 s, still takes the fire damage
        if (isOnFire()) rage = 200;
        AttributeInstance spd = getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            boolean has = spd.getModifier(RAGE_ID) != null;
            if (rage > 0 && !has) spd.addTransientModifier(new AttributeModifier(RAGE_ID, "tank rage", 0.35, AttributeModifier.Operation.MULTIPLY_TOTAL));
            if (rage <= 0 && has) spd.removeModifier(RAGE_ID);
        }
        if (rage > 0) {
            rage--;
            if (tickCount % 4 == 0) ((ServerLevel) level()).sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 3, getZ(), 1, 0.6, 0.3, 0.6, 0);
        }
    }

    @Override public void setCustomName(Component c) { super.setCustomName(c); bar.setName(getDisplayName()); }
    @Override public void readAdditionalSaveData(CompoundTag t) { super.readAdditionalSaveData(t); if (hasCustomName()) bar.setName(getDisplayName()); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bar.removePlayer(p); }
    @Override public void remove(RemovalReason r) { super.remove(r); bar.removeAllPlayers(); }

    // ---------------- body ----------------
    @Override public boolean causeFallDamage(float d, float m, DamageSource s) {
        if (d > 2 && level() instanceof ServerLevel) { playSound(SoundEvents.GENERIC_EXPLODE, 1f, 0.5f); ring(1.2); }
        return false;
    }
    @Override protected void playStepSound(BlockPos p, BlockState b) {
        playSound(SoundEvents.RAVAGER_STEP, 1.1f, 0.55f + random.nextFloat() * 0.1f);
        groundBurst(getX(), getZ(), 1.4, 8);
    }
    @Override protected float nextStep() { return moveDist + 1.6f; }
    @Override protected void tickDeath() {
        if (++deathTime >= DEATH_LEN && !level().isClientSide && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
        if (deathTime == 22) { playSound(SoundEvents.GENERIC_EXPLODE, 0.8f, 0.4f); if (!level().isClientSide) groundBurst(getX(), getZ(), 2.5, 30); }
    }
    @Override protected float voicePitch() { return 0.55f + random.nextFloat() * 0.15f; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.RAVAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.RAVAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override protected float getSoundVolume() { return 1.6f; }

    private int enemiesNear(double r) {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r, 2, r), e -> e != this && isEnemy(e) && (e instanceof Mob || e instanceof Player)).size();
    }

    /** chase + pick a move. Holds MOVE/LOOK so the base claw goal never runs. */
    class TankFight extends Goal {
        int repath;
        TankFight() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { LivingEntity t = getTarget(); return t != null && t.isAlive(); }
        @Override public boolean canContinueToUse() { return canUse() || busy(); }
        @Override public void start() { setAggressive(true); }
        @Override public void stop() { setAggressive(false); getNavigation().stop(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (busy()) {
                getNavigation().stop();
                if (t != null && action() != LEAP) getLookControl().setLookAt(t, 20, 30);
                if (t != null && action() != LEAP && actionTick < (action() == THROW ? THROW_REL : 10)) // track while winding up
                    setYRot(Mth.approachDegrees(getYRot(), (float) (Mth.atan2(t.getZ() - getZ(), t.getX() - getX()) * Mth.RAD_TO_DEG) - 90, 12));
                yBodyRot = getYRot();
                return;
            }
            if (t == null) return;
            getLookControl().setLookAt(t, 30, 30);
            double hd = Math.sqrt(Mth.square(t.getX() - getX()) + Mth.square(t.getZ() - getZ())), dy = t.getY() - getY();
            if (leapCd <= 0 && onGround() && dy > 1.4 && dy < 5 && hd < 7 && hd > 1.5) {
                startAction(LEAP, LEAP_LEN); leapCd = 60;
            } else if (slamCd <= 0 && onGround() && (enemiesNear(4.5) >= 3 || hd < 4 && random.nextInt(60) == 0)) {
                startAction(SLAM, SLAM_LEN); slamCd = 200;
            } else if (throwCd <= 0 && hd > 8 && hd < 30 && hasLineOfSight(t)) {
                startAction(THROW, THROW_LEN); throwCd = 140;
            } else if (punchCd <= 0 && inReach(t)) {
                if (nextHook) { startAction(HOOK, HOOK_LEN); punchCd = HOOK_LEN + 4; }
                else { startAction(HAYMAKER, HAY_LEN); punchCd = HAY_LEN + 6; }
                nextHook = !nextHook;
            } else if (--repath <= 0) {
                repath = 5;
                getNavigation().moveTo(t, rage > 0 ? 1.25 : 1.0);
            }
        }
    }
}
