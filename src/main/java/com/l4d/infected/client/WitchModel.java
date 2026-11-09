package com.l4d.infected.client;

import com.l4d.infected.entity.WitchInfected;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import static com.l4d.infected.client.Anim.*;
import static com.l4d.infected.entity.WitchInfected.*;

/** model spec: assets/l4dinfected/models/entity/witch.json (built by tools/tex_witch.py). */
public class WitchModel extends InfectedModel<WitchInfected> {
    private final ModelPart body, head, rArm, lArm, rFore, lFore, rLeg, lLeg, rShin, lShin, hairBack, hairFront, hairL, hairR, veil, veilBack;

    public WitchModel(ModelPart root) {
        super(root, "witch");
        body = part("body"); head = part("head"); rArm = part("right_arm"); lArm = part("left_arm"); rFore = part("right_forearm");
        lFore = part("left_forearm"); rLeg = part("right_leg"); lLeg = part("left_leg"); rShin = part("right_shin"); lShin = part("left_shin");
        hairBack = part("hair_back"); hairFront = part("hair_front"); hairL = part("hair_l"); hairR = part("hair_r");
        veil = part("veil"); veilBack = part("veil_back");
    }

    @Override
    public void setupAnim(WitchInfected e, float ls, float la, float age, float yaw, float pitch) {
        super.setupAnim(e, ls, la, age, yaw, pitch);
        veil.visible = e.variant() == 1;
        // hair and veil hang with gravity against the head's pitch
        float g = body.xRot + head.xRot;
        hairBack.xRot -= g * 0.8f; hairFront.xRot -= g * 0.85f; veilBack.xRot -= g * 0.8f;
        hairL.zRot -= body.zRot; hairR.zRot -= body.zRot;
    }

    @Override
    protected void locomotion() {
        float s = limbAmt;
        switch (ent.mode()) {
            case ENRAGED -> sprint(s);
            case LEAVING -> shuffle(s);
            default -> sit(1);
        }
        if (ent.action() == RISE) sit(1 - smooth(seg(ent.actionTime(pt), 3, 12)));
    }

    /** curled on the ground: knees up, face buried in her hands, sob bursts heave the shoulders; startle lifts the head and shakes her. */
    private void sit(float w) {
        if (w <= 0) return;
        float st = ent.startle(), sob = Mth.square(Math.max(0, Mth.sin(age * 0.21f)));
        float shake = Mth.sin(age * 2.1f) * (0.015f + 0.1f * st) + sob * Mth.sin(age * 1.4f) * 0.05f;
        body.y = lerp(body.y, 22, w);
        body.xRot = lerp(body.xRot, 0.75f - 0.35f * st + sob * 0.08f, w);
        body.yRot = lerp(body.yRot, 0, w);
        body.zRot = lerp(body.zRot, shake, w);
        head.xRot = lerp(head.xRot, 0.35f - 0.95f * st, w);
        head.yRot = lerp(head.yRot, Mth.sin(age * 3.3f) * 0.08f * st, w);
        for (int i = 0; i < 2; i++) {
            float sg = i == 0 ? 1 : -1;
            ModelPart leg = i == 0 ? rLeg : lLeg, shin = i == 0 ? rShin : lShin, arm = i == 0 ? rArm : lArm, fore = i == 0 ? rFore : lFore;
            leg.y = lerp(leg.y, 22, w);
            leg.xRot = lerp(leg.xRot, -1.9f, w);
            leg.zRot = lerp(leg.zRot, -0.2f * sg, w);
            shin.xRot = lerp(shin.xRot, 2.25f, w);
            arm.xRot = lerp(arm.xRot, -1.0f + 0.6f * st, w);
            arm.yRot = lerp(arm.yRot, 0, w);
            arm.zRot = lerp(arm.zRot, -0.25f * sg + shake, w);
            arm.y -= sob * 0.7f * w;
            fore.xRot = lerp(fore.xRot, -2.6f + 1.6f * st, w);
        }
    }

    /** enraged: low forward-pitched sprint, splayed clawing arms flailing, knees kicking high, hair streaming. */
    private void sprint(float s) {
        float c = limb * 0.9f, br = Mth.sin(age * 0.5f);
        body.xRot = 0.35f + 0.4f * s + 0.03f * br;
        body.yRot = Mth.cos(c) * 0.25f * s;
        body.y += Math.abs(Mth.sin(c)) * -1.2f * s;
        head.xRot += -0.3f - 0.4f * s;
        head.yRot -= body.yRot;
        rLeg.xRot = Mth.cos(c) * 1.3f * s; lLeg.xRot = -Mth.cos(c) * 1.3f * s;
        rShin.xRot = (0.2f + Math.max(0, Mth.sin(c)) * 1.5f) * s;
        lShin.xRot = (0.2f + Math.max(0, -Mth.sin(c)) * 1.5f) * s;
        rArm.xRot = -0.7f - Mth.cos(c) * 1.1f * s; lArm.xRot = -0.7f + Mth.cos(c) * 1.1f * s;
        rArm.zRot = 0.45f + 0.15f * s + 0.05f * br; lArm.zRot = -0.45f - 0.15f * s - 0.05f * br;
        rFore.xRot = lFore.xRot = -0.6f + 0.1f * br;
        hairBack.xRot += 0.5f * s + 0.12f * Mth.sin(age * 0.9f) * s;
        veilBack.xRot += 0.7f * s + 0.15f * Mth.sin(age * 0.8f) * s;
        hairL.zRot -= 0.2f * s; hairR.zRot += 0.2f * s;
    }

    /** leaving the scene: slow hunched shuffle, one hand pressed to her face, the other dragging; sobs jolt the torso. */
    private void shuffle(float s) {
        float c = limb * 0.5f, sob = Mth.square(Math.max(0, Mth.sin(age * 0.23f)));
        body.xRot = 0.5f + sob * 0.1f;
        body.zRot = sob * Mth.sin(age * 1.4f) * 0.05f;
        head.xRot = 0.45f; head.yRot = 0;
        rLeg.xRot = Mth.cos(c) * 0.5f * s; lLeg.xRot = -Mth.cos(c) * 0.5f * s;
        rShin.xRot = Math.max(0, Mth.sin(c)) * 0.5f * s; lShin.xRot = Math.max(0, -Mth.sin(c)) * 0.5f * s;
        rArm.xRot = -1.5f; rArm.yRot = -0.35f; rFore.xRot = -1.6f;
        lArm.xRot = 0.1f + Mth.cos(c) * 0.2f * s; lArm.zRot = -0.12f;
        rArm.y -= sob * 0.6f;
    }

    @Override
    protected void action(int id) {
        float t = ent.actionTime(pt);
        if (id == RISE) {
            // spring up out of the crouch, arch back and scream with arms flung wide, then drop into the hunting crouch
            float sc = bump(t, 2, 6, RISE_LEN), shake = sc * Mth.sin(t * 3f) * 0.06f;
            head.xRot += -1.0f * sc + shake;
            body.xRot += -0.55f * sc;
            body.y -= 1.5f * bump(t, 2, 5, 11);
            rArm.xRot += -0.6f * sc; lArm.xRot += -0.6f * sc;
            rArm.zRot += 1.1f * sc; lArm.zRot -= 1.1f * sc;
            rFore.xRot += -0.5f * sc + shake; lFore.xRot += -0.5f * sc - shake;
        } else if (id == RAKE) rake(t);
    }

    /** one claw rake: cocked high and wide, ripped down diagonally across the victim with a head lunge, claws trailing. */
    private void rake(float t) {
        boolean left = ent.alt();
        float sg = left ? -1 : 1;
        ModelPart arm = left ? lArm : rArm, fore = left ? lFore : rFore, off = left ? rArm : lArm;
        float[] ts = {0, 3, RAKE_HIT, RAKE_HIT + 2, RAKE_LEN};
        arm.xRot = keys(t, ts, new float[]{arm.xRot, -2.8f, -0.7f, 0.3f, arm.xRot});
        arm.zRot = keys(t, ts, new float[]{arm.zRot, 0.8f * sg, -0.45f * sg, -0.6f * sg, arm.zRot});
        arm.yRot = keys(t, ts, new float[]{arm.yRot, 0.3f * sg, -0.5f * sg, -0.6f * sg, arm.yRot});
        fore.xRot += keys(t, ts, new float[]{0, -0.7f, 0.1f, 0.2f, 0});
        off.xRot = lerp(off.xRot, -1.2f, bump(t, 0, 3, RAKE_LEN));
        body.yRot += keys(t, ts, new float[]{0, 0.4f * sg, -0.45f * sg, -0.5f * sg, 0});
        body.xRot += keys(t, ts, new float[]{0, -0.15f, 0.3f, 0.3f, 0});
        head.xRot += keys(t, ts, new float[]{0, -0.25f, 0.25f, 0.2f, 0});
        head.z -= 2.5f * bump(t, 2, RAKE_HIT, RAKE_LEN);
    }
}
