package me.cleardragonf.com.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.block.ManaBatteryBlock;
import me.cleardragonf.com.blockentity.ManaBatteryBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ManaBatteryRenderer implements BlockEntityRenderer<ManaBatteryBlockEntity> {
    private static final ResourceLocation PANEL_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/block/mana_battery.png");

    public ManaBatteryRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ManaBatteryBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof ManaBatteryBlock)) return;

        // Only the controller renders the entire cluster as one unit
        if (!be.isController()) return;

        int minX = be.getMinX(), minY = be.getMinY(), minZ = be.getMinZ();
        int maxX = be.getMaxX(), maxY = be.getMaxY(), maxZ = be.getMaxZ();
        if (maxX < minX || maxY < minY || maxZ < minZ) return;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX));
        final float eps = 0.001f;

        int cx = be.getBlockPos().getX();
        int cy = be.getBlockPos().getY();
        int cz = be.getBlockPos().getZ();

        float x0 = (float)(minX - cx) + eps;
        float y0 = (float)(minY - cy) + eps;
        float z0 = (float)(minZ - cz) + eps;
        float x1 = (float)(maxX - cx + 1) - eps;
        float y1 = (float)(maxY - cy + 1) - eps;
        float z1 = (float)(maxZ - cz + 1) - eps;

        // Render the six faces of the whole cluster AABB, stretching the texture per face
        // NORTH (-Z)
        quadTinted(vc, pose, x1, y0, z0,  x0, y0, z0,  x0, y1, z0,  x1, y1, z0,
                0f, 0f, 1f, 1f, packedLight, packedOverlay, 0, 0, -1, be);
        // SOUTH (+Z)
        quadTinted(vc, pose, x0, y0, z1,  x1, y0, z1,  x1, y1, z1,  x0, y1, z1,
                0f, 0f, 1f, 1f, packedLight, packedOverlay, 0, 0, 1, be);
        // WEST (-X)
        quadTinted(vc, pose, x0, y0, z0,  x0, y0, z1,  x0, y1, z1,  x0, y1, z0,
                0f, 0f, 1f, 1f, packedLight, packedOverlay, -1, 0, 0, be);
        // EAST (+X)
        quadTinted(vc, pose, x1, y0, z1,  x1, y0, z0,  x1, y1, z0,  x1, y1, z1,
                0f, 0f, 1f, 1f, packedLight, packedOverlay, 1, 0, 0, be);
        // DOWN (-Y)
        quadTinted(vc, pose, x0, y0, z1,  x1, y0, z1,  x1, y0, z0,  x0, y0, z0,
                0f, 0f, 1f, 1f, packedLight, packedOverlay, 0, -1, 0, be);
        // UP (+Y)
        quadTinted(vc, pose, x0, y1, z0,  x1, y1, z0,  x1, y1, z1,  x0, y1, z1,
                0f, 0f, 1f, 1f, packedLight, packedOverlay, 0, 1, 0, be);
    }

    private static void quadTinted(VertexConsumer vc, PoseStack pose,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float x3, float y3, float z3,
                              float x4, float y4, float z4,
                              float u1, float v1, float u2, float v2,
                              int light, int overlay,
                              float nx, float ny, float nz,
                              ManaBatteryBlockEntity be) {
        float r=1f,g=1f,b=1f,a=0.85f;
        int cap = Math.max(1, be.getCapacity());
        float fill = Math.min(1f, be.getStored() / (float) cap);
        long t = System.currentTimeMillis();
        float pulse = (float)(Math.sin(t / 300.0) * 0.1 + 0.1);
        a = 0.55f + pulse;
        r = 0.25f + 0.35f * fill;
        g = 0.7f + 0.25f * fill;
        b = 1.0f;
        var lp = pose.last();
        vc.addVertex(lp, x1, y1, z1).setColor(r,g,b,a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        vc.addVertex(lp, x2, y2, z2).setColor(r,g,b,a).setUv(u2, v1).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        vc.addVertex(lp, x3, y3, z3).setColor(r,g,b,a).setUv(u2, v2).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
        vc.addVertex(lp, x4, y4, z4).setColor(r,g,b,a).setUv(u1, v2).setOverlay(overlay).setLight(light).setNormal(lp, nx, ny, nz);
    }
}

