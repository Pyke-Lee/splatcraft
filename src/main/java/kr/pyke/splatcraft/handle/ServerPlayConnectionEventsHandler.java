package kr.pyke.splatcraft.handle;

import kr.pyke.splatcraft.network.SCPacket;
import kr.pyke.splatcraft.team.TeamSync;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ServerPlayConnectionEventsHandler {
    private ServerPlayConnectionEventsHandler() { }

    public static void register() {
        join();
    }

    private static void join() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();

            SCPacket.sendFieldSync(player);
            SCPacket.sendFullInkSnapshot(player);

            TeamSync.syncAllOnLogin(server, player);
        });
    }
}
