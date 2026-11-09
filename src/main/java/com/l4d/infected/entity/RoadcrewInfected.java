package com.l4d.infected.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Worker Infected: ear defenders (deaf to noise) and a heavy tool; wide two-handed sweep hits everything in front. */
public class RoadcrewInfected extends CommonInfected {
    public static final int SWEEP = 50;
    public RoadcrewInfected(EntityType<? extends RoadcrewInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(45, 4, 0.29).add(Attributes.ATTACK_KNOCKBACK, 0.8); }
    @Override protected boolean deaf() { return true; }
    @Override protected int chooseAttack(LivingEntity t, double d) { return d < 3.2 && random.nextFloat() < 0.5f ? SWEEP : super.chooseAttack(t, d); }
    @Override protected int actionLength(int id) { return id == SWEEP ? 24 : super.actionLength(id); }
    @Override
    protected void onActionTick(int id, int t) {
        if (id != SWEEP) { super.onActionTick(id, t); return; }
        if (t == 3) playSound(SoundEvents.ANVIL_PLACE, 0.5f, 1.6f);
        if (t == 11) {
            playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f, 0.6f);
            Vec3 f = facing();
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2.4, 0.6, 2.4), Infected::isEnemy))
                if (new Vec3(e.getX() - getX(), 0, e.getZ() - getZ()).normalize().dot(f) > -0.2 || distanceToSqr(e) < 2) {
                    e.invulnerableTime = 0;
                    if (e.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f)) shove(e, 0.9, 0.3);
                }
        }
    }
}
