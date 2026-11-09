package com.l4d.infected.client;

import com.l4d.infected.entity.FallenInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: fallen.json (tools/tex_uncommon.py) */
public class FallenModel extends CommonModel<FallenInfected> {
    public FallenModel(ModelPart root) { super(root, "fallen"); }

    static final float[] T = {0, .3f, .44f, .62f, 1};
    @Override protected void action(int id) {
        if (id != FallenInfected.BUTT) { super.action(id); return; }
        float p = ent.actionProgress(pt), w = k(p, new float[]{0, .1f, .85f, 1}, 0, 1, 1, 0);
        // rifle held across the body: pull it back to the hip, thrust the stock forward, retrieve
        to(body, w, k(p, T, 0.15f, -0.1f, 0.5f, 0.4f, 0.15f), k(p, T, 0, 0.5f, -0.5f, -0.4f, 0), 0);
        head.xRot += w * k(p, T, 0, -0.1f, 0.25f, 0.2f, 0);
        to(ra, w, k(p, T, -0.9f, -0.8f, -1.55f, -1.4f, -0.9f), k(p, T, 0.3f, 0.5f, -0.3f, -0.2f, 0.3f), 0.1f);
        to(la, w, k(p, T, -0.9f, -0.7f, -1.5f, -1.3f, -0.9f), k(p, T, -0.4f, 0.1f, -0.5f, -0.4f, -0.4f), -0.1f);
        to(rf, w, k(p, T, -1.0f, -1.9f, -0.2f, -0.4f, -1.0f), 0, 0); to(lf, w, k(p, T, -1.0f, -1.7f, -0.2f, -0.4f, -1.0f), 0, 0);
        drop(w * k(p, T, 0, 0.4f, 1.2f, 1f, 0));
        to(rl, w, k(p, T, 0, 0.2f, -0.5f, -0.5f, 0), 0, 0); to(rs, w, k(p, T, 0, 0.2f, 0.6f, 0.6f, 0), 0, 0);
    }
}
