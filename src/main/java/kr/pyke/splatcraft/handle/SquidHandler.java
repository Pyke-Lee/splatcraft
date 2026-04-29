package kr.pyke.splatcraft.handle;

import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.manager.PlayerTeamManager;
import kr.pyke.splatcraft.registry.component.ModComponents;
import kr.pyke.splatcraft.registry.component.squid.ISquidComponent;
import kr.pyke.splatcraft.util.InkHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SquidHandler {
    private static final Identifier SQUID_SPEED_ID = Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "squid_speed");
    private static final double SQUID_SPEED_BONUS = 0.6d;

    private SquidHandler() { }

    public static void tickPlayer(ServerPlayer player) {
        ISquidComponent component = ModComponents.SQUID.get(player);
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        byte playerTeamID = PlayerTeamManager.getTeamID(player);
        if (playerTeamID == 0) {
            exitSquid(player, component, speedAttribute);
            return;
        }

        boolean onAllyInk = InkHelper.getInkTeamBelow(player.level(), player.blockPosition()) == playerTeamID;
        boolean wantsSquid = player.isShiftKeyDown() && onAllyInk;

        if (wantsSquid && !component.isSubmerged()) {
            enterSquid(player, component, speedAttribute);
        }
        else if (!wantsSquid && component.isSubmerged()) {
            exitSquid(player, component, speedAttribute);
        }
    }

    private static void enterSquid(ServerPlayer player, ISquidComponent component, AttributeInstance speedAttribute) {
        component.setSubmerged(true);
        player.setInvisible(true);

        if (speedAttribute != null && speedAttribute.getModifier(SQUID_SPEED_ID) == null) {
            speedAttribute.addTransientModifier(new AttributeModifier(SQUID_SPEED_ID, SQUID_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void exitSquid(ServerPlayer player, ISquidComponent component, AttributeInstance speedAttr) {
        if (component.isSubmerged()) {
            component.setSubmerged(false);
            player.setInvisible(false);
        }

        if (speedAttr != null) {
            speedAttr.removeModifier(SQUID_SPEED_ID);
        }
    }
}
