package kr.pyke.splatcraft.handle;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.manager.PlayerTeamManager;
import kr.pyke.splatcraft.util.InkHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class InkMovementHandler {
    private static final Identifier ALLY_INK_SPEED_ID = Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "ally_ink_speed");
    private static final Identifier ENEMY_INK_SLOW_ID = Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "enemy_ink_slow");
    private static final double ALLY_SPEED_BONUS = 0.4;
    private static final double ENEMY_SPEED_PENALTY = -0.5;

    private InkMovementHandler() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                byte playerTeamID = PlayerTeamManager.getTeamID(player);
                AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr == null) { continue; }

                speedAttr.removeModifier(ALLY_INK_SPEED_ID);
                speedAttr.removeModifier(ENEMY_INK_SLOW_ID);

                if (playerTeamID == 0) { continue; }

                byte inkTeamBelow = InkHelper.getInkTeamBelow(player.level(), player.blockPosition());
                if (inkTeamBelow == 0) { continue; }

                if (inkTeamBelow == playerTeamID) {
                    speedAttr.addTransientModifier(new AttributeModifier(ALLY_INK_SPEED_ID, ALLY_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
                else {
                    speedAttr.addTransientModifier(new AttributeModifier(ENEMY_INK_SLOW_ID, ENEMY_SPEED_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        });
    }
}