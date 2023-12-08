package red.jackf.eyespy.ping;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.ping.lies.Constants;
import red.jackf.eyespy.ping.lies.LieManager;
import red.jackf.eyespy.raycasting.Raycasting;

import java.util.Collection;

public class Ping {
    /**
     * Check if a player can ping given their current state and mod settings.
     * @param player Player trying to ping
     * @return Whether the player can ping at this time.
     */
    public static boolean canActivate(ServerPlayer player) {
        if (!EyeSpy.CONFIG.instance().ping.enabled) return false;
        if (EyeSpy.CONFIG.instance().ping.requiresZoomIn) {
            return player.getUseItem().is(Items.SPYGLASS);
        } else {
            return player.getMainHandItem().is(Items.SPYGLASS) || player.getOffhandItem().is(Items.SPYGLASS);
        }
    }

    public static void activate(ServerPlayer player) {
        EyeSpy.LOGGER.debug("Ping from {}", player.getName().getString());

        HitResult hit = Raycasting.cast(player);

        switch (hit.getType()) {
            case MISS -> onMiss(player);
            case BLOCK -> onBlock(player, (BlockHitResult) hit);
            case ENTITY -> onEntity(player, (EntityHitResult) hit);
        }
    }

    private static void onMiss(ServerPlayer player) {
        EyeSpy.LOGGER.debug("Result: MISS");

        Sounds.playMiss(player);
    }

    private static void onBlock(ServerPlayer player, BlockHitResult blockHit) {
        EyeSpy.LOGGER.debug("Result: BLOCK {}", blockHit.getBlockPos().toShortString());

        ServerLevel level = player.serverLevel();
        Collection<ServerPlayer> players = PlayerLookup.around(level, player.getEyePosition(), EyeSpy.CONFIG.instance().ping.notifyRangeBlocks);

        var existing = LieManager.getBlockHighlight(blockHit.getBlockPos());

        if (existing.isPresent()) {
            if (player.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
                existing.get().lie().fade();
                Sounds.playWarn(players, blockHit.getBlockPos().getCenter());
                LieManager.createBlock(level, players, blockHit.getBlockPos(), true);
            } else {
                Sounds.playBlock(players, blockHit, level.getBlockState(blockHit.getBlockPos()));
                existing.get().refreshLifetime();
            }

            return;
        }

        Sounds.playBlock(players, blockHit, level.getBlockState(blockHit.getBlockPos()));
        LieManager.createBlock(level, players, blockHit.getBlockPos(), false);
    }

    private static void onEntity(ServerPlayer player, EntityHitResult entityHit) {
        EyeSpy.LOGGER.debug("Result: ENTITY {}", entityHit.getEntity().getClass().getSimpleName());

        ServerLevel level = player.serverLevel();
        Collection<ServerPlayer> players = PlayerLookup.tracking(entityHit.getEntity());

        var existing = LieManager.getEntityHighlight(entityHit.getEntity());

        if (existing.isPresent()) {
            if (player.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
                existing.get().lie().fade();
                Sounds.playWarn(players, entityHit.getEntity().getEyePosition());
                LieManager.createEntity(level, players, entityHit.getEntity(), true);
            } else {
                Sounds.playEntity(players, entityHit);
                existing.get().refreshLifetime();
            }
            return;
        }

        Sounds.playEntity(players, entityHit);
        LieManager.createEntity(level, players, entityHit.getEntity(), false);
    }
}
