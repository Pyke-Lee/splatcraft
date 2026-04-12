package kr.pyke.splatcraft.registry;

import kr.pyke.splatcraft.registry.entity.ModEntities;
import kr.pyke.splatcraft.registry.item.ModItems;

public class ModRegistry {
    private ModRegistry() { }

    public static void register() {
        ModItems.register();
        ModEntities.register();
    }
}
