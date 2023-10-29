package red.jackf.eyespy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.eyespy.lies.Constants;
import red.jackf.eyespy.lies.LieManager;

public class EyeSpy implements ModInitializer {
	public static Logger getLogger(String suffix) {
		return LoggerFactory.getLogger("red.jackf.eyespy.Eye Spy" + (suffix.isBlank() ? "" : "/" + suffix));
	}
    public static final Logger LOGGER = getLogger("");

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

		Sounds.playBlock(player, blockHit, player.serverLevel().getBlockState(blockHit.getBlockPos()));

		var existing = LieManager.getBlockHighlight(player, blockHit.getBlockPos());

		if (existing.isPresent()) {
			if (player.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
				existing.get().lie().fade();
				LieManager.createBlock(player, blockHit.getBlockPos(), true);
			} else {
				existing.get().refreshLifetime();
			}

			return;
		}

		LieManager.createBlock(player, blockHit.getBlockPos(), false);
	}

	private static void onEntity(ServerPlayer player, EntityHitResult entityHit) {
		LOGGER.debug("Result: ENTITY {}", entityHit.getEntity().getClass().getSimpleName());

		Sounds.playEntity(player, entityHit);

		var existing = LieManager.getEntityHighlight(player, entityHit.getEntity());

		if (existing.isPresent()) {
			if (player.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
				existing.get().lie().fade();
				LieManager.createEntity(player, entityHit.getEntity(), true);
			} else {
				existing.get().refreshLifetime();
			}

			return;
		}

		LieManager.createEntity(player, entityHit.getEntity(), false);
	}

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LieManager.fadeEverything(handler.player));
	}
}