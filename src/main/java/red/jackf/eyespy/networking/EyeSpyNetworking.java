package red.jackf.eyespy.networking;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.networking.packets.C2SHasClientModInstalled;
import red.jackf.eyespy.networking.packets.C2SPing;
import red.jackf.eyespy.ping.Ping;
import red.jackf.eyespy.ping.lies.LieManager;

import java.util.HashSet;
import java.util.Set;

public class EyeSpyNetworking {
    private static final Set<GameProfile> HAS_CLIENT_MOD_INSTALLED = new HashSet<>();

    public static boolean hasClientInstalled(GameProfile profile) {
        return HAS_CLIENT_MOD_INSTALLED.contains(profile);
    }

    public static void setup() {
        ServerPlayNetworking.registerGlobalReceiver(C2SHasClientModInstalled.TYPE, (packet, player, sender) -> {
            EyeSpy.LOGGER.debug("{} has client mod installed, not using swap items override", player.getName().getString());
            HAS_CLIENT_MOD_INSTALLED.add(player.getGameProfile());
        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPing.TYPE, ((packet, player, sender) -> {
            if (Ping.canActivate(player)) Ping.activate(player);
        }));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            HAS_CLIENT_MOD_INSTALLED.remove(handler.getOwner());
            LieManager.fadeEverything(handler.player);
        });
    }
}