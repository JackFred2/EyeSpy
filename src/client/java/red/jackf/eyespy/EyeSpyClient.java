package red.jackf.eyespy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import red.jackf.eyespy.networking.packets.C2SPing;

public class EyeSpyClient implements ClientModInitializer {
    private static final KeyMapping PING = new KeyMapping("key.eyespy.ping", GLFW.GLFW_KEY_Z, "key.categories.multiplayer");

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(PING);
        EyeSpyClientNetworking.setup();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            var connection = client.getConnection();
            if (connection != null && ClientPlayNetworking.canSend(C2SPing.TYPE)) {
                while (PING.consumeClick()) {
                    ClientPlayNetworking.send(new C2SPing());
                }
            }
        });
    }
}
