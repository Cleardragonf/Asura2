package me.cleardragonf.com.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.blockentity.ManaRelayBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
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

        // Draw an 8x8x8 cube centered at origin using a sprite sheet 96x16 (six 16x16 tiles in a row)
        float s = 0.25f; // half-size (cube from -s..s)
        float x0 = -s, x1 = s, y0 = -s, y1 = s, z0 = -s, z1 = s;

        // UVs (pixels) mapped to [0,1] by dividing by 96x16
        float texW = 96f, texH = 16f;
        // segments (top, bottom, north, south, west, east)
        float[][] seg = new float[][]{
                {0, 0, 16, 16},   // UP
                {16, 0, 32, 16},  // DOWN
                {32, 0, 48, 16},  // NORTH
                {48, 0, 64, 16},  // SOUTH
                {64, 0, 80, 16},  // WEST
                {80, 0, 96, 16}   // EAST
        };

        // Matrices
        Matrix4f m4 = pose.last().pose();
        Matrix3f n3 = pose.last().normal();

        // UP (+Y)
        quad(vc, m4, n3,
                x0, y1, z1,  x1, y1, z1,  x1, y1, z0,  x0, y1, z0,
                seg[0][0]/texW, seg[0][1]/texH, seg[0][2]/texW, seg[0][3]/texH,
                0, 1, 0, packedLight, packedOverlay);
        // DOWN (-Y)
        quad(vc, m4, n3,
                x0, y0, z0,  x1, y0, z0,  x1, y0, z1,  x0, y0, z1,
                seg[1][0]/texW, seg[1][1]/texH, seg[1][2]/texW, seg[1][3]/texH,
                0, -1, 0, packedLight, packedOverlay);
        // NORTH (-Z)
        quad(vc, m4, n3,
                x1, y0, z0,  x0, y0, z0,  x0, y1, z0,  x1, y1, z0,
                seg[2][0]/texW, seg[2][1]/texH, seg[2][2]/texW, seg[2][3]/texH,
                0, 0, -1, packedLight, packedOverlay);
        // SOUTH (+Z)
        quad(vc, m4, n3,
                x0, y0, z1,  x1, y0, z1,  x1, y1, z1,  x0, y1, z1,
                seg[3][0]/texW, seg[3][1]/texH, seg[3][2]/texW, seg[3][3]/texH,
                0, 0, 1, packedLight, packedOverlay);
        // WEST (-X)
        quad(vc, m4, n3,
                x0, y0, z0,  x0, y0, z1,  x0, y1, z1,  x0, y1, z0,
                seg[4][0]/texW, seg[4][1]/texH, seg[4][2]/texW, seg[4][3]/texH,
                -1, 0, 0, packedLight, packedOverlay);
        // EAST (+X)
        quad(vc, m4, n3,
                x1, y0, z1,  x1, y0, z0,  x1, y1, z0,  x1, y1, z1,
                seg[5][0]/texW, seg[5][1]/texH, seg[5][2]/texW, seg[5][3]/texH,
                1, 0, 0, packedLight, packedOverlay);

        pose.popPose();
    }

    private static void quad(VertexConsumer vc, Matrix4f m4, Matrix3f n3,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float x3, float y3, float z3,
                              float x4, float y4, float z4,
                              float u0, float v0, float u1, float v1,
                              float nx, float ny, float nz,
                              int light, int overlay) {
        vc.vertex(m4, x1, y1, z1).color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(n3, nx, ny, nz).endVertex();
        vc.vertex(m4, x2, y2, z2).color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(n3, nx, ny, nz).endVertex();
        vc.vertex(m4, x3, y3, z3).color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(n3, nx, ny, nz).endVertex();
        vc.vertex(m4, x4, y4, z4).color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(n3, nx, ny, nz).endVertex();
    }
}
