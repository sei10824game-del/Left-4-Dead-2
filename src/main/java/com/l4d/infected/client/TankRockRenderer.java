package com.l4d.infected.client;

import com.l4d.infected.entity.TankRock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Tumbling chunk of torn-up ground: a cluster of rotated stone / cobble / dirt blocks. */
public class TankRockRenderer extends EntityRenderer<TankRock> {
    private static final BlockState[] MAT = {Blocks.COBBLESTONE.defaultBlockState(), Blocks.STONE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState()};
    // x, y, z offset (block units of the cluster), scale, yaw/pitch tilt in degrees
    private static final float[][] CHUNKS = {{0, 0, 0, 1f, 0, 0}, {0.45f, 0.2f, 0.1f, 0.7f, 30, 20}, {-0.4f, 0.25f, -0.2f, 0.65f, -25, 40},
            {0.05f, -0.4f, 0.3f, 0.6f, 50, -15}, {-0.1f, 0.35f, 0.45f, 0.5f, 10, 60}};
    private final BlockRenderDispatcher blocks;

    public TankRockRenderer(EntityRendererProvider.Context c) { super(c); blocks = c.getBlockRenderDispatcher(); shadowRadius = 0.6f; }

    @Override
    public void render(TankRock e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        float spin = (e.tickCount + pt) * 18f;
        ps.pushPose();
        ps.translate(0, 0.5, 0);
        ps.mulPose(Axis.YP.rotationDegrees(e.getId() * 47 % 360));
        ps.mulPose(Axis.XP.rotationDegrees(spin));
        ps.mulPose(Axis.ZP.rotationDegrees(spin * 0.37f));
        ps.scale(0.85f, 0.85f, 0.85f);
        for (int i = 0; i < CHUNKS.length; i++) {
            float[] c = CHUNKS[i];
            ps.pushPose();
            ps.translate(c[0], c[1], c[2]);
            ps.mulPose(Axis.YP.rotationDegrees(c[4]));
            ps.mulPose(Axis.XP.rotationDegrees(c[5]));
            ps.scale(c[3], c[3], c[3]);
            ps.translate(-0.5, -0.5, -0.5);
            blocks.renderSingleBlock(MAT[i], ps, buf, light, OverlayTexture.NO_OVERLAY);
            ps.popPose();
        }
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, light);
    }

    @SuppressWarnings("deprecation")
    @Override public ResourceLocation getTextureLocation(TankRock e) { return TextureAtlas.LOCATION_BLOCKS; }
}
