package com.l4d.infected.client;

import com.l4d.infected.entity.RiotInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.lerp;

/** model spec: riot.json (tools/tex_riot.py). Shield hangs at the side when idle and is raised as a guard when aggro. */
public class RiotModel extends CommonModel<RiotInfected> {
    public RiotModel(ModelPart root) { super(root, "riot"); }

    @Override
    protected void locomotion() {
        super.locomotion();
        float g = runAmt, sway = sin(limb * 0.6662f) * 0.08f * limbAmt;
        to(la, 1, lerp(-0.1f + sway, -1.45f + sway, g), lerp(1.4f, 0.45f, g), lerp(-0.1f, 0, g));
        to(lf, 1, lerp(-0.1f, -1.5f, g), 0, 0);
        head.xRot += 0.1f * g; // peeks over the rim
    }

    static final float[] T = {0, .3f, .42f, .5f, .65f, 1};
    @Override
    protected void action(int id) {
        if (id != RiotInfected.BASH) { super.action(id); return; }
        float p = ent.actionProgress(pt), w = k(p, new float[]{0, .1f, .8f, 1}, 0, 1, 1, 0);
        // coil: crouch, left shoulder back, slab tucked -> drive: lead leg steps, shoulder & slab slam forward (frame 10) -> settle
        drop(w * k(p, T, 0, 2.5f, 1f, 0.5f, 1f, 0));
        to(body, w, k(p, T, 0.2f, 0.55f, 0.35f, 0.45f, 0.4f, 0.2f), k(p, T, 0, -0.5f, 0.6f, 0.5f, 0.2f, 0), 0);
        head.xRot += w * k(p, T, 0, 0.2f, 0.3f, 0.3f, 0.1f, 0);
        to(la, w, k(p, T, -1.45f, -1.1f, -1.55f, -1.5f, -1.45f, -1.45f), k(p, T, 0.45f, 0.2f, 0.65f, 0.6f, 0.45f, 0.45f), 0);
        to(lf, w, k(p, T, -1.5f, -1.75f, -1.35f, -1.4f, -1.5f, -1.5f), 0, 0);
        to(ra, w, k(p, T, -0.5f, 0.3f, -1.2f, -1.1f, -0.8f, -0.5f), 0, k(p, T, 0.25f, 0.2f, -0.2f, -0.2f, 0, 0.25f));
        to(rf, w, k(p, T, -0.8f, -0.5f, -1.3f, -1.3f, -1f, -0.8f), 0, 0);
        to(rl, w, k(p, T, 0, 0.4f, 0.6f, 0.5f, 0.2f, 0), 0, 0);
        to(rs, w, k(p, T, 0.2f, 0.9f, 0.3f, 0.3f, 0.5f, 0.2f), 0, 0);
        to(ll, w, k(p, T, 0, -0.6f, -0.9f, -0.8f, -0.4f, 0), 0, 0);
        to(ls, w, k(p, T, 0.2f, 1.0f, 0.6f, 0.6f, 0.5f, 0.2f), 0, 0);
    }
}
