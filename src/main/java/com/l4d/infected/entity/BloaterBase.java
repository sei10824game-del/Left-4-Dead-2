package com.l4d.infected.entity;

import com.l4d.infected.L4D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Boomer-type bloater: vomits a cone of bile (also as a pain reflex), pops into a bile burst on death.
 * Anything covered in bile becomes the target of every infected within 40 blocks while the bile lasts.
 */
@Mod.EventBusSubscriber(modid = L4D.ID)
public abstract class BloaterBase extends Infected {
    public static final int VOMIT = 2, BURP = 3;
    /** vomit timeline: wind-up until SPRAY_START, sustained spray until SPRAY_END, then recovery. */
    public static final int VOMIT_LEN = 56, SPRAY_START = 16, SPRAY_END = 46, BURP_LEN = 26;
    static final int BILE_TICKS = 400;
    static final double VOMIT_RANGE = 6, POP_RADIUS = 4, LURE_RANGE = 40;
    /** bile-covered entity -> game time the bile wears off (server only). */
    private static final Map<LivingEntity, Long> BILED = new WeakHashMap<>();
    int vomitCooldown = 40;
    private boolean popped;

    protected BloaterBase(EntityType<? extends BloaterBase> t, Level l) { super(t, l); xpReward = 15; }

    public static AttributeSupplier.Builder bloaterAttrs(double speed) {
        return baseAttrs(50, 2, speed).add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void registerActionGoals() { goalSelector.addGoal(2, new VomitGoal()); }

    void vomit() {
        startAction(VOMIT, VOMIT_LEN);
        vomitCooldown = 240 + random.nextInt(100);
        getNavigation().stop();
    }

    @Override
    protected void onActionTick(int id, int t) {
        if (id == VOMIT) {
            LivingEntity tg = getTarget();
            if (tg != null) getLookControl().setLookAt(tg, 6, 30); // slow head tracking = the spray sweeps after its victim
            if (t == 0) playSound(SoundEvents.GENERIC_DRINK, 1.3f, 0.45f);                    // gulping wind-up
            if (t == SPRAY_START) { playSound(SoundEvents.HOGLIN_ATTACK, 1.4f, 0.55f); playSound(SoundEvents.GENERIC_SPLASH, 1f, 0.7f); }
            if (t >= SPRAY_START && t < SPRAY_END) spray(t);
        } else if (id == BURP && t == 9) {
            playSound(SoundEvents.PLAYER_BURP, 1.6f, 0.4f + random.nextFloat() * 0.15f);
            Vec3 m = mouth();
            ((ServerLevel) level()).sendParticles(ParticleTypes.SNEEZE, m.x, m.y, m.z, 6, 0.15, 0.1, 0.15, 0.02);
        } else super.onActionTick(id, t);
    }

    Vec3 mouth() { return getEyePosition().add(getViewVector(1f).scale(0.6)).add(0, -0.3, 0); }

    private void spray(int t) {
        ServerLevel sl = (ServerLevel) level();
        Vec3 dir = getViewVector(1f), m = mouth();
        float strength = t < SPRAY_END - 8 ? 1f : (SPRAY_END - t) / 8f; // tapers off at the end
        for (int i = 0; i < 7; i++) {
            Vec3 d = dir.add(random.nextGaussian() * 0.11, random.nextGaussian() * 0.08 - 0.06, random.nextGaussian() * 0.11).normalize();
            double v = (0.35 + random.nextFloat() * 0.35) * strength;
            sl.sendParticles(i % 3 == 0 ? ParticleTypes.ITEM_SLIME : ParticleTypes.SNEEZE, m.x, m.y, m.z, 0, d.x, d.y, d.z, v);
        }
        if (t % 6 == 0) playSound(SoundEvents.SLIME_SQUISH, 1f, 0.5f + random.nextFloat() * 0.2f);
        if (t % 2 != 0) return;
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(VOMIT_RANGE), Infected::isEnemy)) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(m);
            double dist = to.length();
            if (dist < VOMIT_RANGE * strength + 0.5 && to.normalize().dot(dir) > (dist < 1.5 ? 0.2 : 0.82) && hasLineOfSight(e)) bile(e);
        }
    }

    /** cover an entity in bile: vision loss for players, glow for everyone, and the horde comes for it. */
    public static void bile(LivingEntity e) {
        if (!isEnemy(e) || e.level().isClientSide) return;
        if (e instanceof Player) {
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70));
            e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
        }
        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, BILE_TICKS));
        if (BILED.put(e, e.level().getGameTime() + BILE_TICKS) == null) {
            e.playSound(SoundEvents.SLIME_BLOCK_FALL, 1f, 0.5f);
            lure(e);
        }
    }

    private static void lure(LivingEntity e) {
        for (Infected i : e.level().getEntitiesOfClass(Infected.class, e.getBoundingBox().inflate(LURE_RANGE, 16, LURE_RANGE),
                i -> i.getTarget() != e && !(i instanceof WitchInfected)))
            i.setTarget(e);
    }

    /** keeps the horde locked onto bile-covered victims until the bile wears off. */
    @SubscribeEvent
    public static void bileTick(TickEvent.LevelTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || ev.level.isClientSide || ev.level.getGameTime() % 10 != 0 || BILED.isEmpty()) return;
        long now = ev.level.getGameTime();
        BILED.entrySet().removeIf(en -> {
            LivingEntity e = en.getKey();
            if (e.level() != ev.level) return e.isRemoved();
            boolean done = en.getValue() < now || !isEnemy(e) || e.isRemoved();
            if (!done) lure(e);
            return done;
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (vomitCooldown > 0) vomitCooldown--;
        if (!busy() && getTarget() == null && isAlive() && random.nextInt(320) == 0) startAction(BURP, BURP_LEN);
    }

    /** pain reflex: a hit may make it retch on the attacker. */
    @Override
    public boolean hurt(DamageSource s, float a) {
        boolean ok = super.hurt(s, a);
        if (ok && !level().isClientSide && isAlive() && action() != VOMIT && vomitCooldown < 120 && random.nextFloat() < 0.35f
                && s.getEntity() instanceof LivingEntity att && isEnemy(att) && distanceToSqr(att) < VOMIT_RANGE * VOMIT_RANGE) {
            setTarget(att);
            vomit();
        }
        return ok;
    }

    @Override
    public void die(DamageSource s) {
        super.die(s);
        if (dead && !popped && level() instanceof ServerLevel sl) { popped = true; pop(sl); }
    }

    /** bursts: bile splash in POP_RADIUS, shove, no block damage. */
    private void pop(ServerLevel sl) {
        Vec3 c = position().add(0, getBbHeight() * 0.55, 0);
        sl.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z, 3, 0.5, 0.4, 0.5, 0);
        sl.sendParticles(ParticleTypes.SNEEZE, c.x, c.y, c.z, 140, 0.4, 0.4, 0.4, 0.45);
        sl.sendParticles(ParticleTypes.ITEM_SLIME, c.x, c.y, c.z, 90, 0.9, 0.6, 0.9, 0.35);
        sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, c.x, c.y, c.z, 12, 0.6, 0.4, 0.6, 0.2);
        playSound(SoundEvents.GENERIC_EXPLODE, 1.3f, 1.35f);
        playSound(SoundEvents.SLIME_DEATH, 1.6f, 0.45f);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(POP_RADIUS), Infected::isEnemy)) {
            if (distanceToSqr(e) > POP_RADIUS * POP_RADIUS) continue;
            bile(e);
            Vec3 d = e.position().subtract(position()).normalize();
            e.knockback(0.7, -d.x, -d.z);
            e.hurtMarked = true;
        }
    }

    /** popped bloaters do not lie around: gone after two ticks (the model hides immediately). */
    @Override
    protected void tickDeath() {
        if (++deathTime >= 2 && !level().isClientSide && !isRemoved()) { level().broadcastEntityEvent(this, (byte) 60); remove(RemovalReason.KILLED); }
    }

    @Override protected double moveSpeedMul() { return 1.0; }
    @Override protected int clawLength() { return 18; }
    @Override protected float voicePitch() { return 0.55f + random.nextFloat() * 0.12f; }
    @Override protected void playStepSound(BlockPos p, BlockState b) { playSound(SoundEvents.SLIME_SQUISH, 0.25f, 0.5f); playSound(SoundEvents.ZOMBIE_STEP, 0.2f, 0.6f); }

    class VomitGoal extends Goal {
        VomitGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            LivingEntity t = getTarget();
            return t != null && isEnemy(t) && vomitCooldown <= 0 && !busy() && distanceToSqr(t) < 4.5 * 4.5 && hasLineOfSight(t);
        }
        @Override public boolean canContinueToUse() { return action() == VOMIT; }
        @Override public void start() { vomit(); }
        @Override public void tick() { getNavigation().stop(); }
    }
}
