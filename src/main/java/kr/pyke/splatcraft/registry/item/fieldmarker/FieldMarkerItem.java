package kr.pyke.splatcraft.registry.item.fieldmarker;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FieldMarkerItem extends Item {
    private static final Map<UUID, Selection> SELECTIONS = new ConcurrentHashMap<>();

    public FieldMarkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.level().isClientSide()) { return InteractionResult.PASS; }

        BlockPos clickedPos = context.getClickedPos();
        UUID playerID = player.getUUID();

        if (player.isShiftKeyDown()) {
            Selection selection = SELECTIONS.computeIfAbsent(playerID, k -> new Selection());
            selection.pos2 = clickedPos.immutable();

            player.sendOverlayMessage(Component.literal("점 2 지정: ").append(formatPos(clickedPos)).withStyle(ChatFormatting.GREEN));

            if (selection.pos1 != null) {
                player.sendSystemMessage(Component.literal("두 점이 지정되었습니다. ").append(Component.literal("/splatcraft field create <id>").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" 로 구역을 생성하세요.")).withStyle(ChatFormatting.GRAY));
            }
        }
        else {
            Selection selection = SELECTIONS.computeIfAbsent(playerID, k -> new Selection());
            selection.pos1 = clickedPos.immutable();

            player.sendOverlayMessage(Component.literal("점 1 지정: ").append(formatPos(clickedPos)).withStyle(ChatFormatting.AQUA));
        }

        return InteractionResult.SUCCESS;
    }

    public static Selection getSelection(UUID playerID) { return SELECTIONS.get(playerID); }

    public static void clearSelection(UUID playerID) {
        SELECTIONS.remove(playerID);
    }

    private static Component formatPos(BlockPos pos) { return Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]").withStyle(ChatFormatting.WHITE); }

    public static class Selection {
        public BlockPos pos1;
        public BlockPos pos2;

        public boolean isComplete() { return pos1 != null && pos2 != null; }
    }
}