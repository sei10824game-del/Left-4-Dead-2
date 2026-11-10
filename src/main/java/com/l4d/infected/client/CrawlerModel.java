package com.l4d.infected.client;

import com.l4d.infected.entity.CrawlerInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: crawler.json. Torso flat to the ground, arms haul it forward, stumps trail behind. */
public class CrawlerModel extends CommonModel<CrawlerInfected> {
    public CrawlerModel(ModelPart root) { super(root, "crawler"); }

    @Override
    protected void locomotion() {
        float s = Math.max(limbAmt, runAmt * 0.5f), c = limb * 0.9f, br = sin(age * 0.1f);
        drop(9f); body.xRot = 1.4f + 0.04f * br; body.yRot = sin(c) * 0.25f * s;
        head.xRot = lookX - 1.2f; head.yRot = lookY;
        ra.xRot = -2.2f + sin(c) * 0.8f * s; la.xRot = -2.2f - sin(c) * 0.8f * s; ra.zRot = 0.3f; la.zRot = -0.3f;
        rf.xRot = -0.3f + Math.max(0, cos(c)) * -0.6f * s; lf.xRot = -0.3f + Math.max(0, -cos(c)) * -0.6f * s;
        rl.xRot = -1.4f; ll.xRot = -1.4f; rl.yRot = sin(c) * 0.3f * s; ll.yRot = sin(c) * 0.3f * s;
    }

    @Override
    protected void claw() {
        float p = ent.actionProgress(pt), h = (float) (ent.actionLen() * 5 / 12) / Math.max(1, ent.actionLen());
        boolean left = ent.alt(); ModelPart a = left ? la : ra;
        float b = bump(p, h - 0.2f, h, h + 0.25f);
        body.xRot += -0.35f * b;                                               // rears up then rakes down
        a.xRot = lerp(a.xRot, keys(p, new float[]{0, h - .15f, h, h + .2f, 1}, new float[]{-2.2f, -3.0f, -1.4f, -1.6f, -2.2f}), 1);
        head.xRot += 0.4f * b; part("jaw").xRot = 0.8f * b;
    }
}
