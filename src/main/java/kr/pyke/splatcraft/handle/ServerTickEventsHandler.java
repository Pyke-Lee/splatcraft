package kr.pyke.splatcraft.handle;

import kr.pyke.splatcraft.manager.InkStorage;
import kr.pyke.splatcraft.network.SCPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ServerTickEventsHandler {
    private static final int INK_SYNC_INTERVAL = 2;
    private static int tickCounter = 0;

    private ServerTickEventsHandler() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= INK_SYNC_INTERVAL) {
                tickCounter = 0;
                for (var level : server.getAllLevels()) {
                    boolean hadDirty = InkStorage.get(level).hasDirtyChunks();
                    SCPacket.broadcastDirtyInk(level);
                    if (hadDirty) { InkStorage.markSavedDataDirty(level); }
                }
            }
        });
    }
}
