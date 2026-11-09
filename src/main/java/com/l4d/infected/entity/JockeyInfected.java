package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Small cackling rider: crouch -> leap -> ride the victim's head, steering it and clawing. Ids match JockeyModel. */
public class JockeyInfected extends Infected {
    public static final int CACKLE = 2, LEAP = 3, RIDE = 4, STUN = 5, YANK = 10;
    private int leapCd = 40, dir = 1;
    private Vec3 leapVec = Vec3.ZERO;

    public JockeyInfected(EntityType<? extends JockeyInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(100, 3, 0.38); }

    @Override protected void registerActionGoals() { goalSelector.addGoal(2, new LeapGoal()); }
    @Override protected float voicePitch() { return 1.5f + random.nextFloat() * 0.3f; }
    @Override public double getMyRidingOffset() { return 0.3; }
    @Override public boolean canBeLeashed(net.minecraft.world.entity.player.Player p) { return false; }

    class LeapGoal extends Goal {
        @Override public boolean canUse() {
            LivingEntity t = getTarget();
            if (t == null || busy() || leapCd > 0 || !onGround() || isPassenger() || !Grip.grabbable(t)) return false;
            double d = distanceTo(t);
            return d >= 2.5 && d <= 10 && hasLineOfSight(t);
        }
        @Override public void start() {
            getNavigation().stop();
            startAction(CACKLE, 14);
            playSound(SoundEvents.WITCH_CELEBRATE, 1.2f, 1.8f);
        }
    }

    @Override
    protected void onActionTick(int id, int t) {
        LivingEntity tg = getTarget();
        switch (id) {
            case CACKLE -> {
                if (tg != null) getLookControl().setLookAt(tg, 60, 60);
                setDeltaMovement(0, getDeltaMovement().y, 0);
                if (t == actionLen() - 1) launch(tg);
            }
            case LEAP -> leapTick(t);
            case RIDE -> rideTick(t);
            case STUN -> { if (t == 0) playSound(SoundEvents.WITCH_HURT, 1f, 1.9f); }
            default -> super.onActionTick(id, t);
        }
    }

    private void launch(LivingEntity t) {
        if (t == null || !t.isAlive()) { endAction(); return; }
        Vec3 d = t.position().add(0, t.getBbHeight() * 0.75, 0).subtract(position());
        double h = Math.max(1, Math.sqrt(d.x * d.x + d.z * d.z)), hs = 0.95, time = h / hs;
        leapVec = new Vec3(d.x / h * hs, 0, d.z / h * hs);
        setDeltaMovement(leapVec.x, Mth.clamp(d.y / time + 0.04 * time, 0.3, 0.9), leapVec.z);
        setYRot((float) (Mth.atan2(d.z, d.x) * 180 / Math.PI) - 90);
        startAction(LEAP, 0);
        leapCd = 60 + random.nextInt(40);
    }

    private void leapTick(int t) {
        setDeltaMovement(leapVec.x, getDeltaMovement().y, leapVec.z);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.4), Infected::isEnemy)) {
            if (!Grip.grabbable(e) || e.getPassengers().size() > 0) continue;
            if (startRiding(e, true)) {
                setTarget(e);
                startAction(RIDE, 0);
                playSound(SoundEvents.WITCH_CELEBRATE, 1f, 1.6f);
                return;
            }
        }
        if (t > 3 && (onGround() || horizontalCollision)) startAction(STUN, 16);
        else if (t > 50) endAction();
    }

    private void rideTick(int t) {
        if (!(getVehicle() instanceof LivingEntity v) || !v.isAlive()) { dismount(); return; }
        setYRot(v.getYRot());
        yBodyRot = v.getYRot();
        if (t % 24 == 0) dir = -dir;                                     // zig-zag: swap steering side
        if (t % 2 == 0) {
            float yaw = (v.getYRot() + 70 * dir) * Mth.DEG_TO_RAD;
            Vec3 side = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)).scale(0.14);
            Vec3 m = v.getDeltaMovement();
            v.setDeltaMovement(m.x + side.x, m.y, m.z + side.z);
            v.hurtMarked = true;
            if (v instanceof Mob mob) mob.getNavigation().stop();
        }
        if (t % YANK == YANK - 3) {                                      // hand yank lands on the model's grip frame
            v.invulnerableTime = 0;
            v.hurt(damageSources().mobAttack(this), 1.5f);
            playSound(SoundEvents.PLAYER_ATTACK_CRIT, 0.8f, 1.7f);
            if (level() instanceof ServerLevel sl)
                sl.sendParticles(ParticleTypes.CRIT, v.getX(), v.getEyeY(), v.getZ(), 4, 0.2, 0.15, 0.2, 0.2);
        }
    }

    private void dismount() {
        stopRiding();
        setDeltaMovement(random.nextGaussian() * 0.3, 0.45, random.nextGaussian() * 0.3);
        startAction(STUN, 24);
    }

    @Override
    public boolean hurt(DamageSource s, float a) {
        boolean ok = super.hurt(s, a);
        if (ok && action() == RIDE && a >= 5) dismount();               // heavy hit knocks it off
        return ok;
    }

    @Override
    public void tick() {
        if (leapCd > 0) leapCd--;
        super.tick();
        LivingEntity t = getTarget();
        if (!level().isClientSide && !busy() && t != null && onGround() && tickCount % 12 == 0 && distanceTo(t) > 4) {
            Vec3 f = t.position().subtract(position()).normalize();     // erratic sidestep hop while closing in
            double s = random.nextBoolean() ? 0.35 : -0.35;
            setDeltaMovement(f.x * 0.2 - f.z * s, 0.38, f.z * 0.2 + f.x * s);
        }
    }
}
