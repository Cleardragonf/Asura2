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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ManaBatteryRenderer implements BlockEntityRenderer<ManaBatteryBlockEntity> {
    // Panel texture; can be replaced with a dedicated battery panel
    private static final ResourceLocation PANEL_TEX =
            ResourceLocation.fromNamespaceAndPath(Asura.MODID, "textures/block/mana_generator.png");

    public ManaBatteryRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ManaBatteryBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof ManaBatteryBlock)) return;

        int minX = be.getMinX(), minY = be.getMinY(), minZ = be.getMinZ();
        int maxX = be.getMaxX(), maxY = be.getMaxY(), maxZ = be.getMaxZ();
        if (maxX < minX || maxY < minY || maxZ < minZ) return; // bounds not initialized yet

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX));

        // Slight offset to avoid z-fighting with the block model
        final float eps = 0.001f;

        for (Direction dir : Direction.values()) {
            // Only draw on exposed faces: if neighbor is NOT a battery.
            var npos = be.getBlockPos().relative(dir);
            boolean exposed = !(be.getLevel().getBlockState(npos).getBlock() instanceof ManaBatteryBlock);
            if (!exposed) continue;

            // Compute UV segment for this block on the cluster face
            int uLen, vLen, u0, v0;
            float nx = 0, ny = 0, nz = 0;
            switch (dir) {
                case NORTH -> { // face at local z=0, U=X, V=Y
                    uLen = (maxX - minX + 1);
                    vLen = (maxY - minY + 1);
                    u0 = be.getBlockPos().getX() - minX;
                    v0 = be.getBlockPos().getY() - minY;
                    float u1 = u0 / (float) uLen, v1 = v0 / (float) vLen;
                    float u2 = (u0 + 1) / (float) uLen, v2 = (v0 + 1) / (float) vLen;
                    nx = 0; ny = 0; nz = -1;
                    quadTinted(vc, pose,
                            0+eps, 0+eps, 0+eps,
                            1-eps, 0+eps, 0+eps,
                            1-eps, 1-eps, 0+eps,
                            0+eps, 1-eps, 0+eps,
                            u1, v1, u2, v2,
                            packedLight, packedOverlay,
                            nx, ny, nz,
                            be);
                }
                case SOUTH -> { // face at local z=1, U=X, V=Y
                    uLen = (maxX - minX + 1);
                    vLen = (maxY - minY + 1);
                    u0 = be.getBlockPos().getX() - minX;
                    v0 = be.getBlockPos().getY() - minY;
                    float u1 = u0 / (float) uLen, v1 = v0 / (float) vLen;
                    float u2 = (u0 + 1) / (float) uLen, v2 = (v0 + 1) / (float) vLen;
                    nx = 0; ny = 0; nz = 1;
                    quadTinted(vc, pose,
                            0+eps, 0+eps, 1-eps,
                            0+eps, 1-eps, 1-eps,
                            1-eps, 1-eps, 1-eps,
                            1-eps, 0+eps, 1-eps,
                            u1, v1, u2, v2,
                            packedLight, packedOverlay,
                            nx, ny, nz,
                            be);
                }
                case WEST -> { // face at local x=0, U=Z, V=Y
                    uLen = (maxZ - minZ + 1);
                    vLen = (maxY - minY + 1);
                    u0 = be.getBlockPos().getZ() - minZ;
                    v0 = be.getBlockPos().getY() - minY;
                    float u1 = u0 / (float) uLen, v1 = v0 / (float) vLen;
                    float u2 = (u0 + 1) / (float) uLen, v2 = (v0 + 1) / (float) vLen;
                    nx = -1; ny = 0; nz = 0;
                    quadTinted(vc, pose,
                            0+eps, 0+eps, 1-eps,
                            0+eps, 0+eps, 0+eps,
                            0+eps, 1-eps, 0+eps,
                            0+eps, 1-eps, 1-eps,
                            u2, v1, u1, v2,
                            packedLight, packedOverlay,
                            nx, ny, nz,
                            be);
                }
                case EAST -> { // face at local x=1, U=Z, V=Y
                    uLen = (maxZ - minZ + 1);
                    vLen = (maxY - minY + 1);
                    u0 = be.getBlockPos().getZ() - minZ;
                    v0 = be.getBlockPos().getY() - minY;
                    float u1 = u0 / (float) uLen, v1 = v0 / (float) vLen;
                    float u2 = (u0 + 1) / (float) uLen, v2 = (v0 + 1) / (float) vLen;
                    nx = 1; ny = 0; nz = 0;
                    quadTinted(vc, pose,
                            1-eps, 0+eps, 0+eps,
                            1-eps, 0+eps, 1-eps,
                            1-eps, 1-eps, 1-eps,
                            1-eps, 1-eps, 0+eps,
                            u1, v1, u2, v2,
                            packedLight, packedOverlay,
                            nx, ny, nz,
                            be);
                }
                case DOWN -> { // face at local y=0, U=X, V=Z
                    uLen = (maxX - minX + 1);
                    vLen = (maxZ - minZ + 1);
                    u0 = be.getBlockPos().getX() - minX;
                    v0 = be.getBlockPos().getZ() - minZ;
                    float u1 = u0 / (float) uLen, v1 = v0 / (float) vLen;
                    float u2 = (u0 + 1) / (float) uLen, v2 = (v0 + 1) / (float) vLen;
                    nx = 0; ny = -1; nz = 0;
                    quadTinted(vc, pose,
                            0+eps, 0+eps, 1-eps,
                            1-eps, 0+eps, 1-eps,
                            1-eps, 0+eps, 0+eps,
                            0+eps, 0+eps, 0+eps,
                            u1, v1, u2, v2,
                            packedLight, packedOverlay,
                            nx, ny, nz,
                            be);
                }
                case UP -> { // face at local y=1, U=X, V=Z
                    uLen = (maxX - minX + 1);
                    vLen = (maxZ - minZ + 1);
                    u0 = be.getBlockPos().getX() - minX;
                    v0 = be.getBlockPos().getZ() - minZ;
                    float u1 = u0 / (float) uLen, v1 = v0 / (float) vLen;
                    float u2 = (u0 + 1) / (float) uLen, v2 = (v0 + 1) / (float) vLen;
                    nx = 0; ny = 1; nz = 0;
                    quadTinted(vc, pose,
                            0+eps, 1-eps, 0+eps,
                            1-eps, 1-eps, 0+eps,
                            1-eps, 1-eps, 1-eps,
                            0+eps, 1-eps, 1-eps,
                            u1, v1, u2, v2,
                            packedLight, packedOverlay,
                            nx, ny, nz,
                            be);
                }
            }
        }

        // Controller indicator: small central marker on exposed faces
        if (be.isController()) {
            float s = 0.25f; // marker size
            float u1 = 0.45f, v1 = 0.45f, u2 = 0.55f, v2 = 0.55f;
            for (Direction dir : Direction.values()) {
                var npos = be.getBlockPos().relative(dir);
                boolean exposed = !(be.getLevel().getBlockState(npos).getBlock() instanceof ManaBatteryBlock);
                if (!exposed) continue;
                switch (dir) {
                    case NORTH -> quadTinted(buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX)), pose,
                            0.5f - s/2, 0.5f - s/2, 0+0.0005f,
                            0.5f + s/2, 0.5f - s/2, 0+0.0005f,
                            0.5f + s/2, 0.5f + s/2, 0+0.0005f,
                            0.5f - s/2, 0.5f + s/2, 0+0.0005f,
                            u1, v1, u2, v2, packedLight, packedOverlay, 0,0,-1, be);
                    case SOUTH -> quadTinted(buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX)), pose,
                            0.5f - s/2, 0.5f - s/2, 1-0.0005f,
                            0.5f - s/2, 0.5f + s/2, 1-0.0005f,
                            0.5f + s/2, 0.5f + s/2, 1-0.0005f,
                            0.5f + s/2, 0.5f - s/2, 1-0.0005f,
                            u1, v1, u2, v2, packedLight, packedOverlay, 0,0,1, be);
                    case WEST -> quadTinted(buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX)), pose,
                            0+0.0005f, 0.5f - s/2, 1 - 0.5f + s/2,
                            0+0.0005f, 0.5f - s/2, 0.5f - s/2,
                            0+0.0005f, 0.5f + s/2, 0.5f - s/2,
                            0+0.0005f, 0.5f + s/2, 1 - 0.5f + s/2,
                            u1, v1, u2, v2, packedLight, packedOverlay, -1,0,0, be);
                    case EAST -> quadTinted(buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX)), pose,
                            1-0.0005f, 0.5f - s/2, 0.5f - s/2,
                            1-0.0005f, 0.5f - s/2, 1 - 0.5f + s/2,
                            1-0.0005f, 0.5f + s/2, 1 - 0.5f + s/2,
                            1-0.0005f, 0.5f + s/2, 0.5f - s/2,
                            u1, v1, u2, v2, packedLight, packedOverlay, 1,0,0, be);
                    case DOWN -> quadTinted(buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX)), pose,
                            0.5f - s/2, 0+0.0005f, 1 - 0.5f + s/2,
                            0.5f + s/2, 0+0.0005f, 1 - 0.5f + s/2,
                            0.5f + s/2, 0+0.0005f, 0.5f - s/2,
                            0.5f - s/2, 0+0.0005f, 0.5f - s/2,
                            u1, v1, u2, v2, packedLight, packedOverlay, 0,-1,0, be);
                    case UP -> quadTinted(buffer.getBuffer(RenderType.entityTranslucent(PANEL_TEX)), pose,
                            0.5f - s/2, 1-0.0005f, 0.5f - s/2,
                            0.5f + s/2, 1-0.0005f, 0.5f - s/2,
                            0.5f + s/2, 1-0.0005f, 1 - 0.5f + s/2,
                            0.5f - s/2, 1-0.0005f, 1 - 0.5f + s/2,
                            u1, v1, u2, v2, packedLight, packedOverlay, 0,1,0, be);
                }
            }
        }
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
        // Tint by fill; slight pulsing alpha
        int cap = Math.max(1, be.getCapacity());
        float fill = Math.min(1f, be.getStored() / (float) cap);
        long t = System.currentTimeMillis();
        float pulse = (float)(Math.sin(t / 300.0) * 0.1 + 0.1);
        a = 0.55f + pulse;
        // Cyan-ish that brightens with fill
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
