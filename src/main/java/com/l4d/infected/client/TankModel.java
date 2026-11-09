package com.l4d.infected.client;

import com.l4d.infected.entity.TankInfected;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import static com.l4d.infected.client.Anim.*;
import static com.l4d.infected.entity.TankInfected.*;

/**
 * model spec: assets/l4dinfected/models/entity/tank.json (built by tools/tex_tank.py).
 * Rest pose: torso pitched forward {@link #L}, head/arms counter-rotated so the arms hang. Everything below adds to it.
 */
public class TankModel extends InfectedModel<TankInfected> {
    static final float L = 18 * Mth.DEG_TO_RAD;
    private final ModelPart body, head, jaw, rArm, rFore, lArm, lFore, rLeg, lLeg, rock;

    public TankModel(ModelPart root) {
        super(root, "tank");
        body = part("body"); head = part("head"); jaw = part("jaw"); rArm = part("right_arm"); rFore = part("right_forearm");
        lArm = part("left_arm"); lFore = part("left_forearm"); rLeg = part("right_leg"); lLeg = part("left_leg"); rock = part("rock");
    }

    @Override
    public void setupAnim(TankInfected e, float ls, float la, float age, float yaw, float pitch) {
        super.setupAnim(e, ls, la, age, yaw, pitch);
        head.xRot -= L; // base class overwrote head pitch
        rock.visible = e.action() == THROW && e.actionTick >= 9 && e.actionTick < THROW_REL;
        if (e.deathTime > 0) death(e.deathTime + pt);
    }

    /** heavy knuckle-swinging lumber: slow cadence, side-to-side sway, weight dropping on every footfall, panting jaw. */
    @Override
    protected void locomotion() {
        float s = limbAmt, c = limb * 0.42f, br = Mth.sin(age * 0.07f), fall = Math.abs(Mth.cos(c)) * s;
        body.xRot += 0.12f * s + 0.03f * br;
        body.yRot += Mth.sin(c) * 0.14f * s;
        body.zRot += Mth.sin(c) * 0.07f * s;
        body.y += fall * 1.2f;
        head.xRot += 0.06f * fall - 0.03f * br;
        jaw.xRot += 0.12f + 0.1f * Math.max(0, br) + 0.15f * s;
        rLeg.xRot += Mth.cos(c) * 0.75f * s;
        lLeg.xRot -= Mth.cos(c) * 0.75f * s;
        rArm.xRot += -Mth.cos(c) * 0.5f * s - 0.02f * br;
        lArm.xRot += Mth.cos(c) * 0.5f * s - 0.02f * br;
        rFore.xRot += -0.15f - 0.25f * s * Math.max(0, -Mth.cos(c));
        lFore.xRot += -0.15f - 0.25f * s * Math.max(0, Mth.cos(c));
        rArm.zRot += 0.07f + 0.03f * br; lArm.zRot -= 0.07f + 0.03f * br;
    }

    @Override
    protected void action(int id) {
        float t = ent.actionTime(pt);
        switch (id) {
            case HAYMAKER -> haymaker(t);
            case HOOK -> hook(t);
            case THROW -> rockThrow(t);
            case SLAM -> slam(t);
            case LEAP -> leap(t);
            default -> {}
        }
    }

    /** overhand right haymaker: rear back + twist, arm cocked behind the head, chop down through the target, stagger forward. */
    private void haymaker(float t) {
        float[] ts = {0, 12, HAY_HIT - 2, HAY_HIT, HAY_HIT + 4, HAY_LEN};
        rArm.xRot += keys(t, ts, new float[]{0, -3.0f, -3.2f, -1.0f, -0.1f, 0});
        rArm.zRot += keys(t, ts, new float[]{0, 0.35f, 0.4f, -0.2f, -0.25f, 0});
        rFore.xRot += keys(t, ts, new float[]{0, -1.1f, -1.3f, -0.1f, 0, 0});
        lArm.xRot += keys(t, ts, new float[]{0, -1.0f, -1.1f, 0.4f, 0.3f, 0});
        lArm.zRot += keys(t, ts, new float[]{0, -0.3f, -0.3f, -0.1f, 0, 0});
        body.xRot += keys(t, ts, new float[]{0, -0.3f, -0.35f, 0.45f, 0.5f, 0});
        body.yRot += keys(t, ts, new float[]{0, 0.55f, 0.62f, -0.45f, -0.55f, 0});
        body.y += keys(t, ts, new float[]{0, -0.5f, -1f, 2f, 2.5f, 0});
        head.yRot -= keys(t, ts, new float[]{0, 0.4f, 0.45f, -0.3f, -0.35f, 0});
        head.xRot += keys(t, ts, new float[]{0, -0.2f, -0.25f, 0.2f, 0.2f, 0});
        jaw.xRot += keys(t, ts, new float[]{0, 0.7f, 0.8f, 0.9f, 0.4f, 0});
        rLeg.xRot += keys(t, ts, new float[]{0, 0.35f, 0.4f, -0.45f, -0.5f, 0});
        lLeg.xRot += keys(t, ts, new float[]{0, -0.2f, -0.2f, 0.3f, 0.35f, 0});
    }

    /** quick left hook: short coil out to the side, whip across the front, snap back to guard. */
    private void hook(float t) {
        float[] ts = {0, 5, HOOK_HIT, HOOK_HIT + 3, HOOK_LEN};
        lArm.xRot += keys(t, ts, new float[]{0, -1.1f, -1.4f, -1.3f, 0});
        lArm.yRot += keys(t, ts, new float[]{0, -0.9f, 0.5f, 0.8f, 0});
        lArm.zRot += keys(t, ts, new float[]{0, -0.5f, -0.2f, 0, 0});
        lFore.xRot += keys(t, ts, new float[]{0, -0.9f, -0.6f, -0.5f, 0});
        body.yRot += keys(t, ts, new float[]{0, -0.45f, 0.45f, 0.55f, 0});
        body.xRot += keys(t, ts, new float[]{0, -0.05f, 0.15f, 0.15f, 0});
        head.yRot += keys(t, ts, new float[]{0, 0.3f, -0.3f, -0.35f, 0});
        rArm.xRot += keys(t, ts, new float[]{0, -0.4f, 0.3f, 0.3f, 0});
        jaw.xRot += keys(t, ts, new float[]{0, 0.3f, 0.6f, 0.4f, 0});
        lLeg.xRot += keys(t, ts, new float[]{0, 0.2f, -0.3f, -0.3f, 0});
    }

    /** crouch, both fists dig in and tear up a slab, heave it overhead, cock back behind the head, hurl, follow through. */
    private void rockThrow(float t) {
        float[] ts = {0, 8, 16, 28, 35, THROW_REL, THROW_REL + 4, THROW_LEN};
        float arms = keys(t, ts, new float[]{0, -0.8f, -0.7f, -2.9f, -3.2f, -1.2f, -0.6f, 0});
        rArm.xRot += arms; lArm.xRot += arms;
        float in = keys(t, ts, new float[]{0, 0.2f, 0.3f, 0.35f, 0.3f, 0, 0, 0});
        rArm.zRot -= in; lArm.zRot += in; // fists converge on the slab
        rArm.yRot += in * 0.6f; lArm.yRot -= in * 0.6f;
        float fore = keys(t, ts, new float[]{0, -0.2f, -0.5f, -0.5f, -0.3f, -0.1f, 0, 0});
        rFore.xRot += fore; lFore.xRot += fore;
        body.xRot += keys(t, ts, new float[]{0, 0.75f, 0.8f, -0.3f, -0.5f, 0.5f, 0.55f, 0});
        body.y += keys(t, ts, new float[]{0, 2.5f, 3f, -0.5f, -1f, 1.5f, 1.5f, 0});
        head.xRot += keys(t, ts, new float[]{0, -0.6f, -0.6f, -0.3f, -0.4f, 0.2f, 0.2f, 0});
        jaw.xRot += keys(t, ts, new float[]{0, 0.3f, 0.5f, 0.9f, 0.6f, 0.9f, 0.5f, 0});
        rLeg.xRot += keys(t, ts, new float[]{0, -0.45f, -0.45f, 0.1f, 0.3f, -0.4f, -0.4f, 0});
        lLeg.xRot += keys(t, ts, new float[]{0, 0.3f, 0.3f, 0, -0.1f, 0.35f, 0.35f, 0});
        // the slab rides between the fists (body space)
        float[] rt = {9, 16, 28, 35, THROW_REL};
        rock.y = keys(t, rt, new float[]{6, -2, -47, -41, -40});
        rock.z = keys(t, rt, new float[]{-15, -15, 4, 16, -14});
        rock.xRot = keys(t, rt, new float[]{0, 0.3f, -0.4f, -0.9f, 0.5f});
        float shake = bump(t, 8, 12, 17) * Mth.sin(t * 2.6f);
        body.zRot += 0.03f * shake;
        rock.zRot = 0.1f * shake;
    }

    /** both fists raised overhead with a roar, hang, then slam the ground; body quakes through the impact. */
    private void slam(float t) {
        float[] ts = {0, 12, SLAM_HIT - 3, SLAM_HIT, SLAM_HIT + 7, SLAM_LEN};
        float arms = keys(t, ts, new float[]{0, -3.0f, -3.3f, -0.9f, -0.9f, 0});
        rArm.xRot += arms; lArm.xRot += arms;
        rArm.zRot -= keys(t, ts, new float[]{0, 0.35f, 0.4f, 0.1f, 0.1f, 0});
        lArm.zRot += keys(t, ts, new float[]{0, 0.35f, 0.4f, 0.1f, 0.1f, 0});
        float fore = keys(t, ts, new float[]{0, -0.6f, -0.7f, 0, 0, 0});
        rFore.xRot += fore; lFore.xRot += fore;
        body.xRot += keys(t, ts, new float[]{0, -0.35f, -0.4f, 0.85f, 0.85f, 0});
        body.y += keys(t, ts, new float[]{0, -1f, -1.5f, 3f, 3f, 0});
        head.xRot += keys(t, ts, new float[]{0, -0.5f, -0.6f, 0.1f, 0.1f, 0});
        jaw.xRot += keys(t, ts, new float[]{0, 0.9f, 1.0f, 0.5f, 0.3f, 0});
        float q = bump(t, SLAM_HIT, SLAM_HIT + 1, SLAM_HIT + 8) * Mth.sin(t * 3.1f);
        body.zRot += 0.05f * q; body.xRot += 0.03f * q;
        rLeg.xRot += keys(t, ts, new float[]{0, 0.1f, 0.1f, -0.35f, -0.35f, 0});
        lLeg.xRot += keys(t, ts, new float[]{0, 0.1f, 0.1f, 0.25f, 0.25f, 0});
    }

    /** sink, explode upward with arms flung up, then reach forward to grab the ledge. */
    private void leap(float t) {
        float[] ts = {0, LEAP_GO, LEAP_GO + 3, 13, LEAP_LEN};
        body.xRot += keys(t, ts, new float[]{0, 0.6f, -0.2f, 0.3f, 0});
        body.y += keys(t, ts, new float[]{0, 3f, -1f, 0, 0});
        float arms = keys(t, ts, new float[]{0, 0.7f, -2.6f, -1.6f, 0});
        rArm.xRot += arms; lArm.xRot += arms;
        float legs = keys(t, ts, new float[]{0, -0.3f, 0.5f, -0.2f, 0});
        rLeg.xRot += legs; lLeg.xRot += legs * 0.7f;
        jaw.xRot += keys(t, ts, new float[]{0, 0.2f, 0.8f, 0.4f, 0});
    }

    /** death: knees give, it sits back hard, then folds forward over its fists with a dying shudder. */
    private void death(float d) {
        float k = smooth(seg(d, 0, 14)), f = smooth(seg(d, 12, 26)), sh = (1 - seg(d, 10, 45)) * Mth.sin(d * 2.4f);
        for (ModelPart leg : new ModelPart[]{rLeg, lLeg}) { leg.y += 8 * k; leg.xRot = -1.45f * k; leg.yRot = (leg == rLeg ? 0.25f : -0.25f) * k; }
        body.y = 8 + 8 * k;
        body.xRot = L + 0.2f * k + 0.95f * f + 0.03f * sh;
        body.yRot = 0; body.zRot = 0.04f * sh + 0.08f * f;
        rArm.xRot = -L - 0.3f * k - 0.6f * f; lArm.xRot = -L - 0.2f * k - 0.7f * f;
        rArm.zRot = 0.3f * k; lArm.zRot = -0.25f * k;
        rFore.xRot = -0.2f * f; lFore.xRot = -0.3f * f;
        head.xRot = -L + 0.6f * f; head.yRot = 0.3f * f;
        jaw.xRot = 0.5f * k;
        rock.visible = false;
    }

    /** cancels the renderer's 90 degree death flip: the tank slumps in place instead of tipping over. */
    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        if (ent != null && ent.deathTime > 0) {
            float f = Math.min(1, Mth.sqrt((ent.deathTime + pt - 1) / 20f * 1.6f));
            ps.pushPose();
            ps.translate(0, 1.501, 0);
            ps.mulPose(Axis.ZP.rotationDegrees(-90 * f));
            ps.translate(0, -1.501, 0);
            super.renderToBuffer(ps, vc, light, overlay, r, g, b, a);
            ps.popPose();
        } else super.renderToBuffer(ps, vc, light, overlay, r, g, b, a);
    }
}
