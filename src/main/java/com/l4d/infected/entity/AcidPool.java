package com.l4d.infected.entity;

import com.l4d.infected.L4D;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** Burning acid puddle: hurts + slows every non-infected standing in it every half second, then dries up. */
public class AcidPool extends Entity {
    static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(AcidPool.class, EntityDataSerializers.FLOAT);
    static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(AcidPool.class, EntityDataSerializers.INT);
    @Nullable private Entity owner; // damage credit only, not saved

    public AcidPool(EntityType<? extends AcidPool> t, Level l) { super(t, l); noPhysics = true; }
    public AcidPool(Level l, double x, double y, double z, float radius, int life, @Nullable Entity owner) {
        this(L4D.ACID_POOL.get(), l);
        setPos(x, y, z);
        entityData.set(RADIUS, radius);
        entityData.set(LIFE, life);
        this.owner = owner;
    }

    public float radius() { return entityData.get(RADIUS); }
    public int life() { return entityData.get(LIFE); }

    @Override protected void defineSynchedData() { entityData.define(RADIUS, 2.5f); entityData.define(LIFE, 140); }
    @Override protected void readAdditionalSaveData(CompoundTag t) { entityData.set(RADIUS, t.getFloat("Radius")); entityData.set(LIFE, t.getInt("Life")); }
    @Override protected void addAdditionalSaveData(CompoundTag t) { t.putFloat("Radius", radius()); t.putInt("Life", life() - tickCount); }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().inflate(radius()); }

    @Override
    public void tick() {
        super.tick();
        float r = radius();
        if (level().isClientSide) {
            for (int i = 0; i < r * 1.5f; i++) {
                double a = random.nextDouble() * Math.PI * 2, d = Math.sqrt(random.nextDouble()) * r;
                double x = getX() + Math.cos(a) * d, z = getZ() + Math.sin(a) * d;
                level().addParticle(i == 0 && random.nextInt(4) == 0 ? ParticleTypes.SMOKE : AcidSpit.ACID_DUST, x, getY() + 0.05, z, 0, 0.03, 0);
                if (random.nextInt(10) == 0) level().addParticle(ParticleTypes.BUBBLE_POP, x, getY() + 0.05, z, 0, 0.02, 0);
            }
            return;
        }
        if (tickCount >= life()) { discard(); return; }
        if (tickCount % 16 == 1) playSound(SoundEvents.FIRE_EXTINGUISH, 0.35f, 1.5f + random.nextFloat() * 0.3f);
        if (tickCount % 10 != 0) return;
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class,
                new AABB(getX() - r, getY() - 0.5, getZ() - r, getX() + r, getY() + 0.8, getZ() + r), Infected::isEnemy)) {
            double dx = e.getX() - getX(), dz = e.getZ() - getZ();
            if (dx * dx + dz * dz > r * r || !e.attackable()) continue;
            if (e.hurt(damageSources().indirectMagic(this, owner), 2f)) e.playSound(SoundEvents.GENERIC_BURN, 0.4f, 1.6f);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1));
        }
    }
}
