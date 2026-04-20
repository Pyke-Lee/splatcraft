package kr.pyke.splatcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.pyke.splatcraft.data.ChunkInkData;
import kr.pyke.splatcraft.data.InkEntry;
import kr.pyke.splatcraft.manager.InkStorage;
import kr.pyke.splatcraft.manager.TeamColorManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class InkWorldRenderer {
    private static final float OFFSET = 0.001f;
    private static final int RENDER_CHUNK_RADIUS = 8;

    private InkWorldRenderer() { }

    public static void render(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) { return; }

        InkStorage storage = InkStorage.get(level);
        if (storage.getAllChunks().isEmpty()) { return; }

        PoseStack poseStack = context.poseStack();
        MultiBufferSource.BufferSource bufferSource = context.bufferSource();

        Vec3 camPos = context.levelState().cameraRenderState.pos;
        int camCX = (int) camPos.x() >> 4;
        int camCZ = (int) camPos.z() >> 4;

        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.debugQuads());

        poseStack.pushPose();

        for (Map.Entry<ChunkPos, ChunkInkData> chunkEntry : storage.getAllChunks().entrySet()) {
            ChunkPos chunkPos = chunkEntry.getKey();

            if (Math.abs(chunkPos.x() - camCX) > RENDER_CHUNK_RADIUS || Math.abs(chunkPos.z() - camCZ) > RENDER_CHUNK_RADIUS) { continue; }

            ChunkInkData chunkData = chunkEntry.getValue();
            for (Map.Entry<BlockPos, InkEntry> blockEntry : chunkData.getEntries().entrySet()) {
                BlockPos pos = blockEntry.getKey();
                InkEntry inkEntry = blockEntry.getValue();

                int color = TeamColorManager.getColor(inkEntry.getTeamID());

                float x = (float) (pos.getX() - camPos.x());
                float y = (float) (pos.getY() - camPos.y());
                float z = (float) (pos.getZ() - camPos.z());

                for (Direction face : Direction.values()) {
                    if (inkEntry.hasFace(face)) {
                        renderInkFace(consumer, poseStack, x, y, z, face, color);
                    }
                }
            }
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static void renderInkFace(VertexConsumer consumer, PoseStack poseStack, float x, float y, float z, Direction face, int color) {
        PoseStack.Pose pose = poseStack.last();

        switch (face) {
            case UP -> {
                float oy = y + 1.0f + OFFSET;
                consumer.addVertex(pose, x, oy, z).setColor(color);
                consumer.addVertex(pose, x, oy, z + 1).setColor(color);
                consumer.addVertex(pose, x + 1, oy, z + 1).setColor(color);
                consumer.addVertex(pose, x + 1, oy, z).setColor(color);
            }
            case DOWN -> {
                float oy = y - OFFSET;
                consumer.addVertex(pose, x, oy, z + 1).setColor(color);
                consumer.addVertex(pose, x, oy, z).setColor(color);
                consumer.addVertex(pose, x + 1, oy, z).setColor(color);
                consumer.addVertex(pose, x + 1, oy, z + 1).setColor(color);
            }
            case NORTH -> {
                float oz = z - OFFSET;
                consumer.addVertex(pose, x + 1, y, oz).setColor(color);
                consumer.addVertex(pose, x + 1, y + 1, oz).setColor(color);
                consumer.addVertex(pose, x, y + 1, oz).setColor(color);
                consumer.addVertex(pose, x, y, oz).setColor(color);
            }
            case SOUTH -> {
                float oz = z + 1.0f + OFFSET;
                consumer.addVertex(pose, x, y, oz).setColor(color);
                consumer.addVertex(pose, x, y + 1, oz).setColor(color);
                consumer.addVertex(pose, x + 1, y + 1, oz).setColor(color);
                consumer.addVertex(pose, x + 1, y, oz).setColor(color);
            }
            case WEST -> {
                float ox = x - OFFSET;
                consumer.addVertex(pose, ox, y, z).setColor(color);
                consumer.addVertex(pose, ox, y + 1, z).setColor(color);
                consumer.addVertex(pose, ox, y + 1, z + 1).setColor(color);
                consumer.addVertex(pose, ox, y, z + 1).setColor(color);
            }
            case EAST -> {
                float ox = x + 1.0f + OFFSET;
                consumer.addVertex(pose, ox, y, z + 1).setColor(color);
                consumer.addVertex(pose, ox, y + 1, z + 1).setColor(color);
                consumer.addVertex(pose, ox, y + 1, z).setColor(color);
                consumer.addVertex(pose, ox, y, z).setColor(color);
            }
        }
    }
}