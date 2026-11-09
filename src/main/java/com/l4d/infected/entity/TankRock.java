package com.l4d.infected.entity;

import com.l4d.infected.L4D;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Slab of ground hurled by the tank: heavy arc, crushing direct hit, a smaller splash around impact. Never breaks blocks. */
public class TankRock extends ThrowableProjectile {
    public static final float SPEED = 1.7f, GRAVITY = 0.05f, DAMAGE = 15, SPLASH = 6;

    public TankRock(EntityType<? extends TankRock> t, Level l) { super(t, l); }
    public TankRock(Level l, LivingEntity owner, double x, double y, double z) {
        super(L4D.TANK_ROCK.get(), x, y, z, l);
        setOwner(owner);
    }

    @Override protected void defineSynchedData() {}
    @Override protected float getGravity() { return GRAVITY; }
    @Override protected boolean canHitEntity(Entity e) { return !(e instanceof Infected) && super.canHitEntity(e); }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 200) discard();
        if (level().isClientSide && tickCount % 2 == 0)
            level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), getX(), getY(), getZ(), 0, 0, 0);
    }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        if (r.getEntity() instanceof LivingEntity e) smash(e, DAMAGE, 1.6);
    }

    private void smash(LivingEntity e, float dmg, double kb) {
        if (!e.hurt(damageSources().thrown(this, getOwner()), dmg)) return;
        Vec3 v = getDeltaMovement().multiply(1, 0, 1).normalize();
        e.knockback(kb, -v.x, -v.z);
        e.setDeltaMovement(e.getDeltaMovement().add(0, 0.35, 0));
        e.hurtMarked = true;
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (level().isClientSide) return;
        Entity direct = r instanceof EntityHitResult eh ? eh.getEntity() : null;
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2.5),
                e -> e != direct && Infected.isEnemy(e) && e.distanceToSqr(this) < 2.5 * 2.5))
            smash(e, SPLASH, 0.8);
        ServerLevel sl = (ServerLevel) level();
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()), getX(), getY(), getZ(), 60, 0.6, 0.6, 0.6, 0.3);
        sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), getX(), getY(), getZ(), 30, 0.4, 0.4, 0.4, 0.2);
        sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 2, 0.3, 0.3, 0.3, 0);
        playSound(SoundEvents.GENERIC_EXPLODE, 1.6f, 0.6f);
        playSound(SoundEvents.STONE_BREAK, 2f, 0.5f);
        discard();
    }
}
