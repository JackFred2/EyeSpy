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
     * Check if a pinger can ping given their current state and mod settings.
     * @param player Player trying to ping
     * @return Whether the pinger can ping at this time.
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

        long startTime = System.nanoTime();

        HitResult hit = Raycasting.cast(player);

        switch (hit.getType()) {
            case MISS -> onMiss(player);
            case BLOCK -> onBlock(player, (BlockHitResult) hit);
            case ENTITY -> onEntity(player, (EntityHitResult) hit);
        }

        EyeSpy.LOGGER.debug("Time taken: {}ns", System.nanoTime() - startTime);
    }

    private static void onMiss(ServerPlayer player) {
        EyeSpy.LOGGER.debug("Result: MISS");

        Sounds.playMiss(player);
    }

    private static void onBlock(ServerPlayer pinger, BlockHitResult blockHit) {
        EyeSpy.LOGGER.debug("Result: BLOCK {}", blockHit.getBlockPos().toShortString());

        ServerLevel level = pinger.serverLevel();
        Collection<ServerPlayer> viewing = PlayerLookup.around(level, pinger.getEyePosition(), EyeSpy.CONFIG.instance().ping.notifyRangeBlocks);

        LieManager.resetBlockHighlightColours(pinger);

        var existing = LieManager.getBlockHighlight(pinger, blockHit.getBlockPos());

        if (existing.isPresent()) {
            if (pinger.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
                existing.get().fade();
                Sounds.playWarn(viewing, blockHit.getBlockPos().getCenter());
                LieManager.createBlock(level, blockHit.getBlockPos(), pinger, viewing, true);
            } else {
                Sounds.playBlock(viewing, blockHit, level.getBlockState(blockHit.getBlockPos()));
                LieManager.bump(pinger, existing.get());
            }

            return;
        }

        Sounds.playBlock(viewing, blockHit, level.getBlockState(blockHit.getBlockPos()));
        LieManager.createBlock(level, blockHit.getBlockPos(), pinger, viewing, false);
    }

    private static void onEntity(ServerPlayer pinger, EntityHitResult entityHit) {
        EyeSpy.LOGGER.debug("Result: ENTITY {}", entityHit.getEntity().getClass().getSimpleName());

        Collection<ServerPlayer> viewing = PlayerLookup.tracking(entityHit.getEntity());

        var existing = LieManager.getEntityHighlight(pinger, entityHit.getEntity());

        if (existing.isPresent()) {
            if (pinger.level().getGameTime() - existing.get().getLastRefreshed() <= Constants.DOUBLE_TAP_INTERVAL) {
                existing.get().fade();
                Sounds.playWarn(viewing, entityHit.getEntity().getEyePosition());
                LieManager.createEntity(pinger, viewing, entityHit.getEntity(), true);
            } else {
                Sounds.playEntity(viewing, entityHit);
                LieManager.bump(pinger, existing.get());
            }
            return;
        }

        Sounds.playEntity(viewing, entityHit);
        LieManager.createEntity(pinger, viewing, entityHit.getEntity(), false);
    }

    public static boolean pingTextEnabled() {
        return EyeSpy.CONFIG.instance().ping.showDescriptionText || EyeSpy.CONFIG.instance().ping.showDistanceText;
    }
}
