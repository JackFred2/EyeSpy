package red.jackf.eyespy;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import red.jackf.eyespy.networking.packets.C2SHasClientModInstalled;

public class EyeSpyClientNetworking {
    public static void setup() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientPlayNetworking.canSend(C2SHasClientModInstalled.TYPE)) {
                sender.sendPacket(new C2SHasClientModInstalled());
            }
        });
    }
}
