package kr.pyke.splatcraft.handle;

import kr.pyke.splatcraft.manager.FieldManager;
import kr.pyke.splatcraft.manager.InkStorage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerLifeCycleEventsHandler {
    private ServerLifeCycleEventsHandler() { }

    public static void register() {
        serverStarted();
        serverStopping();
    }

    private static void serverStarted() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (var level : server.getAllLevels()) {
                FieldManager.loadFromSavedData(level);
                InkStorage.loadFromSavedData(level);
            }
        });
    }

    private static void serverStopping() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (var level : server.getAllLevels()) {
                if (InkStorage.get(level).hasDirtyChunks()) { InkStorage.markSavedDataDirty(level); }

                InkStorage.remove(level);
                FieldManager.remove(level);
            }
        });
    }
}
