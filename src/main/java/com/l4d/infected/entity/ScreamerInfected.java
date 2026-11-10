package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Screamer: harmless alone. Spots a victim, runs to a safe distance, then wails and calls every infected nearby. */
public class ScreamerInfected extends CommonInfected {
    public static final int SCREAM = 80;
    private int screamCd = 0;
    public ScreamerInfected(EntityType<? extends ScreamerInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(40, 1, 0.36); }
    @Override protected int chooseAttack(LivingEntity t, double d) { return NONE; }
    @Override protected float stumbleChance() { return 0; }
    @Override protected int actionLength(int id) { return id == SCREAM ? 50 : super.actionLength(id); }
    @Override protected boolean alertsHorde() { return false; }

    @Override
    protected void registerActionGoals() {
        super.registerActionGoals();
        goalSelector.addGoal(2, new Goal() {                 // keep away from the victim until the wail is ready
            { setFlags(java.util.EnumSet.of(Flag.MOVE)); }
            int re;
            @Override public boolean canUse() { LivingEntity t = getTarget(); return t != null && !busy() && screamCd == 0 && distanceTo(t) < 16; }
            @Override public boolean canContinueToUse() { return canUse(); }
            @Override public void tick() {
                LivingEntity t = getTarget();
                if (t == null || --re > 0) return;
                re = 8;
                Vec3 p = DefaultRandomPos.getPosAway(ScreamerInfected.this, 14, 6, t.position());
                if (p != null) getNavigation().moveTo(p.x, p.y, p.z, 1.3);
            }
        });
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (screamCd > 0) screamCd--;
        LivingEntity t = getTarget();
        if (t != null && !busy() && screamCd == 0 && distanceTo(t) >= 14 && hasLineOfSight(t)) { getNavigation().stop(); startAction(SCREAM, 50); }
    }

    @Override
    protected void onActionTick(int id, int t) {
        if (id != SCREAM) { super.onActionTick(id, t); return; }
        getNavigation().stop();
        LivingEntity tg = getTarget();
        if (tg != null) getLookControl().setLookAt(tg, 60, 60);
        if (t == 14) {
            playSound(SoundEvents.ENDERMAN_SCREAM, 4f, 0.6f);
            playSound(SoundEvents.GHAST_SCREAM, 4f, 0.5f);
            if (tg != null) for (Infected i : level().getEntitiesOfClass(Infected.class, getBoundingBox().inflate(60, 30, 60), i -> i != this && !(i instanceof WitchInfected) && i.getTarget() == null)) i.setTarget(tg);
        }
        if (t >= 14 && t < 40 && t % 4 == 0 && level() instanceof net.minecraft.server.level.ServerLevel sl) {
            Vec3 f = facing(); sl.sendParticles(ParticleTypes.SONIC_BOOM, getX() + f.x * 0.6, getEyeY(), getZ() + f.z * 0.6, 1, 0, 0, 0, 0);
        }
        if (t == actionLen() - 1) screamCd = 600;
    }
}
