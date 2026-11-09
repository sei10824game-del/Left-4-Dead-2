package com.l4d.infected.client;

import com.l4d.infected.entity.CedaInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: ceda.json (tools/tex_uncommon.py) */
public class CedaModel extends CommonModel<CedaInfected> {
    public CedaModel(ModelPart root) { super(root, "ceda"); }

    static final float[] T = {0, .2f, .45f, .55f, .75f, 1};
    @Override protected void action(int id) {
        if (id != CedaInfected.FUME) { super.action(id); return; }
        float p = ent.actionProgress(pt), w = k(p, new float[]{0, .1f, .85f, 1}, 0, 1, 1, 0);
        // inhale: head back, chest swells, arms sweep out -> heave: lurch forward, jaw wide, arms drop -> sag and recover
        to(body, w, k(p, T, 0.1f, -0.15f, -0.4f, 0.65f, 0.35f, 0.1f), 0, 0);
        head.xRot += w * k(p, T, 0, -0.4f, -0.75f, 0.6f, 0.3f, 0);
        to(ra, w, k(p, T, -0.3f, -0.6f, -1.0f, 0.2f, -0.2f, -0.3f), 0, k(p, T, 0.1f, 0.5f, 0.9f, 0.2f, 0.2f, 0.1f));
        to(la, w, k(p, T, -0.3f, -0.6f, -1.0f, 0.2f, -0.2f, -0.3f), 0, -k(p, T, 0.1f, 0.5f, 0.9f, 0.2f, 0.2f, 0.1f));
        to(rf, w, -0.5f, 0, 0); to(lf, w, -0.5f, 0, 0);
        drop(w * k(p, T, 0, 0, -0.4f, 1.4f, 0.8f, 0));
        part("jaw").xRot = lerp(part("jaw").xRot, k(p, T, 0.1f, 0.3f, 0.5f, 1.0f, 0.5f, 0.1f), w);
    }
}
