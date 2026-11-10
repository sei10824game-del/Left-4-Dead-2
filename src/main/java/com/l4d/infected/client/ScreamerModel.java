package com.l4d.infected.client;

import com.l4d.infected.entity.ScreamerInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: screamer.json. Arms are strapped crossed over the chest; the head does all the acting. */
public class ScreamerModel extends CommonModel<ScreamerInfected> {
    public ScreamerModel(ModelPart root) { super(root, "screamer"); }

    private void bind() {
        ra.xRot = -0.9f; la.xRot = -0.9f; ra.yRot = 0.9f; la.yRot = -0.9f; ra.zRot = 0; la.zRot = 0; rf.xRot = -1.0f; lf.xRot = -1.0f;
    }

    @Override protected void locomotion() { super.locomotion(); bind(); head.zRot += sin(age * 0.9f) * 0.15f; body.yRot += sin(limb * 0.66f) * 0.25f * limbAmt; }

    @Override
    protected void action(int id) {
        if (id != ScreamerInfected.SCREAM) { super.action(id); return; }
        float p = ent.actionProgress(pt), t = ent.actionTime(pt);
        float in = smooth(seg(p, 0, 0.25f)), out = smooth(seg(p, 0.8f, 1)), w = in * (1 - out), sh = sin(t * 2.5f) * w;
        bind();
        body.xRot = lerp(body.xRot, -0.45f, w); body.zRot = 0.06f * sh;      // arches back, shuddering
        head.xRot = lerp(head.xRot, -0.95f, w); head.zRot = 0.08f * sh;
        part("jaw").xRot = lerp(part("jaw").xRot, 1.1f + 0.1f * sh, w);
        rl.xRot = 0.1f * sh; ll.xRot = -0.1f * sh;
        drop(-0.3f * w * Math.abs(sh));
    }
}
