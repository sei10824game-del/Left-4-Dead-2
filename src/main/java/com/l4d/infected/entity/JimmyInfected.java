package com.l4d.infected.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Gridiron Brute: tough, fast, hard hitting; sprint-tackles from range with a lowered-shoulder charge. */
public class JimmyInfected extends CommonInfected {
    public static final int TACKLE = 20;
    private boolean tackled;
    public JimmyInfected(EntityType<? extends JimmyInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() {
        return baseAttrs(80, 6, 0.33).add(Attributes.KNOCKBACK_RESISTANCE, 0.8).add(Attributes.ATTACK_KNOCKBACK, 0.9);
    }

    @Override protected float stumbleChance() { return 1 / 900f; }
    @Override protected int chooseAttack(LivingEntity t, double d) {
        if (d > 3 && d < 9 && onGround() && hasLineOfSight(t) && random.nextFloat() < 0.55f) return TACKLE;
        return inReach(t) && random.nextFloat() < 0.25f ? SLAM : NONE;
    }
    @Override protected int actionLength(int id) { return id == TACKLE ? 26 : super.actionLength(id); }

    @Override
    protected void onActionTick(int id, int t) {
        if (id != TACKLE) { super.onActionTick(id, t); return; }
        LivingEntity tg = getTarget();
        if (t == 0) { tackled = false; playSound(SoundEvents.RAVAGER_ROAR, 0.6f, 1.4f); }
        if (t >= 8 && t < 17 && !tackled) {               // the charge: drive along the facing (look control steers a little)
            Vec3 f = facing();
            setDeltaMovement(f.x * 0.7, getDeltaMovement().y, f.z * 0.7);
            if (t % 3 == 0) playSound(SoundEvents.ZOMBIE_STEP, 0.6f, 0.8f);
            if (tg != null && inReach(tg) && distanceToSqr(tg) < 5 && (tackled = hitTarget(1.4f))) {
                shove(tg, 1.6, 0.55);
                playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1f, 0.6f);
                setDeltaMovement(getDeltaMovement().multiply(0.2, 1, 0.2));
            }
        }
        if (t == 17 && !tackled) setDeltaMovement(getDeltaMovement().multiply(0.3, 1, 0.3));
    }
}
