package com.l4d.infected.client;

import com.l4d.infected.entity.JimmyInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: jimmy.json (tools/tex_jimmy.py). Broad-shouldered brute; heavier, wider-armed sprint; shoulder tackle. */
public class JimmyModel extends CommonModel<JimmyInfected> {
    public JimmyModel(ModelPart root) { super(root, "jimmy"); }

    @Override
    protected void locomotion() {
        super.locomotion();
        ra.zRot += 0.2f; la.zRot -= 0.2f;           // pads push the arms out
        body.xRot += 0.08f * runAmt;
    }

    static final float[] T = {0, .3f, .36f, .62f, .72f, 1};
    @Override
    protected void action(int id) {
        if (id != JimmyInfected.TACKLE) { super.action(id); return; }
        float p = ent.actionProgress(pt), w = k(p, new float[]{0, .1f, .85f, 1}, 0, 1, 1, 0);
        float charge = bump(p, .28f, .4f, .66f), cyc = age * 1.3f;
        // set: sink into a three-point crouch, right shoulder dropped -> charge: driving legs, shoulder leads ->
        // impact: shoulder punches through, off arm wraps -> recovery: rise out of the crouch
        drop(w * k(p, T, 0, 3.5f, 2f, 1.5f, 1f, 0));
        to(body, w, k(p, T, 0.2f, 0.95f, 0.85f, 0.9f, 1.05f, 0.25f), k(p, T, 0, -0.45f, -0.4f, -0.4f, -0.15f, 0), 0);
        head.xRot += w * k(p, T, 0, -0.75f, -0.8f, -0.8f, -0.6f, 0);
        to(ra, w, k(p, T, -0.5f, 0.2f, -0.4f, -0.5f, -1.4f, -0.6f), 0, k(p, T, 0.2f, 0.1f, 0.1f, 0.1f, 0.5f, 0.2f));
        to(rf, w, k(p, T, -0.6f, -0.3f, -1.6f, -1.6f, -0.7f, -0.6f), 0, 0);
        to(la, w, k(p, T, -0.5f, -0.3f, 0.5f, 0.6f, -1.3f, -0.5f), 0, k(p, T, -0.2f, -0.2f, -0.2f, -0.3f, -0.6f, -0.2f));
        to(lf, w, k(p, T, -0.6f, -0.4f, -0.8f, -0.8f, -1.2f, -0.6f), 0, 0);
        to(rl, w, lerp(k(p, T, 0, -1.0f, -0.6f, -0.6f, 0.4f, 0), sin(cyc) * 1.2f - 0.3f, charge), 0, 0);
        to(ll, w, lerp(k(p, T, 0, 0.3f, 0.5f, 0.5f, -0.8f, 0), -sin(cyc) * 1.2f - 0.3f, charge), 0, 0);
        to(rs, w, lerp(k(p, T, 0.2f, 1.6f, 1.2f, 1.2f, 0.6f, 0.2f), 0.5f + 0.9f * Math.max(0, cos(cyc)), charge), 0, 0);
        to(ls, w, lerp(k(p, T, 0.2f, 0.9f, 1.0f, 1.0f, 1.2f, 0.2f), 0.5f + 0.9f * Math.max(0, -cos(cyc)), charge), 0, 0);
    }
}
