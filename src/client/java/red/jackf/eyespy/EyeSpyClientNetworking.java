package red.jackf.eyespy;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import red.jackf.eyespy.networking.packets.C2SHasClientModInstalled;
import red.jackf.eyespy.networking.packets.S2CSettings;

public class EyeSpyClientNetworking {
    public static void setup() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ClientPlayNetworking.canSend(C2SHasClientModInstalled.TYPE)) {
                ClientPlayNetworking.registerReceiver(S2CSettings.TYPE, ((packet, player, responseSender) -> EyeSpyClient.lastSettings = packet));
                sender.sendPacket(new C2SHasClientModInstalled());
            }
        });
    }
}
