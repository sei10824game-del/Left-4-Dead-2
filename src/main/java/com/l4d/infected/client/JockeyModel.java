package com.l4d.infected.client;

import com.l4d.infected.entity.JockeyInfected;
import net.minecraft.client.model.geom.ModelPart;

import static com.l4d.infected.client.Anim.*;
import static com.l4d.infected.entity.JockeyInfected.*;

/** model spec: assets/l4dinfected/models/entity/jockey.json */
public class JockeyModel extends CrouchModel<JockeyInfected> {
    public JockeyModel(ModelPart root) { super(root, "jockey", 3, 3); }

    private void jaw(float o) { part("jaw").xRot = o; }
    private void ears(float z) { part("right_ear").zRot = -z; part("left_ear").zRot = z; }

    @Override
    protected void gait() {
        float s = limbAmt, c = limb * 0.9f, br = sin(age * 0.2f);
        if (!ent.onGround()) { // hopping: tucked then kicked out
            pitch(0.9f); stance(-0.4f, 1.6f, -0.5f, 1.7f, 0.5f);
            arm("right", -1.2f, 0.7f, -0.4f, -0.3f); arm("left", -1.2f, 0.7f, -0.4f, -0.3f);
            head(-0.2f); jaw(0.6f); ears(0.5f); return;
        }
        float g = sin(c);
        pitch(0.9f + 0.05f * br + Math.abs(g) * 0.1f * s);
        stance(-0.8f + g * 0.5f * s, 1.5f, -0.8f - g * 0.5f * s, 1.5f, Math.abs(g) * 0.4f * s);
        arm("right", -0.4f + g * 0.8f * s, 0.2f, -0.6f, -0.4f);
        arm("left", -0.4f - g * 0.8f * s, 0.2f, -0.6f, -0.4f);
        head(0.1f + 0.1f * sin(age * 0.6f)); part("head").zRot = sin(age * 0.17f) * 0.2f;
        jaw(0.15f + Math.max(0, sin(age * 0.5f)) * 0.3f * (1 - s)); ears(0.15f * br);
    }

    @Override
    protected void claw() {
        float p = ent.actionProgress(pt), h = clawHit();
        boolean left = ent.alt(); String a = left ? "left" : "right";
        pitch(beat(p, h, 0.9f, 0.5f, 1.3f, 1.35f));
        part("body").yRot = (left ? -1 : 1) * beat(p, h, 0, 0.4f, -0.6f, -0.5f);
        stance(-0.9f, 1.6f, -0.9f, 1.6f, 0);
        arm(a, beat(p, h, -0.4f, 0.8f, -2.4f, -2.2f), beat(p, h, 0.2f, 0.7f, -0.4f, -0.2f), beat(p, h, -0.6f, -1.3f, -0.1f, -0.2f), -0.5f);
        arm(left ? "right" : "left", -0.8f, 0.4f, -0.6f, -0.4f);
        head(beat(p, h, 0, -0.3f, 0.3f, 0.2f)); jaw(beat(p, h, 0.2f, 0.9f, 0.1f, 0.5f)); ears(0.5f);
    }

    @Override
    protected void action(int id) {
        float t = ent.actionTime(pt), p = ent.actionProgress(pt);
        switch (id) {
            case CACKLE -> { // sink, shoulders heaving with laughter, head thrown back, hands wobbling at the cheeks, then spring
                float c = smooth(seg(p, 0, 0.6f)), sp = in(seg(p, 0.85f, 1)), sh = sin(age * 2.2f) * c * (1 - sp);
                pitch(lerp(lerp(0.9f, 0.6f, c) + 0.06f * sh, 1.35f, sp));
                stance(lerp(-0.8f, -1.4f, c), lerp(1.5f, 2.4f, c), lerp(-0.8f, -1.4f, c), lerp(1.5f, 2.4f, c), 0);
                arm("right", lerp(-0.4f, -2.2f, c) + 0.2f * sh, 0.5f * c, -1.6f * c, -0.6f);
                arm("left", lerp(-0.4f, -2.2f, c) - 0.2f * sh, 0.5f * c, -1.6f * c, -0.6f);
                head(-0.5f * c + 0.1f * sh); part("head").zRot = 0.3f * sh; jaw(0.9f * c + 0.1f * sh); ears(0.9f * c);
            }
            case LEAP -> { // stretched, hands reaching for the head, legs trailing
                pitch(1.4f); leg("right", 0.9f, 0.5f); leg("left", 0.7f, 0.6f);
                arm("right", -2.6f, 0.3f, -0.1f, -0.4f); arm("left", -2.6f, 0.3f, -0.1f, -0.4f);
                head(-0.4f); jaw(1.0f); ears(-0.4f);
            }
            case RIDE -> { // legs clamped round the victim, hands hauling hair and ears in alternating yanks
                float u = (t % YANK) / YANK, r = sin(u * (float) Math.PI * 2), y = in(seg(u, 0.4f, 0.7f)) * (1 - seg(u, 0.7f, 1));
                pitch(0.55f + 0.1f * r);
                part("body").yRot = sin(age * 0.2f) * 0.2f; part("body").zRot = sin(age * 0.11f) * 0.12f;
                leg("right", -1.1f, 1.4f); leg("left", -1.1f, 1.4f);
                part("right_leg").zRot = 0.45f; part("left_leg").zRot = -0.45f;
                arm("right", lerp(-2.8f, -1.9f, y), 0.35f, -0.9f - 0.5f * y, -0.8f);
                arm("left", lerp(-1.9f, -2.8f, y), 0.35f, -1.4f + 0.5f * y, -0.8f);
                head(-0.3f + 0.2f * y); part("head").zRot = 0.25f * r; jaw(0.7f + 0.3f * y); ears(0.8f);
            }
            case STUN -> { // thrown off: tumbling flail then shaking itself
                float sh = 1 - smooth(seg(p, 0.5f, 1));
                pitch(-0.2f + 0.3f * (1 - sh)); stance(-0.5f, 1.2f, -0.4f, 1.0f, 0);
                arm("right", -2f + sin(t) * 0.6f * sh, 0.9f, -0.6f, 0); arm("left", -2f + cos(t) * 0.6f * sh, 0.9f, -0.6f, 0);
                head(-0.2f); part("head").yRot += sin(t * 1.5f) * 0.5f * sh; jaw(0.7f * sh); ears(0.9f);
            }
            default -> {}
        }
    }
}
