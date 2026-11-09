package com.l4d.infected.client;

import com.l4d.infected.entity.MeatWallInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: meatwall.json (tools/tex_meatwall.py). */
public class MeatWallModel extends InfectedModel<MeatWallInfected> {
    public MeatWallModel(ModelPart root) { super(root, "meatwall"); }

    @Override
    protected void locomotion() {
        float s = limbAmt, c = limb * 0.5f, br = (float) Math.sin(age * 0.07f);
        part("body").xRot = 0.1f + 0.03f * br; part("body").zRot = (float) Math.sin(c) * 0.06f * s;
        part("head").xRot += 0.2f; part("head").zRot = (float) Math.sin(age * 0.05f) * 0.15f;
        part("right_leg").xRot = (float) Math.cos(c) * 0.5f * s; part("left_leg").xRot = (float) -Math.cos(c) * 0.5f * s;
        part("right_arm").xRot = -0.3f + (float) Math.cos(c) * 0.2f * s + 0.03f * br; part("left_arm").xRot = -0.3f - (float) Math.cos(c) * 0.2f * s;
        part("right_arm").zRot = 0.15f; part("left_arm").zRot = -0.15f;
        for (String n : new String[]{"mass_a", "mass_b"}) if (has(n)) part(n).zRot = 0.04f * br;
    }

    @Override
    protected void claw() { action(MeatWallInfected.SMASH); }

    @Override
    protected void action(int id) {
        float p = ent.actionProgress(pt), w = keys(p, new float[]{0, .1f, .85f, 1}, new float[]{0, 1, 1, 0});
        if (id == MeatWallInfected.SMASH) { // both fists to the ceiling, body arches back, then the whole wall drops forward
            float[] T = {0, .5f, .58f, .7f, 1};
            float a = keys(p, T, new float[]{-0.3f, -3.0f, -0.2f, -0.1f, -0.3f});
            part("right_arm").xRot = lerp(part("right_arm").xRot, a, w); part("left_arm").xRot = lerp(part("left_arm").xRot, a, w);
            part("body").xRot = lerp(part("body").xRot, keys(p, T, new float[]{0.1f, -0.35f, 0.7f, 0.65f, 0.1f}), w);
            part("head").xRot += w * keys(p, T, new float[]{0, -0.4f, 0.4f, 0.3f, 0});
        } else if (id == MeatWallInfected.CRUSH) { // lowers the shoulder and rams
            float[] T = {0, .25f, .4f, .6f, 1};
            part("body").xRot = lerp(part("body").xRot, keys(p, T, new float[]{0.1f, 0.2f, 0.75f, 0.6f, 0.1f}), w);
            part("body").yRot = lerp(part("body").yRot, keys(p, T, new float[]{0, 0.5f, -0.2f, -0.2f, 0}), w);
            part("right_arm").xRot = lerp(part("right_arm").xRot, keys(p, T, new float[]{-0.3f, -1.4f, -1.6f, -1.2f, -0.3f}), w);
            part("left_arm").xRot = lerp(part("left_arm").xRot, keys(p, T, new float[]{-0.3f, -1.4f, -1.6f, -1.2f, -0.3f}), w);
        }
    }
}
