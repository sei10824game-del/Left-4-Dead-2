package com.l4d.infected.client;

import com.l4d.infected.entity.BloaterBase;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/**
 * Shared rig of the bloaters (boomer.json / boomette.json use the same part names):
 * waddling walk with a lagging, breathing gut, pulsing pustules, vomit (rear back + throat swell -> heave + sustained
 * convulsing spray -> recover), burp idle and a heavy backhand slap.
 */
public class BloaterModel<T extends BloaterBase> extends InfectedModel<T> {
    static final float[] VT = {0, 10, 16, 19, 30, 46, 56};
    static final float[] BT = {0, 8, 10, 16, 26};
    static final float[] CT = {0, .3f, .39f, .55f, 1};

    public BloaterModel(ModelPart root, String spec) { super(root, spec); }

    static float sin(float x) { return (float) Math.sin(x); }
    static float cos(float x) { return (float) Math.cos(x); }
    static void scale(ModelPart p, float x, float y, float z) { p.xScale = x; p.yScale = y; p.zScale = z; }
    static void mul(ModelPart p, float s) { p.xScale *= s; p.yScale *= s; p.zScale *= s; }

    @Override
    protected void locomotion() {
        root.visible = ent.deathTime == 0; // popped: nothing left to draw
        float s = limbAmt, c = limb * 0.5f, br = sin(age * 0.08f);
        ModelPart body = part("body"), belly = part("belly"), head = part("head");
        float roll = cos(c) * 0.13f * s;
        body.zRot = roll;
        body.xRot += 0.06f * s + 0.025f * br;
        body.y -= Math.abs(sin(c)) * 0.9f * s;
        head.xRot += head.getInitialPose().xRot;
        head.zRot = -roll * 0.7f;
        // stubby legs swing out around the gut
        part("right_leg").xRot = cos(c) * 0.75f * s;
        part("left_leg").xRot = -cos(c) * 0.75f * s;
        part("right_leg").zRot = 0.06f + 0.1f * s * Math.max(0, cos(c));
        part("left_leg").zRot = -0.06f - 0.1f * s * Math.max(0, -cos(c));
        // gut lags the waddle, squashes on each footfall, breathes
        belly.zRot = -cos(c - 0.7f) * 0.12f * s;
        belly.xRot = sin(c * 2) * 0.05f * s;
        float b = 1 + 0.035f * br, squash = 0.04f * Math.abs(cos(c)) * s;
        scale(belly, b + squash * 0.5f, 1 + 0.02f * br - squash, b * 1.02f);
        float beat = Math.max(0, sin(age * 0.25f));
        scale(part("pustules"), 1, 1, 1); mul(part("pustules"), 1 + 0.14f * beat * beat);
        scale(part("back_sac"), 1, 1, 1); mul(part("back_sac"), 1 + 0.08f * Math.max(0, sin(age * 0.19f + 1.5f)));
        scale(part("throat"), 1, 1, 1); mul(part("throat"), 1 + 0.04f * sin(age * 0.12f));
        part("jaw").xRot = 0.08f + 0.05f * br;
        part("drool").xRot = -0.2f * s - part("jaw").xRot;
        part("drool").zRot = sin(age * 0.11f) * 0.2f - roll;
        // dangling arms, swinging against the legs, forearms hanging slack
        for (int side = 0; side < 2; side++) {
            float sg = side == 0 ? 1 : -1;
            ModelPart arm = part(side == 0 ? "right_arm" : "left_arm"), fore = part(side == 0 ? "right_fore" : "left_fore");
            arm.xRot = -sg * cos(c) * 0.45f * s + 0.04f * br;
            arm.zRot += sg * (0.06f * s + 0.02f * br);
            fore.xRot += -0.15f * s + sin(c - 0.8f) * 0.15f * s * sg;
        }
        if (has("hair")) part("hair").xRot += -0.6f * (head.xRot - head.getInitialPose().xRot) + 0.12f * s + 0.04f * sin(age * 0.07f);
    }

    @Override
    protected void action(int id) {
        float t = ent.actionTime(pt);
        if (id == BloaterBase.VOMIT) vomit(t);
        else if (id == BloaterBase.BURP) burp(t);
    }

    private void vomit(float t) {
        // wind-up: rear back, clench gut, throat balloons, arms pulled back; release: heave forward, jaw wide;
        // spray: rhythmic convulsions of gut/throat/torso; recovery: settle back into the idle pose.
        float spray = seg(t, 16, 18) * (1 - seg(t, 42, 46)), conv = sin(t * 0.9f) * spray;
        float tremble = sin(age * 3.3f) * 0.035f * seg(t, 7, 12) * (1 - seg(t, 15, 16));
        part("body").xRot += keys(t, VT, new float[]{0, -.3f, -.36f, .42f, .36f, .38f, 0}) + 0.06f * conv + tremble;
        part("head").xRot += keys(t, VT, new float[]{0, -.55f, -.6f, .35f, .3f, .3f, 0}) + tremble;
        part("jaw").xRot += keys(t, VT, new float[]{0, .15f, .1f, 1f, .9f, .95f, 0}) + 0.12f * sin(t * 1.3f) * spray;
        mul(part("throat"), keys(t, VT, new float[]{1, 1.35f, 1.5f, .95f, 1, 1, 1}) + 0.08f * sin(t * 0.9f + 1) * spray);
        mul(part("belly"), keys(t, VT, new float[]{1, .93f, .9f, 1.05f, .95f, .95f, 1}) + 0.05f * conv);
        mul(part("pustules"), 1 + 0.2f * seg(t, 4, 16) * (1 - seg(t, 18, 30)));
        float ax = keys(t, VT, new float[]{0, .45f, .5f, -.5f, -.45f, -.45f, 0});
        float az = keys(t, VT, new float[]{0, .25f, .3f, .45f, .4f, .4f, 0});
        float fx = keys(t, VT, new float[]{0, -.4f, -.5f, -.3f, -.3f, -.3f, 0});
        part("right_arm").xRot += ax; part("left_arm").xRot += ax;
        part("right_arm").zRot += az; part("left_arm").zRot -= az;
        part("right_fore").xRot += fx; part("left_fore").xRot += fx;
        float leg = keys(t, VT, new float[]{0, .1f, .12f, -.25f, -.22f, -.22f, 0});
        part("right_leg").xRot += leg; part("left_leg").xRot -= leg;
        part("drool").visible = t < 15 || t > 46;
    }

    private void burp(float t) {
        part("body").xRot += keys(t, BT, new float[]{0, -.1f, .08f, .04f, 0});
        part("head").xRot += keys(t, BT, new float[]{0, -.35f, .15f, .1f, 0});
        part("jaw").xRot += keys(t, BT, new float[]{0, .05f, .6f, .4f, 0});
        mul(part("throat"), keys(t, BT, new float[]{1, 1.3f, .9f, 1, 1}));
        mul(part("belly"), keys(t, BT, new float[]{1, 1.07f, .95f, .98f, 1}));
    }

    /** heavy backhand slap: arm hauled up and out, torso winds away, swings across (hit frame = 7/18), gut lags behind. */
    @Override
    protected void claw() {
        float p = ent.actionProgress(pt), sg = ent.alt() ? -1 : 1;
        String side = ent.alt() ? "left" : "right";
        ModelPart arm = part(side + "_arm"), fore = part(side + "_fore");
        arm.xRot += keys(p, CT, new float[]{0, -1.9f, -1.1f, -.6f, 0});
        arm.zRot += sg * keys(p, CT, new float[]{0, .9f, -.2f, -.5f, 0});
        fore.xRot += keys(p, CT, new float[]{0, -.9f, -.15f, -.3f, 0});
        float twist = keys(p, CT, new float[]{0, .35f, -.3f, -.45f, 0});
        part("body").yRot += sg * twist;
        part("body").xRot += keys(p, CT, new float[]{0, -.12f, .18f, .12f, 0});
        part("belly").yRot -= sg * keys(p, CT, new float[]{0, .1f, .25f, -.2f, 0});
        part("head").yRot -= sg * twist * 0.5f;
        part("jaw").xRot += 0.35f * bump(p, .2f, .39f, .7f);
    }
}
