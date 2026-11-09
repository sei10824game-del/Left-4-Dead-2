package com.l4d.infected.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Shield Infected: forearm slab blocks 75% of hits from the front +-70 deg (not fire/explosions); shoulder-charging shield bash. */
public class RiotInfected extends CommonInfected {
    public static final int BASH = 20;
    public RiotInfected(EntityType<? extends RiotInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(50, 3.5, 0.28).add(Attributes.KNOCKBACK_RESISTANCE, 0.5); }

    @Override protected float stumbleChance() { return 0; }
    @Override protected int chooseAttack(LivingEntity t, double d) {
        if (!inReach(t)) return NONE;
        float r = random.nextFloat();
        return r < 0.45f ? BASH : r < 0.6f ? SLAM : NONE;
    }
    @Override protected int actionLength(int id) { return id == BASH ? 22 : super.actionLength(id); }

    @Override
    protected void onActionTick(int id, int t) {
        if (id != BASH) { super.onActionTick(id, t); return; }
        Vec3 f = facing();
        if (t == 2) playSound(SoundEvents.ARMOR_EQUIP_IRON, 1f, 0.7f);
        if (t == 8) { setDeltaMovement(getDeltaMovement().add(f.scale(0.4))); hasImpulse = true; }
        if (t == 10 && hitTarget(1.3f)) shove(getTarget(), 1.1, 0.35);
        if (t == 10) playSound(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 0.8f, 1.1f);
    }

    /** the shield is strapped to the body's front: frontal hits mostly glance off. */
    @Override
    public boolean hurt(DamageSource s, float a) {
        Vec3 p = s.getSourcePosition();
        if (p != null && !s.is(DamageTypeTags.IS_FIRE) && !s.is(DamageTypeTags.IS_EXPLOSION)) {
            Vec3 d = new Vec3(p.x - getX(), 0, p.z - getZ());
            if (d.lengthSqr() > 1e-4 && d.normalize().dot(facing()) > 0.342) { // cos 70
                a *= 0.25f;
                if (!level().isClientSide) playSound(SoundEvents.SHIELD_BLOCK, 1f, 0.8f + random.nextFloat() * 0.3f);
            }
        }
        return super.hurt(s, a);
    }
}
