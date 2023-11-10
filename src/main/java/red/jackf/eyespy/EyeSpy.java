package red.jackf.eyespy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.eyespy.command.EyeSpyCommand;
import red.jackf.eyespy.config.EyeSpyConfig;
import red.jackf.eyespy.config.EyeSpyConfigMigrator;
import red.jackf.eyespy.ping.lies.LieManager;
import red.jackf.eyespy.rangefinding.Rangefinder;
import red.jackf.jackfredlib.api.config.ConfigHandler;

public class EyeSpy implements ModInitializer {
	public static Logger getLogger(String suffix) {
		return LoggerFactory.getLogger("red.jackf.eyespy.Eye Spy" + (suffix.isBlank() ? "" : "/" + suffix));
	}
    public static final Logger LOGGER = getLogger("");
	public static final String MODID = "eyespy";

	public static final ConfigHandler<EyeSpyConfig> CONFIG = ConfigHandler.builder(EyeSpyConfig.class)
			.fileName(MODID)
			.withLogger(getLogger("Config"))
			.withMigrator(EyeSpyConfigMigrator.get())
			.withFileWatcher()
			.build();

	@Override
	public void onInitialize() {
		CONFIG.load();
		Rangefinder.setup();
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LieManager.fadeEverything(handler.player));

		CommandRegistrationCallback.EVENT.register(EyeSpyCommand::new);
	}
}