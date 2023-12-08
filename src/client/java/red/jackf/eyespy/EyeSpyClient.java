package red.jackf.eyespy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import red.jackf.eyespy.networking.packets.C2SPing;
import red.jackf.eyespy.networking.packets.S2CSettings;
import red.jackf.jackfredlib.client.api.toasts.*;

import java.util.function.Function;

public class EyeSpyClient implements ClientModInitializer {
    private static final KeyMapping PING = new KeyMapping("key.eyespy.ping", GLFW.GLFW_KEY_Z, "key.categories.multiplayer");
    private static final Function<Boolean, CustomToast> USAGE = requiresZoom ->
            ToastBuilder.builder(ToastFormat.DARK, Component.translatable("eyespy.title"))
                        .withImage(ImageSpec.modIcon("eyespy"))
                        .expiresAfter(3000L)
                        .progressShowsVisibleTime()
                        .addMessage(Component.translatable(requiresZoom ? "eyespy.toast.ping.withZoom" : "eyespy.toast.ping.noZoom",
                                                           PING.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.AQUA),
                                                           Items.SPYGLASS.getDescription()))
                        .build();
    @Nullable
    public static S2CSettings lastSettings = null;
    private static boolean shownToast = false;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(PING);
        EyeSpyClientNetworking.setup();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            shownToast = false;
            lastSettings = null;
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            var connection = client.getConnection();
            if (connection != null && ClientPlayNetworking.canSend(C2SPing.TYPE)) {
                while (PING.consumeClick()) {
                    ClientPlayNetworking.send(new C2SPing());
                }

                if (!shownToast && client.player != null && lastSettings != null && lastSettings.pingEnabled()) {
                    if (client.player.getMainHandItem().is(Items.SPYGLASS) || client.player.getOffhandItem().is(Items.SPYGLASS)) {
                        shownToast = true;
                        Toasts.INSTANCE.send(USAGE.apply(lastSettings.pingRequiresZoom()));
                    }
                }
            }
        });
    }
}
