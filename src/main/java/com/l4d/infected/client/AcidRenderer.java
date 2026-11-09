package com.l4d.infected.client;

import com.l4d.infected.L4D;
import com.l4d.infected.entity.AcidPool;
import com.l4d.infected.entity.AcidSpit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Glowing acid visuals: a camera-facing wobbling glob for {@link AcidSpit}, a flat swirling decal for {@link AcidPool}. */
public abstract class AcidRenderer<T extends Entity> extends EntityRenderer<T> {
    static final ResourceLocation SPIT = new ResourceLocation(L4D.ID, "textures/entity/acid_spit.png");
    static final ResourceLocation POOL = new ResourceLocation(L4D.ID, "textures/entity/acid_pool.png");

    protected AcidRenderer(EntityRendererProvider.Context c) { super(c); }

    /** double-sided textured quad in the local XZ plane (flat) or XY plane (billboard). */
    static void quad(PoseStack ps, VertexConsumer vc, float s, float a, boolean flat) {
        Matrix4f m = ps.last().pose(); Matrix3f n = ps.last().normal();
        float[][] c = {{-s, -s, 0, 1}, {-s, s, 0, 0}, {s, s, 1, 0}, {s, -s, 1, 1}};
        for (int side = 0; side < 2; side++)
            for (int i = 0; i < 4; i++) {
                float[] v = c[side == 0 ? i : 3 - i];
                vc.vertex(m, v[0], flat ? 0 : v[1], flat ? v[1] : 0).color(1f, 1f, 1f, a).uv(v[2], v[3])
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
            }
    }

    public static class Spit extends AcidRenderer<AcidSpit> {
        public Spit(EntityRendererProvider.Context c) { super(c); }
        @Override public ResourceLocation getTextureLocation(AcidSpit e) { return SPIT; }
        @Override
        public void render(AcidSpit e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.tickCount + pt;
            ps.pushPose();
            ps.mulPose(entityRenderDispatcher.cameraOrientation());
            ps.mulPose(Axis.ZP.rotationDegrees(age * 25));
            ps.scale(1 + 0.15f * (float) Math.sin(age * 1.3f), 1 - 0.12f * (float) Math.sin(age * 1.3f), 1); // jelly wobble
            quad(ps, buf.getBuffer(RenderType.entityTranslucentEmissive(SPIT)), 0.32f, 1f, false);
            ps.popPose();
            super.render(e, yaw, pt, ps, buf, light);
        }
    }

    public static class Pool extends AcidRenderer<AcidPool> {
        public Pool(EntityRendererProvider.Context c) { super(c); }
        @Override public ResourceLocation getTextureLocation(AcidPool e) { return POOL; }
        @Override
        public void render(AcidPool e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.tickCount + pt, r = e.radius();
            float a = 0.9f * Math.min(1, age / 6) * Math.max(0, Math.min(1, (e.life() - age) / 25));
            float spread = 0.55f + 0.45f * Anim.out(Math.min(1, age / 10));
            VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(POOL));
            ps.pushPose();
            ps.translate(0, 0.03, 0);
            ps.mulPose(Axis.YP.rotationDegrees(age * 0.8f));
            quad(ps, vc, r * spread, a, true);
            ps.translate(0, 0.012, 0);
            ps.mulPose(Axis.YP.rotationDegrees(-age * 2.1f + 70));
            quad(ps, vc, r * spread * (0.72f + 0.05f * (float) Math.sin(age * 0.2f)), a * 0.7f, true); // churning inner layer
            ps.popPose();
            super.render(e, yaw, pt, ps, buf, light);
        }
    }
}
