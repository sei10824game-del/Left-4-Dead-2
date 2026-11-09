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

/** Hazmat Infected: fireproof, leaks toxic fumes; exhales a poison cloud in a crafted heave. */
public class CedaInfected extends CommonInfected {
    public static final int FUME = 30;
    public CedaInfected(EntityType<? extends CedaInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(55, 3, 0.29); }
    @Override public boolean fireImmune() { return true; }
    @Override protected int chooseAttack(LivingEntity t, double d) { return d < 4.5 && hasLineOfSight(t) && random.nextFloat() < 0.4f ? FUME : super.chooseAttack(t, d); }
    @Override protected int actionLength(int id) { return id == FUME ? 24 : super.actionLength(id); }
    @Override protected void clientFx() { if (random.nextInt(4) == 0) level().addParticle(ParticleTypes.SMOKE, getRandomX(0.4), getY() + 1.5, getRandomZ(0.4), 0, 0.03, 0); }

    @Override
    protected void onActionTick(int id, int t) {
        if (id != FUME) { super.onActionTick(id, t); return; }
        if (t == 4) playSound(SoundEvents.PUFFER_FISH_BLOW_UP, 1f, 0.5f);
        if (t >= 12 && t < 18) {                       // sustained exhale in a forward cone
            Vec3 f = facing();
            if (level() instanceof net.minecraft.server.level.ServerLevel sl)
                sl.sendParticles(ParticleTypes.SNEEZE, getX() + f.x, getEyeY() - 0.2, getZ() + f.z, 3, 0.25, 0.15, 0.25, 0.08);
            if (t == 12) { playSound(SoundEvents.CAMPFIRE_CRACKLE, 1f, 0.5f); hitTarget(0.8f); }
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5, 1, 3.5), Infected::isEnemy))
                if (new Vec3(e.getX() - getX(), 0, e.getZ() - getZ()).normalize().dot(f) > 0.5) e.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        }
    }
}
