package red.jackf.eyespy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

public class EyeSpy implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("eyespy");
	private static final int HIGHLIGHT_LIFETIME = 5 * SharedConstants.TICKS_PER_SECOND;

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
			Debris.INSTANCE.schedule(existing.get(), HIGHLIGHT_LIFETIME);
			return;
		}

		ServerLevel level = player.serverLevel();
		BlockState state = level.getBlockState(blockHit.getBlockPos());

		var lie = EntityLie.builder(EntityBuilders.blockDisplay(level)
											.state(state)
											.positionCentered(blockHit.getBlockPos())
											.scaleAndCenter(0.99f)
											.brightness(15, 15)
											.viewRangeModifier(5f)
											.glowing(true,
													 Colour.fromInt(state.getBlock().defaultMapColor().col).scaleBrightness(1.25F))
											.build())
				.onFade(LieManager::removeBlock)
				.createAndShow(player);

		LieManager.addBlock(player, lie);

		Debris.INSTANCE.schedule(lie, HIGHLIGHT_LIFETIME);
	}

	private static void onEntity(ServerPlayer player, EntityHitResult entityHit) {
		LOGGER.debug("Result: ENTITY {}", entityHit.getEntity().getClass().getSimpleName());

		Sounds.playEntity(player, entityHit);

		var existing = LieManager.getEntityHighlight(player, entityHit.getEntity());

		if (existing.isPresent()) {
			Debris.INSTANCE.schedule(existing.get(), HIGHLIGHT_LIFETIME);
			return;
		}

		var lie = EntityGlowLie.builder(entityHit.getEntity())
				.colour(ChatFormatting.GREEN)
				.onFade(LieManager::removeEntity)
				.createAndShow(player);

		LieManager.addEntity(player, lie);

		Debris.INSTANCE.schedule(lie, HIGHLIGHT_LIFETIME);
	}

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LieManager.fade(handler.player));
	}
}