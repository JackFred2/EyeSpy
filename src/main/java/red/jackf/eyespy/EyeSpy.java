package red.jackf.eyespy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.eyespy.command.EyeSpyCommand;
import red.jackf.eyespy.config.EyeSpyConfig;
import red.jackf.eyespy.config.EyeSpyConfigMigrator;
import red.jackf.eyespy.networking.EyeSpyNetworking;
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

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
		CONFIG.load();
		Rangefinder.setup();
		EyeSpyNetworking.setup();

		CommandRegistrationCallback.EVENT.register(EyeSpyCommand::new);
	}
}