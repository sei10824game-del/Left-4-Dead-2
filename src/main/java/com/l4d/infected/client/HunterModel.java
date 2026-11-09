package com.l4d.infected.client;

import com.l4d.infected.entity.HunterInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;
import static com.l4d.infected.entity.HunterInfected.*;

/** model spec: assets/l4dinfected/models/entity/hunter.json */
public class HunterModel extends CrouchModel<HunterInfected> {
    public HunterModel(ModelPart root) { super(root, "hunter", 6, 5); }

    private void jaw(float open) { part("jaw").xRot = open; }
    private void cloth(float world) { hang("rag", world); hang("rag_front", world * 0.7f); }
    private float twitch() { return (float) Math.pow(Math.max(0, sin(age * 0.21f)), 12) * 0.5f; }

    @Override
    protected void gait() {
        float s = limbAmt, c = limb * 0.75f, br = sin(age * 0.08f);
        if (!ent.onGround() && !ent.isInWater()) { // ordinary jump / fall: limbs flung out for balance
            pitch(0.6f);
            stance(-1.1f, 1.8f, -0.8f, 1.5f, 0);
            arm("right", -1.6f, 0.5f, -0.3f, -0.3f);
            arm("left", -1.4f, 0.6f, -0.3f, -0.3f);
            head(-0.1f); jaw(0.5f); cloth(0.9f);
            return;
        }
        if (ent.isAggressive()) { // loping knuckle-gallop: claws pull, hind legs drive
            float g = sin(c), g2 = sin(c + 0.5f);
            pitch(1.05f + 0.05f * br * (1 - s) + sin(c * 2) * 0.08f * s);
            stance(-0.9f - g * 0.6f * s, 1.5f + 0.3f * cos(c) * s, -0.9f - g2 * 0.6f * s, 1.5f + 0.3f * cos(c + 0.5f) * s,
                    Math.abs(sin(c)) * 1.2f * s);
            arm("right", -0.6f + g * 0.9f * s, 0.15f, -0.2f - 0.3f * Math.max(0, cos(c)) * s, -0.5f);
            arm("left", -0.6f + g2 * 0.9f * s, 0.15f, -0.2f - 0.3f * Math.max(0, cos(c + 0.5f)) * s, -0.5f);
            head(0.15f + sin(c * 2) * 0.05f * s);
            jaw(0.15f + 0.1f * s + twitch());
            cloth(0.2f + 0.6f * s + sin(age * 0.5f) * 0.15f * s);
        } else { // stalking: low, slow, long arms dangling, head tilting as it listens
            float g = sin(c) * 0.55f * s;
            pitch(0.75f + 0.04f * br);
            stance(-0.75f + g, 1.3f + 0.4f * Math.max(0, cos(c)) * s, -0.75f - g, 1.3f + 0.4f * Math.max(0, -cos(c)) * s, 0);
            arm("right", -0.15f - g * 0.6f, 0.12f, -0.35f - 0.1f * br, -0.3f);
            arm("left", -0.15f + g * 0.6f, 0.12f, -0.35f + 0.1f * br, -0.3f);
            head(0.1f + 0.05f * br);
            part("head").zRot = sin(age * 0.05f) * 0.15f;
            jaw(0.08f + 0.05f * br + twitch());
            cloth(0.1f * s + 0.03f * br);
        }
    }

    /** close-range lunge: coil back, rip upward-across with one claw, body slung forward behind it. */
    @Override
    protected void claw() {
        float p = ent.actionProgress(pt), h = clawHit();
        boolean left = ent.alt();
        String a = left ? "left" : "right", o = left ? "right" : "left";
        float sg = left ? -1 : 1, rw = armWorld(a), ow = armWorld(o), b = B;
        pitch(beat(p, h, b, 0.7f, 1.35f, 1.45f));
        part("body").yRot = sg * beat(p, h, 0, 0.35f, -0.6f, -0.7f);
        float ka = beat(p, h, -0.9f, -1.35f, -0.25f, -0.4f), kk = beat(p, h, 1.5f, 2.2f, 0.5f, 0.8f);
        stance(ka, kk, ka * 0.8f, kk * 0.9f, 0);
        arm(a, beat(p, h, rw, 0.9f, -2.3f, -2.6f), beat(p, h, 0.15f, 0.5f, -0.3f, -0.6f), beat(p, h, -0.3f, -1.2f, -0.1f, -0.2f), -0.4f);
        arm(o, beat(p, h, ow, -1.2f, -0.4f, -0.5f), 0.3f, -0.6f, -0.4f);
        head(beat(p, h, 0, -0.3f, 0.25f, 0.2f));
        jaw(beat(p, h, 0.15f, 0.8f, 0.1f, 0.4f));
        cloth(beat(p, h, 0.2f, 0.1f, 0.9f, 1.0f));
    }

    @Override
    protected void action(int id) {
        float t = ent.actionTime(pt), p = ent.actionProgress(pt);
        switch (id) {
            case COIL -> coil(p, t);
            case LEAP -> leap(t);
            case PIN -> pin(t);
            case STUN -> stun(p, t);
            default -> {}
        }
    }

    /** sinks onto its haunches, trembling and snarling, hips wiggling, then the legs fire. */
    private void coil(float p, float t) {
        float coil = smooth(seg(p, 0, 0.7f)), spring = in(seg(p, 0.85f, 1)), shake = sin(age * 3.1f) * 0.03f * coil;
        pitch(lerp(lerp(1.05f, 0.95f, coil), 1.3f, spring) + shake);
        part("body").zRot = sin(age * 0.9f) * 0.07f * coil * (1 - spring);
        float a = lerp(lerp(-0.9f, -1.35f, coil), -0.2f, spring), k = lerp(lerp(1.5f, 2.3f, coil), 0.4f, spring);
        stance(a, k, a, k, 0);
        float w = lerp(lerp(-0.6f, 0.6f, coil), -2.4f, spring);
        arm("right", w, 0.35f * coil, -0.9f * coil * (1 - spring), -0.6f);
        arm("left", w, 0.35f * coil, -0.9f * coil * (1 - spring), -0.6f);
        head(lerp(0, -0.35f, coil) + shake * 2);
        jaw(0.75f * coil + sin(age * 2.3f) * 0.08f * coil);
        cloth(0.3f * coil + 0.8f * spring);
    }

    /** airborne: stretched flat, claws thrust ahead, legs and rags streaming behind; claws drop to rake late in the arc. */
    private void leap(float t) {
        float e = smooth(seg(t, 0, 4)), flap = sin(age * 1.3f), land = smooth(seg(t, 10, 18));
        pitch(lerp(1.3f, 1.5f, e) - 0.2f * land);
        float w = lerp(-2.4f, -1.75f, e) + 0.5f * land;
        arm("right", w, 0.4f + 0.05f * flap, -0.1f, -0.5f);
        arm("left", w, 0.4f - 0.05f * flap, -0.1f, -0.5f);
        leg("right", lerp(-0.2f, 1.1f, e) - 0.6f * land, 0.6f);
        leg("left", lerp(-0.2f, 0.9f, e) - 0.6f * land, 0.7f);
        head(-0.25f);
        jaw(0.9f);
        cloth(1.3f + 0.25f * flap);
    }

    /** straddling the victim; alternating overhead claw hammers, off-hand pinning, jaw snapping on the impact. */
    private void pin(float t) {
        float u = (t % RAKE) / RAKE, h = (float) RAKE_HIT / RAKE;
        boolean left = ((int) (t / RAKE) & 1) == 1;
        String a = left ? "left" : "right", o = left ? "right" : "left";
        float sg = left ? -1 : 1;
        pitch(beat(u, h, 0.85f, 0.6f, 1.2f, 1.1f));
        part("body").yRot = sg * beat(u, h, 0, 0.3f, -0.45f, -0.4f);
        part("body").zRot = sin(age * 0.4f) * 0.04f;
        stance(-1.35f, 2.3f, -1.35f, 2.3f, beat(u, h, 0, 0.6f, -0.3f, 0));
        part("right_leg").zRot = 0.35f;
        part("left_leg").zRot = -0.35f;
        arm(a, beat(u, h, -0.9f, -3.0f, -0.2f, 0.4f), beat(u, h, 0.2f, 0.5f, -0.15f, -0.1f), beat(u, h, -0.6f, -0.8f, -0.1f, -0.2f), -0.5f);
        arm(o, beat(u, h, -0.6f, -0.7f, -0.45f, -0.5f), 0.1f, -0.5f, -0.6f);
        head(beat(u, h, 0.3f, 0.05f, 0.6f, 0.55f));
        jaw(beat(u, h, 0.3f, 0.8f, 0.05f, 0.3f));
        cloth(0.2f);
    }

    /** knocked off / missed: reels back, head shaking, arms flailing for balance, then gathers itself. */
    private void stun(float p, float t) {
        float sh = 1 - smooth(seg(p, 0.6f, 1)), reel = out(seg(p, 0, 0.15f)) * sh;
        pitch(lerp(B, -0.25f, reel));
        stance(-0.4f + 0.3f * sin(t * 0.7f) * sh, 0.9f + 0.6f * (1 - sh), -0.6f - 0.3f * sin(t * 0.7f) * sh, 1.1f + 0.4f * (1 - sh), 0);
        arm("right", lerp(-0.6f, -2.2f + sin(t * 0.9f) * 0.5f, sh), 0.9f * sh, -0.6f, 0);
        arm("left", lerp(-0.6f, -2.0f + cos(t * 0.9f) * 0.5f, sh), 0.9f * sh, -0.6f, 0);
        head(-0.3f * sh);
        part("head").yRot += sin(t * 1.4f) * 0.5f * sh;
        jaw(0.6f * sh);
        cloth(0.4f * sh);
    }
}
