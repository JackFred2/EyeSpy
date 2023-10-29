package red.jackf.eyespy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EyeSpy implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("eyespy");

	public static void activate(ServerPlayer player, ServerLevel serverLevel) {
		player.sendSystemMessage(Component.literal("ping"));
	}

	@Override
	public void onInitialize() {

	}
}