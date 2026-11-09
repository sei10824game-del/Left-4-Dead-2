package com.l4d.infected.client;

import com.l4d.infected.entity.SpitterInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;
import static com.l4d.infected.client.BloaterModel.*;

/**
 * model spec: assets/l4dinfected/models/entity/spitter.json (tools/tex_spitter.py).
 * Loping prowl with a bobbing neck and gurgling sac; spit = neck coils back while the sac swells and trembles,
 * then the head whips forward and the jaw gapes on the release frame, follow-through, recover.
 */
public class SpitterModel extends InfectedModel<SpitterInfected> {
    static final float[] ST = {0, 8, 15, SpitterInfected.SPIT_RELEASE, 20, 25, SpitterInfected.SPIT_LEN};

    public SpitterModel(ModelPart root) { super(root, "spitter"); }

    @Override
    protected void locomotion() {
        float s = limbAmt, c = limb * 0.6662f, br = sin(age * 0.09f);
        ModelPart head = part("head"), neck = part("neck"), body = part("body");
        head.xRot += head.getInitialPose().xRot;
        body.xRot += 0.12f * s + 0.03f * br;
        body.zRot = cos(c) * 0.06f * s;
        neck.xRot += 0.05f * br + sin(c * 2) * 0.08f * s;
        neck.zRot = -cos(c) * 0.08f * s;
        part("right_leg").xRot = cos(c) * 1.1f * s;
        part("left_leg").xRot = -cos(c) * 1.1f * s;
        for (int side = 0; side < 2; side++) {
            float sg = side == 0 ? 1 : -1;
            ModelPart arm = part(side == 0 ? "right_arm" : "left_arm"), fore = part(side == 0 ? "right_fore" : "left_fore");
            arm.xRot = -sg * cos(c) * 0.7f * s + 0.05f * br;
            arm.zRot += sg * 0.03f * br;
            fore.xRot += -0.25f * s - Math.max(0, sg * sin(c - 0.6f)) * 0.5f * s; // forearms whip loosely behind the swing
        }
        part("rag").xRot = -0.35f * s - 0.12f * s * Math.abs(sin(c)) + 0.04f * sin(age * 0.1f);
        // gurgle: jaw twitches, the sac throbs with an irregular double beat
        part("jaw").xRot = 0.1f + 0.06f * Math.max(0, sin(age * 0.15f)) + 0.03f * sin(age * 0.9f) * Math.max(0, sin(age * 0.05f));
        float sac = 1 + 0.06f * sin(age * 0.2f) + 0.06f * Math.max(0, sin(age * 0.6f)) * Math.max(0, sin(age * 0.07f));
        scale(part("sac"), sac, sac, sac);
    }

    @Override
    protected void action(int id) {
        if (id != SpitterInfected.SPIT) return;
        float t = ent.actionTime(pt), tremble = sin(age * 3.1f) * 0.04f * seg(t, 9, 13) * (1 - seg(t, 16, 17));
        part("neck").xRot += keys(t, ST, new float[]{0, -.5f, -.85f, .35f, .5f, .2f, 0}) + tremble;
        part("head").xRot += keys(t, ST, new float[]{0, -.3f, -.5f, .3f, .35f, .1f, 0});
        part("body").xRot += keys(t, ST, new float[]{0, -.15f, -.25f, .25f, .3f, .1f, 0});
        part("jaw").xRot += keys(t, ST, new float[]{0, .1f, .2f, 1.1f, .9f, .3f, 0});
        mul(part("sac"), keys(t, ST, new float[]{1, 1.3f, 1.6f, .85f, .8f, .95f, 1}) + tremble * 2);
        float ax = keys(t, ST, new float[]{0, .3f, .45f, -.4f, -.5f, -.2f, 0});
        float az = keys(t, ST, new float[]{0, .3f, .4f, .15f, .1f, .05f, 0});
        float fx = keys(t, ST, new float[]{0, -.5f, -.7f, -.2f, -.2f, -.3f, 0});
        part("right_arm").xRot += ax; part("left_arm").xRot += ax;
        part("right_arm").zRot += az; part("left_arm").zRot -= az;
        part("right_fore").xRot += fx; part("left_fore").xRot += fx;
        float leg = keys(t, ST, new float[]{0, .15f, .2f, -.3f, -.35f, -.1f, 0});
        part("right_leg").xRot += leg; part("left_leg").xRot -= leg;
    }

    /** base claw plus the long forearm snapping out on the strike and the neck lunging with it. */
    @Override
    protected void claw() {
        super.claw();
        float p = ent.actionProgress(pt);
        part(ent.alt() ? "left_fore" : "right_fore").xRot += -0.8f * bump(p, .1f, .3f, .4f) + 0.3f * bump(p, .38f, .5f, .8f);
        part("neck").xRot += -0.25f * bump(p, .1f, .3f, .4f) + 0.35f * bump(p, .36f, .45f, .8f);
        part("jaw").xRot += 0.6f * bump(p, .25f, .42f, .7f);
    }
}
