package com.l4d.infected.client;

import com.l4d.infected.entity.Infected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/**
 * Base for crouching specials with three-segment legs (leg/shin/foot) and three-segment arms (arm/fore/hand).
 * Poses are authored in "world" pitch: {@link #pitch} sets the torso lean B, {@link #arm}/{@link #head}/{@link #hang}
 * take angles relative to vertical so limbs stay readable whatever the torso does; {@link #stance} plants the
 * longer leg on the ground by moving the hips.
 */
abstract class CrouchModel<T extends Infected> extends InfectedModel<T> {
    private final float thigh, shin;
    protected float B, look;

    CrouchModel(ModelPart root, String spec, float thigh, float shin) {
        super(root, spec);
        this.thigh = thigh;
        this.shin = shin;
    }

    @Override
    protected final void locomotion() {
        look = part("head").xRot;
        gait();
    }

    protected abstract void gait();

    protected static float sin(float x) { return (float) Math.sin(x); }
    protected static float cos(float x) { return (float) Math.cos(x); }

    /** thigh pitch a (negative = forward), knee bend k (positive = shin back), foot kept flat. Returns leg height. */
    protected float leg(String s, float a, float k) {
        part(s + "_leg").xRot = a;
        part(s + "_shin").xRot = k;
        part(s + "_foot").xRot = -(a + k);
        return thigh * cos(a) + shin * cos(a + k) + 1;
    }

    /** pose both legs and drop the hips so the longer leg touches the ground; lift raises everything. */
    protected void stance(float aR, float kR, float aL, float kL, float lift) {
        float y = 24 - Math.max(leg("right", aR, kR), leg("left", aL, kL)) - lift;
        part("body").y = y;
        part("right_leg").y = y;
        part("left_leg").y = y;
    }

    protected void pitch(float b) { B = b; part("body").xRot = b; }

    /** world: arm pitch relative to hanging straight down (negative = forward/up); z: outward spread (mirrored for left). */
    protected void arm(String s, float world, float z, float elbow, float wrist) {
        part(s + "_arm").xRot = world - B;
        part(s + "_arm").zRot = s.equals("right") ? z : -z;
        part(s + "_fore").xRot = elbow;
        part(s + "_hand").xRot = wrist;
    }

    /** current world pitch of an arm (to blend from). */
    protected float armWorld(String s) { return part(s + "_arm").xRot + B; }

    protected void head(float extra) { part("head").xRot = look - B * 0.85f + extra; }

    /** cloth / dangling part: world pitch (0 = hanging, positive = streaming backwards). */
    protected void hang(String p, float world) { part(p).xRot = world - B; }

    /**
     * One attack beat over u in 0..1 whose impact lands at {@code hit}: rest -> wind (anticipation) -> strike (fast,
     * eased in) -> follow-through -> back to rest.
     */
    protected static float beat(float u, float hit, float rest, float wind, float strike, float follow) {
        float w = smooth(seg(u, 0, hit - 0.15f)), s = in(seg(u, hit - 0.15f, hit));
        float f = smooth(seg(u, hit, hit + 0.15f)), r = smooth(seg(u, hit + 0.15f, 1));
        return lerp(lerp(lerp(lerp(rest, wind, w), strike, s), follow, f), rest, r);
    }

    /** fraction of the claw action at which Infected.onActionTick lands the hit. */
    protected float clawHit() {
        int len = Math.max(1, ent.actionLen());
        return (float) (len * 5 / 12) / len;
    }
}
