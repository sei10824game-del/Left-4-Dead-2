package com.l4d.infected.client;

import com.l4d.infected.entity.RoadcrewInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: roadcrew.json (tools/tex_uncommon.py) */
public class RoadcrewModel extends CommonModel<RoadcrewInfected> {
    public RoadcrewModel(ModelPart root) { super(root, "roadcrew"); }

    static final float[] T = {0, .3f, .46f, .6f, 1};
    @Override protected void action(int id) {
        if (id != RoadcrewInfected.SWEEP) { super.action(id); return; }
        float p = ent.actionProgress(pt), w = k(p, new float[]{0, .1f, .8f, 1}, 0, 1, 1, 0);
        // heave the tool back over the shoulder, then whip round level with the hip: body and arms swing as one
        to(body, w, k(p, T, 0.1f, 0.05f, 0.35f, 0.3f, 0.1f), k(p, T, 0, 1.0f, -1.2f, -1.3f, 0), 0);
        head.yRot += w * k(p, T, 0, 0.5f, -0.9f, -0.9f, 0);
        to(ra, w, k(p, T, -0.3f, -1.5f, -1.5f, -1.3f, -0.3f), k(p, T, 0, 0.5f, -0.9f, -1.0f, 0), k(p, T, 0.1f, 0.4f, 0.2f, 0.2f, 0.1f));
        to(la, w, k(p, T, -0.3f, -1.3f, -1.4f, -1.2f, -0.3f), k(p, T, 0, 0.9f, -0.6f, -0.7f, 0), k(p, T, -0.1f, -0.2f, -0.4f, -0.4f, -0.1f));
        to(rf, w, -0.3f, 0, 0); to(lf, w, -0.4f, 0, 0);
        drop(w * k(p, T, 0, 1f, 2f, 2f, 0));
        to(rl, w, k(p, T, 0, 0.3f, -0.6f, -0.6f, 0), 0, 0); to(ll, w, k(p, T, 0, -0.4f, 0.5f, 0.5f, 0), 0, 0);
        to(rs, w, k(p, T, 0, 0.3f, 0.8f, 0.8f, 0), 0, 0);
        part("jaw").xRot = lerp(part("jaw").xRot, k(p, T, 0.1f, 0.3f, 0.7f, 0.5f, 0.1f), w);
    }
}
