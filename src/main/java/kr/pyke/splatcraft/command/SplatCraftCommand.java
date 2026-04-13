package kr.pyke.splatcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import kr.pyke.splatcraft.data.Field;
import kr.pyke.splatcraft.manager.FieldManager;
import kr.pyke.splatcraft.manager.InkStorage;
import kr.pyke.splatcraft.network.SCPacket;
import kr.pyke.splatcraft.registry.item.fieldmarker.FieldMarkerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Collection;

public class SplatCraftCommand {
    private SplatCraftCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(
            Commands.literal("splatcraft")
                .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
                .then(Commands.literal("field.json")
                    .then(Commands.literal("create")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(SplatCraftCommand::createField)
                        )
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(SplatCraftCommand::removeField)
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(SplatCraftCommand::listFields)
                    )
                    .then(Commands.literal("activate")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(SplatCraftCommand::activateField)
                        )
                    )
                    .then(Commands.literal("deactivate")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(SplatCraftCommand::deactivateField)
                        )
                    )
                )
                .then(Commands.literal("ink")
                    .then(Commands.literal("clear")
                        .executes(SplatCraftCommand::clearAllInk)
                        .then(Commands.argument("fieldID", StringArgumentType.word())
                            .executes(SplatCraftCommand::clearFieldInk)
                        )
                    )
                )
        );
    }

    private static int createField(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("플레이어만 사용할 수 있는 명령어입니다."));
            return 0;
        }

        FieldMarkerItem.Selection selection = FieldMarkerItem.getSelection(player.getUUID());
        if (selection == null || !selection.isComplete()) {
            source.sendFailure(Component.literal("먼저 Field Marker로 두 점을 지정하세요.").withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerLevel level = player.level();
        FieldManager manager = FieldManager.get(level);

        if (manager.getField(id) != null) {
            source.sendFailure(Component.literal("이미 존재하는 구역 ID: " + id).withStyle(ChatFormatting.RED));
            return 0;
        }

        Field field = Field.create(id, selection.pos1, selection.pos2);
        manager.addField(field);
        manager.saveToSavedData(level);

        SCPacket.broadcastFieldSync(level);
        FieldMarkerItem.clearSelection(player.getUUID());

        source.sendSuccess(() -> Component.literal("구역 생성 완료: ").append(Component.literal(id).withStyle(ChatFormatting.YELLOW)).append(Component.literal(" (").append(formatPos(field.getMin())).append(" ~ ").append(formatPos(field.getMax())).append(")")), true);
        return 1;
    }

    private static int removeField(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        ServerLevel level = source.getLevel();
        FieldManager manager = FieldManager.get(level);

        Field removed = manager.removeField(id);
        if (removed == null) {
            source.sendFailure(Component.literal("존재하지 않는 구역: " + id).withStyle(ChatFormatting.RED));
            return 0;
        }

        InkStorage.get(level).clearArea(removed.getMin(), removed.getMax());
        manager.saveToSavedData(level);

        SCPacket.broadcastFieldSync(level);
        SCPacket.broadcastInkClear(level, removed);

        source.sendSuccess(() -> Component.literal("구역 삭제 완료: ").append(Component.literal(id).withStyle(ChatFormatting.YELLOW)), true);
        return 1;
    }

    private static int listFields(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Collection<Field> fields = FieldManager.get(level).getAllFields();

        if (fields.isEmpty()) {
            source.sendSuccess(() -> Component.literal("등록된 구역이 없습니다.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("=== 구역 목록 (" + fields.size() + "개) ===").withStyle(ChatFormatting.GOLD), false);

        for (Field field : fields) {
            ChatFormatting statusColor = field.isActive() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String status = field.isActive() ? "[활성]" : "[비활성]";

            source.sendSuccess(() -> Component.literal(" " + status + " ").withStyle(statusColor).append(Component.literal(field.getID()).withStyle(ChatFormatting.WHITE)).append(Component.literal(" (").append(formatPos(field.getMin())).append(" ~ ").append(formatPos(field.getMax())).append(")").withStyle(ChatFormatting.GRAY)), false);
        }

        return fields.size();
    }

    private static int activateField(CommandContext<CommandSourceStack> context) { return setFieldActive(context, true); }

    private static int deactivateField(CommandContext<CommandSourceStack> context) { return setFieldActive(context, false); }

    private static int setFieldActive(CommandContext<CommandSourceStack> context, boolean active) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        ServerLevel level = source.getLevel();
        FieldManager manager = FieldManager.get(level);

        Field field = manager.getField(id);
        if (field == null) {
            source.sendFailure(Component.literal("존재하지 않는 구역: " + id).withStyle(ChatFormatting.RED));
            return 0;
        }

        field.setActive(active);
        manager.saveToSavedData(level);
        SCPacket.broadcastFieldSync(level);

        String state = active ? "활성화" : "비활성화";
        source.sendSuccess(() -> Component.literal("구역 " + state + ": ").append(Component.literal(id).withStyle(ChatFormatting.YELLOW)), true);
        return 1;
    }

    private static int clearAllInk(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        InkStorage.get(level).clearAll();

        for (Field field : FieldManager.get(level).getAllFields()) {
            SCPacket.broadcastInkClear(level, field);
        }

        source.sendSuccess(() -> Component.literal("모든 잉크가 제거되었습니다.").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int clearFieldInk(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String fieldID = StringArgumentType.getString(context, "fieldID");
        ServerLevel level = source.getLevel();

        Field field = FieldManager.get(level).getField(fieldID);
        if (field == null) {
            source.sendFailure(Component.literal("존재하지 않는 구역: " + fieldID).withStyle(ChatFormatting.RED));
            return 0;
        }

        int removed = InkStorage.get(level).clearArea(field.getMin(), field.getMax());
        SCPacket.broadcastInkClear(level, field);

        source.sendSuccess(() -> Component.literal("구역 " + fieldID + "의 잉크가 제거되었습니다. (블록 " + removed + "개)").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static Component formatPos(BlockPos pos) { return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()); }
}