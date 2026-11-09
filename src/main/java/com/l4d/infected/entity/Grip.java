package com.l4d.infected.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Shared helpers for specials that pin a victim (smoker choke, charger carry/pound). Server side only. */
final class Grip {
    private Grip() {}

    /** victim id -> live, grabbable living entity in the same level, else null. */
    static LivingEntity resolve(Entity owner, int id) {
        if (id < 0) return null;
        return owner.level().getEntity(id) instanceof LivingEntity l && !l.isRemoved() && Infected.isEnemy(l) && grabbable(l) ? l : null;
    }

    /** person-sized and not a boss: something that can be dragged / carried. */
    static boolean grabbable(LivingEntity e) { return e.getBbWidth() <= 1.5f && e.getBbHeight() <= 2.6f && e.canChangeDimensions(); }

    /** move the victim to a point; players get a teleport that leaves their camera rotation alone. */
    static void place(LivingEntity v, Vec3 p) {
        if (v.isPassenger()) v.stopRiding();
        if (v instanceof ServerPlayer sp) sp.connection.teleport(p.x, p.y, p.z, 0, 0, EnumSet.of(RelativeMovement.X_ROT, RelativeMovement.Y_ROT));
        else v.moveTo(p.x, p.y, p.z, v.getYRot(), v.getXRot());
        v.setDeltaMovement(Vec3.ZERO);
        v.fallDistance = 0;
        subdue(v, 5);
    }

    /** victim can't run / fight back: heavy slowness + weakness; mobs lose their path. */
    static void subdue(LivingEntity v, int slow) {
        v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, slow, false, false));
        v.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10, 4, false, false));
        v.setSprinting(false);
        if (v instanceof Mob m) m.getNavigation().stop();
    }

    /** push a velocity to a victim (players need hurtMarked to receive it). */
    static void push(LivingEntity v, Vec3 vel) {
        v.setDeltaMovement(vel);
        v.hurtMarked = true;
    }
}
