package com.l4d.infected.client;

import com.l4d.infected.L4D;
import com.l4d.infected.entity.Infected;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class InfectedRenderer<T extends Infected> extends MobRenderer<T, InfectedModel<T>> {
    private final Function<T, ResourceLocation> tex;
    private final float scale;

    /** texture(s): textures/entity/NAME.png (variant: NAME_N.png when variants>0); optional glowing NAME_eyes.png */
    public InfectedRenderer(EntityRendererProvider.Context c, InfectedModel<T> m, String name, float shadow, float scale, int variants) {
        super(c, m, shadow);
        this.scale = scale;
        ResourceLocation[] t = new ResourceLocation[Math.max(1, variants)];
        for (int i = 0; i < t.length; i++)
            t[i] = new ResourceLocation(L4D.ID, "textures/entity/" + name + (variants > 0 ? "_" + i : "") + ".png");
        tex = e -> t[Math.floorMod(e.variant(), t.length)];
        ResourceLocation eyes = new ResourceLocation(L4D.ID, "textures/entity/" + name + "_eyes.png");
        if (Minecraft.getInstance().getResourceManager().getResource(eyes).isPresent())
            addLayer(new RenderLayer<>(this) {
                @Override
                public void render(PoseStack ps, MultiBufferSource b, int light, T e, float ls, float la, float pt, float age, float yaw, float pitch) {
                    VertexConsumer vc = b.getBuffer(RenderType.eyes(eyes));
                    getParentModel().renderToBuffer(ps, vc, 0xF00000, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
                }
            });
    }

    @Override public ResourceLocation getTextureLocation(T e) { return tex.apply(e); }
    @Override protected void scale(T e, PoseStack ps, float pt) { ps.scale(scale, scale, scale); }
}
