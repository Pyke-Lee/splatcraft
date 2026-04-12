package kr.pyke.splatcraft.client;

import kr.pyke.splatcraft.network.SCPacket;
import net.fabricmc.api.ClientModInitializer;

public class SplatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SCPacket.registerClient();
    }
}
