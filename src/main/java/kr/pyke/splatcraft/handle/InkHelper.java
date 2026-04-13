package kr.pyke.splatcraft.handle;

import kr.pyke.splatcraft.data.InkEntry;
import kr.pyke.splatcraft.manager.FieldManager;
import kr.pyke.splatcraft.manager.InkStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ThreadLocalRandom;

public final class InkHelper {
    private InkHelper() { }

    public static boolean tryApplyInk(Level level, BlockPos pos, Direction face, byte teamID) {
        if (!FieldManager.get(level).isInActiveField(pos)) { return false; }
        if (!canReceiveInk(level, pos)) { return false; }
        if (!isFaceExposed(level, pos, face)) { return false; }

        byte pattern = (byte) ThreadLocalRandom.current().nextInt(8);
        return InkStorage.get(level).applyInk(pos, face, teamID, pattern);
    }

    public static int applySplash(Level level, BlockPos center, int radius, byte teamID) {
        int applied = 0;
        int radiusSq = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) { continue; }

                    BlockPos pos = center.offset(dx, dy, dz);
                    for (Direction face : Direction.values()) {
                        if (tryApplyInk(level, pos, face, teamID)) { applied++; }
                    }
                }
            }
        }

        return applied;
    }

    public static boolean canReceiveInk(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) { return false; }

        return state.isSolidRender();
    }

    public static boolean isFaceExposed(Level level, BlockPos pos, Direction face) {
        BlockPos adjacent = pos.relative(face);
        BlockState adjacentState = level.getBlockState(adjacent);

        return !adjacentState.isSolidRender();
    }

    public static byte getInkTeamBelow(Level level, BlockPos playerPos) {
        BlockPos below = playerPos.below();
        InkStorage storage = InkStorage.get(level);
        if (storage.hasInkOnFace(below, Direction.UP)) { return storage.getTeamAt(below); }

        return 0;
    }

    public static Direction getInkedWallFace(Level level, BlockPos pos, byte teamID) {
        InkStorage storage = InkStorage.get(level);

        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos wallBlock = pos.relative(face);
            Direction oppositeFace = face.getOpposite();

            InkEntry entry = storage.getEntry(wallBlock);
            if (entry != null && entry.hasFace(oppositeFace) && entry.getTeamID() == teamID) { return face; }
        }

        return null;
    }
}