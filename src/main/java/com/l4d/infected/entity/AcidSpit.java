package com.l4d.infected.entity;

import com.l4d.infected.L4D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Spitter glob: arcing projectile with an acid trail; splashes into an {@link AcidPool} on the ground below the impact. */
public class AcidSpit extends ThrowableItemProjectile {
    static final DustParticleOptions ACID_DUST = new DustParticleOptions(new Vector3f(0.6f, 1f, 0.15f), 1.3f);

    public AcidSpit(EntityType<? extends AcidSpit> t, Level l) { super(t, l); }
    public AcidSpit(Level l, LivingEntity owner) { super(L4D.ACID_SPIT.get(), owner, l); }
    @Override protected Item getDefaultItem() { return Items.SLIME_BALL; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ACID_DUST, getX(), getY() + 0.1, getZ(), 0, 0, 0);
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.ITEM_SLIME, getX(), getY(), getZ(), 0, -0.05, 0);
        }
    }

    @Override protected boolean canHitEntity(Entity e) { return super.canHitEntity(e) && !(e instanceof Infected); }

    @Override
    protected void onHitEntity(EntityHitResult r) {
        super.onHitEntity(r);
        if (r.getEntity() instanceof LivingEntity e && Infected.isEnemy(e)) e.hurt(damageSources().thrown(this, getOwner()), 3f);
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 p = r.getLocation();
        if (r instanceof BlockHitResult b) p = p.add(Vec3.atLowerCornerOf(b.getDirection().getNormal()).scale(0.1));
        BlockPos bp = BlockPos.containing(p);
        for (int i = 0; i < 6 && !level().getBlockState(bp.below()).isFaceSturdy(level(), bp.below(), Direction.UP); i++) bp = bp.below();
        sl.sendParticles(ParticleTypes.ITEM_SLIME, p.x, p.y + 0.2, p.z, 24, 0.3, 0.2, 0.3, 0.15);
        sl.sendParticles(ACID_DUST, p.x, p.y + 0.2, p.z, 16, 0.6, 0.2, 0.6, 0);
        playSound(SoundEvents.SLIME_BLOCK_FALL, 1.2f, 0.6f);
        playSound(SoundEvents.FIRE_EXTINGUISH, 0.6f, 1.4f);
        level().addFreshEntity(new AcidPool(level(), p.x, bp.getY(), p.z, 2.5f, 140, getOwner()));
        discard();
    }
}
