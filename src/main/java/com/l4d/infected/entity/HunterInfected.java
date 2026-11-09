package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Stalking pouncer: coil -> ballistic leap -> pin and rake. Action ids match HunterModel. */
public class HunterInfected extends Infected {
    public static final int COIL = 2, LEAP = 3, PIN = 4, STUN = 5, RAKE = 14, RAKE_HIT = 6;
    private int pounceCd = 60, victimId = -1;
    private Vec3 leapVec = Vec3.ZERO, leapFrom = Vec3.ZERO;

    public HunterInfected(EntityType<? extends HunterInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(40, 5, 0.36); }

    @Override protected void registerActionGoals() { goalSelector.addGoal(2, new PounceGoal()); }
    @Override protected float voicePitch() { return 0.7f + random.nextFloat() * 0.15f; }

    class PounceGoal extends Goal {
        @Override public boolean canUse() {
            LivingEntity t = getTarget();
            if (t == null || busy() || pounceCd > 0 || !onGround() || !t.isAlive()) return false;
            double d = distanceTo(t);
            return d >= 5 && d <= 20 && hasLineOfSight(t);
        }
        @Override public void start() {
            getNavigation().stop();
            startAction(COIL, 12);
            playSound(SoundEvents.WOLF_GROWL, 1.3f, 0.6f);
        }
    }

    @Override
    protected void onActionTick(int id, int t) {
        LivingEntity tg = getTarget();
        switch (id) {
            case COIL -> {
                if (tg != null) { getLookControl().setLookAt(tg, 60, 60); setDeltaMovement(0, getDeltaMovement().y, 0); }
                if (t == actionLen() - 1) launch(tg);
            }
            case LEAP -> leapTick(t);
            case PIN -> pinTick(t);
            case STUN -> {
                if (t == 0) playSound(SoundEvents.WOLF_WHINE, 1f, 0.6f);
            }
            default -> super.onActionTick(id, t);
        }
    }

    private void launch(LivingEntity t) {
        if (t == null || !t.isAlive()) { endAction(); return; }
        Vec3 d = t.position().add(0, t.getBbHeight() * 0.5, 0).subtract(position());
        double h = Math.max(1, Math.sqrt(d.x * d.x + d.z * d.z)), hs = 1.15, time = h / hs;
        double vy = Mth.clamp(d.y / time + 0.04 * time, 0.25, 1.0);
        leapVec = new Vec3(d.x / h * hs, 0, d.z / h * hs);
        leapFrom = position();
        setYRot((float) (Mth.atan2(d.z, d.x) * 180 / Math.PI) - 90);
        setDeltaMovement(leapVec.x, vy, leapVec.z);
        startAction(LEAP, 0);
        playSound(SoundEvents.RAVAGER_ROAR, 1f, 1.6f);
        pounceCd = 80 + random.nextInt(40);
    }

    private void leapTick(int t) {
        Vec3 m = getDeltaMovement();
        setDeltaMovement(leapVec.x, m.y, leapVec.z);                     // hold the arc: no air drag
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.35), Infected::isEnemy)) {
            if (!Grip.grabbable(e)) continue;
            float extra = (float) Math.min(6, position().distanceTo(leapFrom) / 3);
            e.invulnerableTime = 0;
            e.hurt(damageSources().mobAttack(this), (float) getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) + extra);
            victimId = e.getId();
            setTarget(e);
            startAction(PIN, 0);
            return;
        }
        if (t > 3 && (onGround() || horizontalCollision)) { startAction(STUN, 24); setDeltaMovement(0, 0, 0); } // missed: stagger
        else if (t > 60) endAction();
    }

    private void pinTick(int t) {
        LivingEntity v = Grip.resolve(this, victimId);
        if (v == null || !v.isAlive()) { release(); return; }
        Grip.subdue(v, 127);
        v.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, 200, false, false));
        v.setDeltaMovement(0, Math.min(0, v.getDeltaMovement().y), 0);
        v.hurtMarked = true;
        Vec3 p = v.position();
        moveTo(p.x, p.y + v.getBbHeight() * 0.35, p.z, getYRot(), getXRot());
        setDeltaMovement(Vec3.ZERO);
        getLookControl().setLookAt(v, 90, 90);
        if (t % RAKE == RAKE_HIT) {
            v.invulnerableTime = 0;
            v.hurt(damageSources().mobAttack(this), 2.5f);
            playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 0.7f);
            level().broadcastEntityEvent(this, (byte) 2);
            if (level() instanceof net.minecraft.server.level.ServerLevel sl)
                sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, v.getX(), v.getY() + v.getBbHeight() * 0.6, v.getZ(), 3, 0.2, 0.2, 0.2, 0.1);
        }
    }

    private void release() {
        victimId = -1;
        startAction(STUN, 30);
    }

    @Override
    public boolean hurt(DamageSource s, float a) {
        boolean ok = super.hurt(s, a);
        if (ok && action() == PIN && a >= 5) { // heavy hit knocks it off
            Vec3 k = s.getEntity() != null ? position().subtract(s.getEntity().position()).normalize() : Vec3.ZERO;
            setDeltaMovement(k.x * 0.8, 0.35, k.z * 0.8);
            release();
        }
        return ok;
    }

    @Override
    public void tick() {
        if (pounceCd > 0) pounceCd--;
        super.tick();
    }
}
