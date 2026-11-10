package com.l4d.infected.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/** Crawler: legless, hugs the ground; leaps at ankles and drags victims down (slowness + weakness). */
public class CrawlerInfected extends CommonInfected {
    public CrawlerInfected(EntityType<? extends CrawlerInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(25, 3, 0.27); }
    @Override protected float stumbleChance() { return 0; }
    @Override protected int chooseAttack(LivingEntity t, double d) {
        return d > 1.5 && d < 5 && onGround() && hasLineOfSight(t) && random.nextFloat() < 0.5f ? LUNGE : NONE;
    }
    @Override protected void onHitEffect(LivingEntity t) {
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
        t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        playSound(SoundEvents.SPIDER_AMBIENT, 0.6f, 0.6f);
    }
    @Override protected float voicePitch() { return 1.4f + random.nextFloat() * 0.2f; }
}
