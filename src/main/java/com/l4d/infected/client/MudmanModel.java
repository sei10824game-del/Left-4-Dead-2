package com.l4d.infected.client;

import com.l4d.infected.entity.MudmanInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;

/** model spec: mudman.json (tools/tex_uncommon.py) */
public class MudmanModel extends CommonModel<MudmanInfected> {
    public MudmanModel(ModelPart root) { super(root, "mudman"); }

    @Override protected void locomotion() {
        super.locomotion();
        float idle = Math.max(0, 1 - runAmt * 4 - limbAmt * 3);   // ambush: hugs the ground, arms planted ahead, head up watching
        drop(idle * 7f);
        to(body, idle, 1.25f, 0, 0); head.xRot -= idle * 1.0f;
        to(ra, idle, -1.5f, 0, 0.3f); to(la, idle, -1.5f, 0, -0.3f); to(rf, idle, -0.3f, 0, 0); to(lf, idle, -0.3f, 0, 0);
        to(rl, idle, -1.3f, 0, 0.3f); to(ll, idle, -1.3f, 0, -0.3f); to(rs, idle, 2.2f, 0, 0); to(ls, idle, 2.2f, 0, 0);
        head.zRot += idle * sin(age * 0.07f) * 0.12f;
    }
}
