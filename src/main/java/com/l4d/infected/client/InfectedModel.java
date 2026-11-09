package com.l4d.infected.client;

import com.l4d.infected.entity.Infected;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.l4d.infected.client.Anim.*;

/**
 * Data driven humanoid-ish model. Expected part names for the default pose: head, body, right_arm, left_arm,
 * right_leg, left_leg (missing ones are ignored). Subclasses override {@link #animate} / {@link #action}.
 */
public class InfectedModel<T extends Infected> extends EntityModel<T> {
    protected final ModelPart root;
    protected final Map<String, ModelPart> parts = new HashMap<>();
    private static final ModelPart DUMMY = new ModelPart(List.of(), Map.of());
    protected float pt, age, limb, limbAmt;
    protected T ent;

    public InfectedModel(ModelPart root, String spec) {
        this.root = root;
        for (JsonModel.Part p : JsonModel.ORDER.get(spec))
            parts.put(p.name(), (p.parent() == null ? root : parts.get(p.parent())).getChild(p.name()));
    }

    public ModelPart part(String n) { return parts.getOrDefault(n, DUMMY); }
    public boolean has(String n) { return parts.containsKey(n); }

    @Override
    public void setupAnim(T e, float limbSwing, float limbAmount, float ageInTicks, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        for (ModelPart p : parts.values()) { p.visible = true; }
        ent = e; age = ageInTicks; limb = limbSwing; limbAmt = Math.min(1f, limbAmount); pt = ageInTicks - e.tickCount;
        ModelPart head = part("head");
        head.yRot = headYaw * ((float) Math.PI / 180f);
        head.xRot = headPitch * ((float) Math.PI / 180f);
        locomotion();
        int a = e.action();
        if (a == Infected.CLAW) claw();
        else action(a);
    }

    /** override for specials: id >= 2. */
    protected void action(int id) {}

    /** zombie-like sprint: arms thrown forward, legs scissor, body leaning into the run, idle breathing. */
    protected void locomotion() {
        float s = limbAmt, c = limb * 0.6662f;
        float breathe = (float) Math.sin(age * 0.09f);
        part("body").xRot = 0.12f * s + 0.02f * breathe;
        part("body").yRot = (float) Math.cos(c) * 0.12f * s;
        part("head").xRot += 0.08f * s;
        part("right_leg").xRot = (float) Math.cos(c) * 1.5f * s;
        part("left_leg").xRot = (float) Math.cos(c + Math.PI) * 1.5f * s;
        part("right_arm").xRot = lerp(0.1f + 0.04f * breathe, -1.2f + (float) Math.cos(c) * 0.7f, s);
        part("left_arm").xRot = lerp(0.1f - 0.04f * breathe, -1.2f + (float) Math.cos(c + Math.PI) * 0.7f, s);
        part("right_arm").zRot = lerp(0.06f, 0.18f, s) + 0.02f * breathe;
        part("left_arm").zRot = lerp(-0.06f, -0.18f, s) - 0.02f * breathe;
    }

    /** Claw: coil back (anticipation), whip across the body, follow-through, recover. Alternates hands. */
    protected void claw() {
        float p = ent.actionProgress(pt);
        boolean left = ent.alt();
        float sgn = left ? -1 : 1;
        ModelPart arm = part(left ? "left_arm" : "right_arm"), off = part(left ? "right_arm" : "left_arm");
        float wind = smooth(seg(p, 0f, 0.38f));
        float strike = in(seg(p, 0.38f, 0.52f));
        float rec = smooth(seg(p, 0.55f, 1f));
        float k = (wind * (1 - strike) + strike * (1 - rec) + 0f);
        // arm: rear-up -> slash diagonally down across body -> relax
        float ax = lerp(lerp(-0.4f, -2.7f, wind), lerp(-2.7f, -0.7f, strike), strike);
        arm.xRot = lerp(ax, arm.xRot, rec);
        arm.zRot = sgn * lerp(lerp(0.2f, 0.9f, wind), lerp(0.9f, -0.7f, strike), strike) * (1 - rec) + arm.zRot * rec;
        arm.yRot = sgn * (-0.4f * wind + 0.9f * strike) * (1 - rec);
        // off arm: guards/balances
        off.xRot = lerp(off.xRot, -0.9f - 0.3f * wind, 0.8f * (1 - rec));
        // torso: lean back, then whip forward & twist
        part("body").xRot += (-0.28f * wind + 0.55f * strike) * (1 - rec);
        part("body").yRot += sgn * (0.35f * wind - 0.8f * strike) * (1 - rec);
        part("head").xRot += (-0.2f * wind + 0.3f * strike) * (1 - rec);
        part("head").yRot -= sgn * (0.3f * wind - 0.6f * strike) * (1 - rec);
        part("right_leg").xRot += 0.35f * strike * (1 - rec);
        part("left_leg").xRot -= 0.25f * strike * (1 - rec);
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
