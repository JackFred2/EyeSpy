package red.jackf.eyespy;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Raycasting {
    public static HitResult cast(ServerPlayer player) {
        return tryEntity(player).orElseGet(() -> tryBlock(player));
    }

    private static Optional<HitResult> tryEntity(ServerPlayer player) {
        final double maxDistance = EyeSpy.CONFIG.instance().ping.maxRangeBlocks;

        Vec3 start = player.getEyePosition(1f);
        Vec3 offset = player.getViewVector(1f).scale(maxDistance);
        Vec3 end = start.add(offset);
        AABB aabb = player.getBoundingBox().expandTowards(offset).inflate(1);
        return Optional.ofNullable(ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                aabb,
                entity -> isValidEntity(entity, player) && PlayerLookup.tracking(entity).contains(player),
                maxDistance * maxDistance
        ));
    }

    private static HitResult tryBlock(ServerPlayer player) {
        return player.pick(EyeSpy.CONFIG.instance().ping.maxRangeBlocks, 1f, false);
    }

    private static boolean isValidEntity(Entity entity, ServerPlayer player) {
        return entity.isPickable() && !entity.isInvisibleTo(player);
    }
}
