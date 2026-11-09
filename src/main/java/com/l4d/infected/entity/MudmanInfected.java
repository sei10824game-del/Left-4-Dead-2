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

/** Mud Crawler: lies low when idle, springs out of ambush with a mud-slung lunge. */
public class MudmanInfected extends CommonInfected {
    public MudmanInfected(EntityType<? extends MudmanInfected> t, Level l) { super(t, l); }
    public static AttributeSupplier.Builder attrs() { return baseAttrs(35, 3, 0.26); }
    @Override protected int chooseAttack(LivingEntity t, double d) {
        if (d > 2.5 && d < 8 && onGround() && hasLineOfSight(t) && random.nextFloat() < 0.6f) return LUNGE;
        return inReach(t) && random.nextFloat() < 0.2f ? SLAM : NONE;
    }
    @Override protected void onHitEffect(LivingEntity t) { t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); }
    @Override
    protected void onActionTick(int id, int t) {
        super.onActionTick(id, t);
        if (id == LUNGE && t == 7 && level() instanceof net.minecraft.server.level.ServerLevel sl)
            sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.MUD.defaultBlockState()), getX(), getY() + 0.2, getZ(), 18, 0.4, 0.1, 0.4, 0.1);
    }
    @Override protected void clientFx() { if (!isAggressive() && random.nextInt(30) == 0) level().addParticle(ParticleTypes.FALLING_DRIPSTONE_WATER, getRandomX(0.5), getY() + 1.2, getRandomZ(0.5), 0, 0, 0); }
}
