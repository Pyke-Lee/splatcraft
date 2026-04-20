package kr.pyke.splatcraft.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.data.TeamColorData;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;

import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TeamColorManager implements PreparableReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final FileToIdConverter FILE_TO_ID = FileToIdConverter.json("team_color");

    private static final int DEFAULT_COLOR = 0xBBFFFFFF;

    private static Map<Byte, TeamColorData> teamColorMap = Collections.emptyMap();

    public static TeamColorData get(byte teamId) { return teamColorMap.get(teamId); }

    public static int getColor(byte teamID) {
        TeamColorData data = teamColorMap.get(teamID);

        return data != null ? data.argb() : DEFAULT_COLOR;
    }

    public static Map<Byte, TeamColorData> getAll() { return Collections.unmodifiableMap(teamColorMap); }

    @Override
    public @NonNull CompletableFuture<Void> reload(@NonNull SharedState currentReload, @NonNull Executor taskExecutor, PreparationBarrier preparationBarrier, @NonNull Executor reloadExecutor) {
        return CompletableFuture.supplyAsync(() -> load(currentReload.resourceManager()), taskExecutor)
            .thenCompose(preparationBarrier::wait)
            .thenAcceptAsync(
            loaded -> {
                teamColorMap = loaded;
                SplatCraft.LOGGER.info("Loaded {} team color entries", loaded.size());
            },
            reloadExecutor
        );
    }

    private static Map<Byte, TeamColorData> load(ResourceManager resourceManager) {
        Map<Byte, TeamColorData> result = new HashMap<>();

        for (var entry : FILE_TO_ID.listMatchingResources(resourceManager).entrySet()) {
            Identifier fileId = entry.getKey();
            Resource resource = entry.getValue();
            Identifier dataId = FILE_TO_ID.fileToId(fileId);

            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                TeamColorData.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> SplatCraft.LOGGER.error("Failed to parse team color {}: {}", dataId, error))
                    .ifPresent(data -> result.put(data.teamID(), data));
            }
            catch (Exception e) {
                SplatCraft.LOGGER.error("Failed to load team color {}", dataId, e);
            }
        }

        return result;
    }
}