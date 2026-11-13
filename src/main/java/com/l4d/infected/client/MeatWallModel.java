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

    private void arms(float x, float z) { part("right_arm").xRot = x; part("left_arm").xRot = x; part("right_arm").zRot = -z; part("left_arm").zRot = z; }

    @Override
    protected void action(int id) {
        float p = ent.actionProgress(pt), t = ent.actionTime(pt);
        float w = keys(p, new float[]{0, .08f, .88f, 1}, new float[]{0, 1, 1, 0});
        ModelPart b = part("body"), h = part("head"), ra = part("right_arm"), la = part("left_arm");
        float[] base = {b.xRot, ra.xRot, la.xRot, ra.zRot, la.zRot};
        switch (id) {
            case MeatWallInfected.SMASH -> { // both fists to the ceiling, body arches back, the whole wall drops forward
                float[] T = {0, .5f, .58f, .7f, 1};
                float a = keys(p, T, new float[]{-0.3f, -3.0f, -0.2f, -0.1f, -0.3f});
                arms(lerp(base[1], a, w), 0.15f);
                b.xRot = lerp(b.xRot, keys(p, T, new float[]{0.1f, -0.35f, 0.7f, 0.65f, 0.1f}), w);
                h.xRot += w * keys(p, T, new float[]{0, -0.4f, 0.4f, 0.3f, 0});
            }
            case MeatWallInfected.CRUSH -> { // lowers the shoulder and rams
                float[] T = {0, .25f, .4f, .6f, 1};
                b.xRot = lerp(b.xRot, keys(p, T, new float[]{0.1f, 0.2f, 0.75f, 0.6f, 0.1f}), w);
                b.yRot = lerp(b.yRot, keys(p, T, new float[]{0, 0.5f, -0.2f, -0.2f, 0}), w);
                arms(lerp(base[1], keys(p, T, new float[]{-0.3f, -1.4f, -1.6f, -1.2f, -0.3f}), w), 0.15f);
            }
            case MeatWallInfected.CRACK3, MeatWallInfected.CRACK5 -> { // fists overhead, hold and tremble, then hammer the ground; ripples travel out
                float hit = id == MeatWallInfected.CRACK3 ? MeatWallInfected.CRACK3_HIT / 40f : MeatWallInfected.CRACK5_HIT / 48f;
                float[] T = {0, hit - .12f, hit, hit + .12f, 1};
                float shake = (float) Math.sin(age * 1.7f) * 0.05f * smooth(seg(p, 0.1f, hit - .12f)) * (1 - seg(p, hit - .12f, hit));
                arms(lerp(base[1], keys(p, T, new float[]{-0.3f, -3.1f, 0.1f, -0.1f, -0.3f}) + shake, w), 0.2f);
                b.xRot = lerp(b.xRot, keys(p, T, new float[]{0.1f, -0.45f, 0.85f, 0.7f, 0.1f}), w);
                h.xRot += w * keys(p, T, new float[]{0, -0.5f, 0.45f, 0.3f, 0});
                b.zRot += shake;
            }
            case MeatWallInfected.PULL -> { // right fist plunges into the chest, hauls a body out backwards and flings it; three cycles
                float u = ((t - 10) % 16 + 16) % 16 / 16f, on = t < 10 || t > 58 ? 0 : 1;
                float[] T = {0, .35f, .75f, .88f, 1};
                float rx = keys(u, T, new float[]{-0.4f, -1.3f, -0.6f, -2.9f, -0.4f}), rz = keys(u, T, new float[]{0.2f, 0.9f, 0.5f, 0.1f, 0.2f});
                ra.xRot = lerp(ra.xRot, lerp(base[1], rx, on), w); ra.zRot = lerp(ra.zRot, lerp(base[3], -rz, on), w);
                la.xRot = lerp(la.xRot, -1.1f, w * on); la.zRot = lerp(la.zRot, 0.7f, w * on);
                b.xRot = lerp(b.xRot, keys(u, T, new float[]{0.1f, 0.3f, -0.35f, 0.55f, 0.1f}), w * on);
                b.yRot = lerp(b.yRot, keys(u, T, new float[]{0, -0.25f, 0.2f, -0.3f, 0}), w * on);
                h.xRot += w * on * keys(u, T, new float[]{0, 0.3f, -0.3f, 0.3f, 0});
            }
            case MeatWallInfected.GRAB -> { // reach, clamp both fists round the victim, lift overhead squeezing, hurl
                float[] T = {0, .1f, .18f, .74f, .78f, 1};
                float sq = (float) Math.sin(age * 0.9f) * 0.08f * seg(p, .18f, .3f) * (1 - seg(p, .72f, .76f));
                arms(lerp(base[1], keys(p, T, new float[]{-0.3f, -1.6f, -1.8f, -2.7f, 0.3f, -0.3f}) + sq, w), keys(p, T, new float[]{0.15f, 0.2f, 0.5f, 0.55f, 0.1f, 0.15f}));
                b.xRot = lerp(b.xRot, keys(p, T, new float[]{0.1f, 0.3f, 0.35f, -0.3f, 0.75f, 0.1f}), w);
                h.xRot += w * keys(p, T, new float[]{0, 0.1f, 0.15f, -0.3f, 0.4f, 0});
            }
            default -> {}
        }
    }
}
