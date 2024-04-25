package red.jackf.eyespy.networking;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.networking.packets.C2SHasClientModInstalled;
import red.jackf.eyespy.networking.packets.C2SPing;
import red.jackf.eyespy.networking.packets.S2CSettings;
import red.jackf.eyespy.ping.Ping;

import java.util.HashSet;
import java.util.Set;

public class EyeSpyNetworking {
    private static final Set<GameProfile> HAS_CLIENT_MOD_INSTALLED = new HashSet<>();

    public static boolean hasClientInstalled(GameProfile profile) {
        return HAS_CLIENT_MOD_INSTALLED.contains(profile);
    }

    public static void sendSettings(ServerPlayer player) {
        ServerPlayNetworking.send(player, S2CSettings.create());
    }

    public static void setup() {
        PayloadTypeRegistry.playC2S().register(C2SHasClientModInstalled.TYPE, C2SHasClientModInstalled.CODEC);
        PayloadTypeRegistry.playC2S().register(C2SPing.TYPE, C2SPing.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSettings.TYPE, S2CSettings.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(C2SHasClientModInstalled.TYPE, (packet, context) -> {
            EyeSpy.LOGGER.debug("{} has client mod installed, not using swap items override", context.player().getName().getString());
            HAS_CLIENT_MOD_INSTALLED.add(context.player().getGameProfile());
            sendSettings(context.player());
        });

        ServerPlayNetworking.registerGlobalReceiver(C2SPing.TYPE, ((packet, context) -> {
            if (Ping.canActivate(context.player(), true)) Ping.activate(context.player());
        }));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> HAS_CLIENT_MOD_INSTALLED.remove(handler.getOwner()));
    }
}
