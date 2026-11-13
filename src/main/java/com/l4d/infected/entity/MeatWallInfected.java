package com.l4d.infected.entity;

import com.l4d.infected.L4D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Meat Wall: colossal fused-flesh boss, tougher and taller than the Tank.
 * Melee: SMASH (ring), CRUSH (ram), GRAB (seize, squeeze, hurl). Ranged: CRACK3 / CRACK5 (travelling ground fissures in 3 / 5
 * directions) and PULL (reaches into its own body, drags out an infected and hurls it, three times). Explodes on death.
 */
public class MeatWallInfected extends Infected {
    public static final int SMASH = 2, CRUSH = 3, CRACK3 = 4, CRACK5 = 5, PULL = 6, GRAB = 7;
    public static final int CRACK3_LEN = 40, CRACK3_HIT = 22, CRACK5_LEN = 48, CRACK5_HIT = 28, PULL_LEN = 70, GRAB_LEN = 76;
    private static final double CRACK_SPEED = 1.3, CRACK_RANGE = 30;

    private int rangedCd = 100, victimId = -1, crackT;
    private final List<Vec3> crackDirs = new ArrayList<>();
    private Vec3 crackFrom = Vec3.ZERO;
    private final Set<Integer> cracked = new HashSet<>();

    public MeatWallInfected(EntityType<? extends MeatWallInfected> t, Level l) { super(t, l); setMaxUpStep(1.5f); xpReward = 200; }
    public static AttributeSupplier.Builder attrs() {
        return baseAttrs(2500, 28, 0.16).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ARMOR, 12).add(Attributes.FOLLOW_RANGE, 48);
    }
    @Override public double reach() { return 5.5; }
    @Override protected float voicePitch() { return 0.4f + random.nextFloat() * 0.1f; }
    @Override public boolean canBeLeashed(net.minecraft.world.entity.player.Player p) { return false; }

    @Override
    protected void registerActionGoals() {
        goalSelector.addGoal(2, new Goal() {
            int id;
            @Override public boolean canUse() {
                LivingEntity t = getTarget();
                if (t == null || busy() || !t.isAlive()) return false;
                double d = distanceTo(t);
                if (inReach(t) && clawCooldown <= 0) { float r = random.nextFloat(); id = r < 0.35f ? SMASH : r < 0.6f ? CRUSH : GRAB; return true; }
                if (rangedCd <= 0 && d > 6 && d < 30 && hasLineOfSight(t)) { float r = random.nextFloat(); id = r < 0.35f ? CRACK3 : r < 0.7f ? CRACK5 : PULL; return true; }
                return false;
            }
            @Override public void start() {
                getNavigation().stop();
                startAction(id, len(id));
                if (id >= CRACK3 && id <= PULL) rangedCd = 140 + random.nextInt(80); else clawCooldown = 50;
            }
        });
    }

    private static int len(int id) {
        return switch (id) { case SMASH -> 30; case CRUSH -> 24; case CRACK3 -> CRACK3_LEN; case CRACK5 -> CRACK5_LEN; case PULL -> PULL_LEN; default -> GRAB_LEN; };
    }

    @Override
    public void tick() {
        if (rangedCd > 0) rangedCd--;
        super.tick();
        if (!level().isClientSide && !crackDirs.isEmpty()) fissures();
    }

    @Override
    protected void onActionTick(int id, int t) {
        LivingEntity tg = getTarget();
        if (tg != null && id != CRUSH) getLookControl().setLookAt(tg, 20, 20);
        if (id >= CRACK3) getNavigation().stop();
        switch (id) {
            case SMASH -> smash(t);
            case CRUSH -> crush(t);
            case CRACK3 -> { if (t == 4) playSound(SoundEvents.RAVAGER_ROAR, 2f, 0.4f); if (t == CRACK3_HIT) crack(tg, 0, 35, -35); }
            case CRACK5 -> { if (t == 4) playSound(SoundEvents.RAVAGER_ROAR, 2f, 0.35f); if (t == CRACK5_HIT) crack(tg, 0, 25, -25, 50, -50); }
            case PULL -> pull(t, tg);
            case GRAB -> grab(t, tg);
            default -> super.onActionTick(id, t);
        }
    }

    // ---------- melee ----------
    private void smash(int t) {
        if (t == 4) playSound(SoundEvents.RAVAGER_ROAR, 2f, 0.5f);
        if (t != 17) return;
        playSound(SoundEvents.GENERIC_EXPLODE, 1.5f, 0.5f);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6, 1, 6), Infected::isEnemy)) {
            e.invulnerableTime = 0;
            if (e.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8f)) launch(e, 1.1, 0.65);
        }
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.1, getZ(), 60, 3.5, 0.05, 3.5, 0.08);
    }

    private void crush(int t) {
        if (t == 6) { Vec3 f = Vec3.directionFromRotation(0, yBodyRot); setDeltaMovement(f.x * 0.6, 0.1, f.z * 0.6); hasImpulse = true; }
        if (t >= 8 && t <= 14 && t % 3 == 2)
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.6), Infected::isEnemy)) {
                e.invulnerableTime = 0;
                if (e.hurt(damageSources().mobAttack(this), 12f)) { launch(e, 1.5, 0.35); playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.5f, 0.4f); }
            }
    }

    private void grab(int t, LivingEntity tg) {
        if (t == 8) {
            if (tg != null && inReach(tg) && Grip.grabbable(tg)) { victimId = tg.getId(); playSound(SoundEvents.RAVAGER_ATTACK, 2f, 0.5f); }
            else { victimId = -1; if (tg != null && hitTarget(1.2f)) launch(tg, 1.2, 0.4); endAction(); return; }
        }
        if (t < 8) return;
        LivingEntity v = Grip.resolve(this, victimId);
        if (v == null || !v.isAlive()) { endAction(); return; }
        Vec3 f = Vec3.directionFromRotation(0, yBodyRot);
        if (t < 58) {
            Grip.place(v, position().add(f.x * 2.6, getBbHeight() * (t < 14 ? 0.35 + 0.045 * (t - 8) : 0.62), f.z * 2.6));
            if (t % 12 == 0) { v.invulnerableTime = 0; v.hurt(damageSources().mobAttack(this), 5f); playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.5f, 0.4f); }
        } else if (t == 58) {                                           // overhead hurl
            v.invulnerableTime = 0; v.hurt(damageSources().mobAttack(this), 10f);
            Grip.push(v, new Vec3(f.x * 1.5, 0.7, f.z * 1.5));
            playSound(SoundEvents.RAVAGER_ROAR, 2f, 0.6f); victimId = -1;
        }
    }

    // ---------- ranged: ground fissures ----------
    /** yaw offsets in degrees from the line towards the target. */
    private void crack(LivingEntity tg, double... offsets) {
        Vec3 d = tg != null ? new Vec3(tg.getX() - getX(), 0, tg.getZ() - getZ()) : Vec3.directionFromRotation(0, yBodyRot);
        double base = Math.atan2(d.z, d.x);
        crackDirs.clear(); cracked.clear();
        for (double o : offsets) crackDirs.add(new Vec3(Math.cos(base + Math.toRadians(o)), 0, Math.sin(base + Math.toRadians(o))));
        crackFrom = position(); crackT = 0;
        playSound(SoundEvents.GENERIC_EXPLODE, 2f, 0.4f);
    }

    private void fissures() {
        crackT++;
        double dist = crackT * CRACK_SPEED;
        if (dist > CRACK_RANGE) { crackDirs.clear(); return; }
        ServerLevel sl = (ServerLevel) level();
        for (Vec3 d : crackDirs) {
            Vec3 p = crackFrom.add(d.scale(dist));
            BlockPos g = sl.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(p)).below();
            if (Math.abs(g.getY() - crackFrom.y) > 4) continue;               // terrain drops away / cliff: the crack fizzles there
            var st = sl.getBlockState(g).isAir() ? Blocks.STONE.defaultBlockState() : sl.getBlockState(g);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st), p.x, g.getY() + 1.1, p.z, 14, 0.6, 0.15, 0.6, 0.35);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, g.getY() + 1.1, p.z, 2, 0.4, 0.1, 0.4, 0.02);
            if (crackT % 2 == 0) sl.playSound(null, p.x, p.y, p.z, SoundEvents.STONE_BREAK, net.minecraft.sounds.SoundSource.HOSTILE, 1.5f, 0.5f);
            for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(p.x - 1.6, g.getY() - 1, p.z - 1.6, p.x + 1.6, g.getY() + 4, p.z + 1.6), Infected::isEnemy))
                if (cracked.add(e.getId())) {
                    e.invulnerableTime = 0;
                    if (e.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.55f)) Grip.push(e, new Vec3(d.x * 0.3, 0.9, d.z * 0.3));
                }
        }
    }

    // ---------- ranged: pull an infected out of its own body and throw it ----------
    private static final List<java.util.function.Supplier<? extends EntityType<? extends Infected>>> POOL =
            List.of(L4D.COMMON, L4D.COMMON, L4D.COMMON, L4D.CEDA, L4D.MUDMAN, L4D.ROADCREW, L4D.FALLEN, L4D.RIOT, L4D.CLOWN, L4D.CRAWLER);

    private void pull(int t, LivingEntity tg) {
        if (t == 6) playSound(SoundEvents.SLIME_SQUISH, 2f, 0.4f);
        if (t < 10 || t > 58 || (t - 10) % 16 != 14 || tg == null || !(level() instanceof ServerLevel sl)) return;
        if (sl.getEntitiesOfClass(Infected.class, getBoundingBox().inflate(40)).size() > 30) return;
        Infected z = POOL.get(random.nextInt(POOL.size())).get().create(sl);
        if (z == null) return;
        Vec3 f = Vec3.directionFromRotation(0, yBodyRot), p = position().add(f.x * 2.2, getBbHeight() * 0.55, f.z * 2.2);
        z.moveTo(p.x, p.y, p.z, getYRot(), 0);
        z.finalizeSpawn(sl, sl.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        Vec3 d = tg.position().subtract(p); double h = Math.max(2, Math.sqrt(d.x * d.x + d.z * d.z)), time = Math.max(8, h / 0.9);
        z.setDeltaMovement(d.x / time, Mth.clamp(d.y / time + 0.04 * time, 0.3, 1.1), d.z / time);
        z.setTarget(tg); z.hasImpulse = true;
        sl.addFreshEntity(z);
        playSound(SoundEvents.SLIME_BLOCK_BREAK, 2.5f, 0.5f);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), p.x, p.y, p.z, 20, 0.5, 0.5, 0.5, 0.2);
    }

    // ---------- death ----------
    private void launch(LivingEntity e, double h, double up) {
        Vec3 d = e.position().subtract(position()).multiply(1, 0, 1).normalize();
        Grip.push(e, new Vec3(d.x * h, up, d.z * h));
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (level().isClientSide) return;
        level().explode(this, getX(), getY() + getBbHeight() * 0.4, getZ(), 7f, Level.ExplosionInteraction.NONE);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 3, getZ(), 3, 1.5, 1.5, 1.5, 0);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()), getX(), getY() + 3, getZ(), 150, 2, 2.5, 2, 0.4);
        }
    }
}
