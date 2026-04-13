package kr.pyke.splatcraft.manager;

import com.mojang.serialization.Codec;
import kr.pyke.splatcraft.SplatCraft;
import kr.pyke.splatcraft.data.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FieldManager {
    private static final Map<Level, FieldManager> INSTANCES = new ConcurrentHashMap<>();
    private final Map<String, Field> fields = new ConcurrentHashMap<>();

    public static FieldManager get(Level level) { return INSTANCES.computeIfAbsent(level, k -> new FieldManager()); }

    public static void remove(Level level) {
        INSTANCES.remove(level);
    }

    public Field getField(String id) { return fields.get(id); }

    public Collection<Field> getAllFields() { return Collections.unmodifiableCollection(fields.values()); }

    public boolean isInActiveField(BlockPos pos) {
        for (Field field : fields.values()) {
            if (field.isActive() && field.contains(pos)) { return true; }
        }

        return false;
    }

    public Field getActiveFieldAt(BlockPos pos) {
        for (Field field : fields.values()) {
            if (field.isActive() && field.contains(pos)) { return field; }
        }

        return null;
    }

    public boolean addField(Field field) {
        if (fields.containsKey(field.getID())) { return false; }
        fields.put(field.getID(), field);

        return true;
    }

    public Field removeField(String id) { return fields.remove(id); }

    public void clear() {
        fields.clear();
    }

    public static void loadFromSavedData(ServerLevel level) {
        FieldSavedData savedData = level.getDataStorage().computeIfAbsent(FieldSavedData.TYPE);
        FieldManager manager = get(level);
        manager.fields.clear();
        for (Field field : savedData.getFields()) {
            manager.fields.put(field.getID(), field);
        }
    }

    public void saveToSavedData(ServerLevel level) {
        FieldSavedData savedData = level.getDataStorage().computeIfAbsent(FieldSavedData.TYPE);
        savedData.setFields(fields.values());
        savedData.setDirty();
    }

    public static class FieldSavedData extends SavedData {
        private static final Identifier FILE_NAME = Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "splatcraft_fields");

        public static final Codec<FieldSavedData> CODEC = Field.CODEC.listOf()
            .xmap(
                list -> {
                    FieldSavedData data = new FieldSavedData();
                    for (Field field : list) { data.fields.put(field.getID(), field); }

                    return data;
                },
                data -> List.copyOf(data.fields.values())
            );

        public static final SavedDataType<FieldSavedData> TYPE = new SavedDataType<>(FILE_NAME, FieldSavedData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

        private final Map<String, Field> fields = new ConcurrentHashMap<>();

        public FieldSavedData() { }

        public Collection<Field> getFields() { return fields.values(); }

        public void setFields(Collection<Field> newFields) {
            fields.clear();
            for (Field field : newFields) { fields.put(field.getID(), field); }
        }
    }
}