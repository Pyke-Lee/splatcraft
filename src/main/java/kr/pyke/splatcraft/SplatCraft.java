package kr.pyke.splatcraft;

import kr.pyke.splatcraft.command.SplatCraftCommand;
import kr.pyke.splatcraft.handle.InkMovementHandler;
import kr.pyke.splatcraft.handle.ServerLifeCycleEventsHandler;
import kr.pyke.splatcraft.handle.ServerPlayConnectionEventsHandler;
import kr.pyke.splatcraft.handle.ServerTickEventsHandler;
import kr.pyke.splatcraft.manager.TeamColorManager;
import kr.pyke.splatcraft.manager.WeaponDataManager;
import kr.pyke.splatcraft.network.SCPacket;
import kr.pyke.splatcraft.registry.ModRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplatCraft implements ModInitializer {
    public static String MOD_ID = "splatcraft";
    public static Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static MinecraftServer SERVER_INSTANCE;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER_INSTANCE = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER_INSTANCE = null);

        SCPacket.registerCodec();
        SCPacket.registerServer();

        ModRegistry.register();

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "weapon_data"), new WeaponDataManager());
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier.fromNamespaceAndPath(MOD_ID, "team_color_data"), new TeamColorManager());

        ServerLifeCycleEventsHandler.register();
        ServerTickEventsHandler.register();
        ServerPlayConnectionEventsHandler.register();

        InkMovementHandler.register();

        CommandRegistrationCallback.EVENT.register(SplatCraftCommand::register);
    }
}
