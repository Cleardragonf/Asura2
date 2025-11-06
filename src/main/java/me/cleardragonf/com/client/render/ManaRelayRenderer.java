package me.cleardragonf.com.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.blockentity.ManaRelayBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Axis;

public class ManaRelayRenderer implements BlockEntityRenderer<ManaRelayBlockEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/block/mana_relay.png");

    public ManaRelayRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ManaRelayBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;

        long t = be.getLevel().getGameTime();
        float time = (t + partialTicks) % 3600;
        float rot = (time * 6.0f) % 360f; // 360 degrees every 10s
        float bob = (float) Math.sin(time / 10.0f) * 0.06f + 0.10f;

        pose.pushPose();
        pose.translate(0.5, 0.5 + bob, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(rot));

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEX));

        // Draw a smaller cube centered at origin; map the full texture per face
        float s = 0.25f; // half-size (cube from -s..s)
        float x0 = -s, x1 = s, y0 = -s, y1 = s, z0 = -s, z1 = s;

        // Full-face UVs
        float u0 = 0f, v0 = 0f, u1 = 1f, v1 = 1f;

        // UP (+Y)
        quad(vc, pose,
                x0, y1, z1,  x1, y1, z1,  x1, y1, z0,  x0, y1, z0,
                u0, v0, u1, v1,
                0, 1, 0, packedLight, packedOverlay);
        // DOWN (-Y)
        quad(vc, pose,
                x0, y0, z0,  x1, y0, z0,  x1, y0, z1,  x0, y0, z1,
                u0, v0, u1, v1,
                0, -1, 0, packedLight, packedOverlay);
        // NORTH (-Z)
        quad(vc, pose,
                x1, y0, z0,  x0, y0, z0,  x0, y1, z0,  x1, y1, z0,
                u0, v0, u1, v1,
                0, 0, -1, packedLight, packedOverlay);
        // SOUTH (+Z)
        quad(vc, pose,
                x0, y0, z1,  x1, y0, z1,  x1, y1, z1,  x0, y1, z1,
                u0, v0, u1, v1,
                0, 0, 1, packedLight, packedOverlay);
        // WEST (-X)
        quad(vc, pose,
                x0, y0, z0,  x0, y0, z1,  x0, y1, z1,  x0, y1, z0,
                u0, v0, u1, v1,
                -1, 0, 0, packedLight, packedOverlay);
        // EAST (+X)
        quad(vc, pose,
                x1, y0, z1,  x1, y0, z0,  x1, y1, z0,  x1, y1, z1,
                u0, v0, u1, v1,
                1, 0, 0, packedLight, packedOverlay);

        pose.popPose();
    }

    private static void quad(VertexConsumer vc, PoseStack pose,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float x3, float y3, float z3,
                              float x4, float y4, float z4,
                              float u0, float v0, float u1, float v1,
                              float nx, float ny, float nz,
                              int light, int overlay) {
        var lp = pose.last();
        vc.addVertex(lp, x1, y1, z1).setColor(1f, 1f, 1f, 1f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        vc.addVertex(lp, x2, y2, z2).setColor(1f, 1f, 1f, 1f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        vc.addVertex(lp, x3, y3, z3).setColor(1f, 1f, 1f, 1f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        vc.addVertex(lp, x4, y4, z4).setColor(1f, 1f, 1f, 1f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
    }
}
