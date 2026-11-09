package com.l4d.infected.client;

import com.l4d.infected.entity.ClownInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: clown.json (tools/tex_uncommon.py) */
public class ClownModel extends CommonModel<ClownInfected> {
    public ClownModel(ModelPart root) { super(root, "clown"); }

    @Override protected void locomotion() {
        super.locomotion();
        float s = Math.max(limbAmt, runAmt), hop = Math.abs(sin(limb * 0.6662f * 0.5f + age * 0.0f));
        drop(-hop * 2.2f * s);                           // bouncy gait: springs off each stride
        part("body").zRot += sin(limb * 0.6662f) * 0.12f * s;
        head.zRot += sin(limb * 0.6662f + 1f) * 0.2f * s;
        part("hat_a").zRot = sin(age * 0.3f) * 0.12f * s; part("hat_b").zRot = -sin(age * 0.3f + 1f) * 0.12f * s;
    }
    static final float[] T = {0, .25f, .45f, .75f, 1};
    @Override protected void action(int id) {
        if (id != ClownInfected.HONK) { super.action(id); return; }
        float p = ent.actionProgress(pt), w = k(p, new float[]{0, .1f, .85f, 1}, 0, 1, 1, 0), sq = bump(p, .4f, .47f, .6f);
        // horn hand to the mouth, head tips back, belly heaves with the squeeze, other hand waves a taunt
        to(la, w, k(p, T, -0.3f, -2.4f, -2.5f, -2.2f, -0.3f), 0, -0.3f); to(lf, w, k(p, T, -0.3f, -2.0f, -2.1f, -1.9f, -0.3f), 0, 0);
        to(ra, w, -0.4f + sin(age * 0.8f) * 0.5f * w, 0, 0.7f);
        to(body, w, k(p, T, 0.1f, -0.3f, -0.4f, -0.2f, 0.1f) + 0.2f * sq, 0, 0);
        head.xRot += w * k(p, T, 0, -0.5f, -0.55f, -0.3f, 0); head.zRot += w * 0.2f * sin(age * 0.9f);
        drop(w * (0.6f * sq - 0.2f));
        part("jaw").xRot = lerp(part("jaw").xRot, 0.5f * w + 0.4f * sq, w);
    }
}
