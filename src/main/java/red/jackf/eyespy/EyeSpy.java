package red.jackf.eyespy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.eyespy.lies.Constants;
import red.jackf.eyespy.lies.LieManager;

import java.util.Collection;

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

		ServerLevel level = player.serverLevel();
		Collection<ServerPlayer> players = PlayerLookup.tracking(level, blockHit.getBlockPos());

		Sounds.playBlock(players, blockHit, level.getBlockState(blockHit.getBlockPos()));

		var existing = LieManager.getBlockHighlight(blockHit.getBlockPos());

		if (existing.isPresent()) {
			if (player.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
				existing.get().lie().fade();
				LieManager.createBlock(level, players, blockHit.getBlockPos(), true);
			} else {
				existing.get().refreshLifetime();
			}

			return;
		}

		LieManager.createBlock(level, players, blockHit.getBlockPos(), false);
	}

	private static void onEntity(ServerPlayer player, EntityHitResult entityHit) {
		LOGGER.debug("Result: ENTITY {}", entityHit.getEntity().getClass().getSimpleName());

		ServerLevel level = player.serverLevel();
		Collection<ServerPlayer> players = PlayerLookup.tracking(entityHit.getEntity());

		Sounds.playEntity(players, entityHit);

		var existing = LieManager.getEntityHighlight(entityHit.getEntity());

		if (existing.isPresent()) {
			if (player.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
				existing.get().lie().fade();
				LieManager.createEntity(level, players, entityHit.getEntity(), true);
			} else {
				existing.get().refreshLifetime();
			}

			return;
		}

		LieManager.createEntity(level, players, entityHit.getEntity(), false);
	}

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LieManager.fadeEverything(handler.player));

		ServerTickEvents.END_WORLD_TICK.register(level -> {
			if (level.getGameTime() % 200 == 0) {
				LOGGER.info("BLOCKS {}", LieManager.BLOCKS.size());
				LOGGER.info("ENTITIES {}", LieManager.ENTITIES.size());
			}
		});
	}
}