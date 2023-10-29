package red.jackf.eyespy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.eyespy.lies.LieManager;

public class EyeSpy implements ModInitializer {
	public static Logger getLogger(String suffix) {
		return LoggerFactory.getLogger("red.jackf.eyespy.Eye Spy" + (suffix.isBlank() ? "" : "/" + suffix));
	}
    public static final Logger LOGGER = getLogger("");
	public static final int HIGHLIGHT_LIFETIME = 5 * SharedConstants.TICKS_PER_SECOND;

	public static void activate(ServerPlayer player) {
		LOGGER.debug("Ping from {}", player.getName().getString());

		HitResult hit = Raycasting.cast(player);

		switch (hit.getType()) {
			case MISS -> onMiss(player);
			case BLOCK -> onBlock(player, (BlockHitResult) hit);
			case ENTITY -> onEntity(player, (EntityHitResult) hit);
		}
	}

	private static void onMiss(ServerPlayer player) {
		LOGGER.debug("Result: MISS");

		Sounds.playMiss(player);
	}

	private static void onBlock(ServerPlayer player, BlockHitResult blockHit) {
		LOGGER.debug("Result: BLOCK {}", blockHit.getBlockPos().toShortString());

		Sounds.playBlock(player, blockHit);

		var existing = LieManager.getBlockHighlight(player, blockHit.getBlockPos());

		if (existing.isPresent()) {
			existing.get().refreshLifetime();
			return;
		}

		LieManager.createBlock(player, blockHit.getBlockPos());
	}

	private static void onEntity(ServerPlayer player, EntityHitResult entityHit) {
		LOGGER.debug("Result: ENTITY {}", entityHit.getEntity().getClass().getSimpleName());

		Sounds.playEntity(player, entityHit);

		var existing = LieManager.getEntityHighlight(player, entityHit.getEntity());

		if (existing.isPresent()) {
			existing.get().refreshLifetime();
			return;
		}

		LieManager.createEntity(player, entityHit.getEntity());
	}

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LieManager.fadeEverything(handler.player));
	}
}