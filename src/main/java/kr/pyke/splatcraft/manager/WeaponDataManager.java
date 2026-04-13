package kr.pyke.splatcraft.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.data.WeaponData;
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

public class WeaponDataManager implements PreparableReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final FileToIdConverter FILE_TO_ID = FileToIdConverter.json("weapon");

    private static Map<Identifier, WeaponData> weaponDataMap = Collections.emptyMap();

    public static WeaponData get(Identifier id) { return weaponDataMap.getOrDefault(id, WeaponData.DEFAULT); }

    public static Map<Identifier, WeaponData> getAll() { return Collections.unmodifiableMap(weaponDataMap); }

    @Override
    public @NonNull CompletableFuture<Void> reload(@NonNull SharedState currentReload, @NonNull Executor taskExecutor, PreparationBarrier preparationBarrier, @NonNull Executor reloadExecutor) {
        return CompletableFuture.supplyAsync(() -> load(currentReload.resourceManager()), taskExecutor).thenCompose(preparationBarrier::wait).thenAcceptAsync(
            loaded -> {
                weaponDataMap = loaded;
                SplatCraft.LOGGER.info("Loaded {} weapon data entries", loaded.size());
            },
            reloadExecutor
        );
    }

    private static Map<Identifier, WeaponData> load(ResourceManager resourceManager) {
        Map<Identifier, WeaponData> result = new HashMap<>();

        for (var entry : FILE_TO_ID.listMatchingResources(resourceManager).entrySet()) {
            Identifier fileID = entry.getKey();
            Resource resource = entry.getValue();
            Identifier dataID = FILE_TO_ID.fileToId(fileID);

            try (Reader reader = resource.openAsReader()) {
                JsonElement json = GSON.fromJson(reader, JsonElement.class);
                WeaponData.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(error -> SplatCraft.LOGGER.error("Failed to parse weapon data {}: {}", dataID, error)).ifPresent(data -> result.put(dataID, data));
            }
            catch (Exception e) {
                SplatCraft.LOGGER.error("Failed to load weapon data {}", dataID, e);
            }
        }

        return result;
    }
}