package red.jackf.eyespy.raycasting;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import red.jackf.eyespy.EyeSpy;

import java.util.Optional;

public class Raycasting {
    public static HitResult cast(ServerPlayer player) {
        Optional<EntityHitResult> entityOpt = tryEntity(player);
        BlockHitResult block = tryBlock(player);
        if (entityOpt.isEmpty()) return block;
        EntityHitResult entity = entityOpt.get();

        double entityDistance = entity.getLocation().distanceToSqr(player.getEyePosition());
        double blockDistance = block.getLocation().distanceToSqr(player.getEyePosition());

        if (entityDistance > blockDistance) return block;
        else return entity;
    }

    private static Optional<EntityHitResult> tryEntity(ServerPlayer player) {
        final double maxDistance = EyeSpy.CONFIG.instance().maxRangeBlocks;

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

    private static boolean isValidEntity(Entity entity, ServerPlayer player) {
        return entity.isPickable() && !entity.isInvisibleTo(player);
    }

    private static BlockHitResult tryBlock(ServerPlayer player) {
        return pick(player, EyeSpy.CONFIG.instance().maxRangeBlocks, false);
    }

    public static BlockHitResult pick(ServerPlayer player, Vec3 to, boolean hitFluids) {
        Vec3 from = player.getEyePosition();

        if (!EyeSpy.CONFIG.instance().loadChunks) {
            BlockPos pos = BlockPos.containing(to);
            Vec3 normalized = to.subtract(from).normalize().scale(4);

            while (!player.level().isLoaded(pos)) {
                to = to.subtract(normalized);
                if (to.distanceToSqr(from) < 256) break; // shrink until within 16 blocks
                pos = BlockPos.containing(to);
            }
        }

        return player.level().clip(new CustomClipContext(from, to, player, hitFluids));
    }

    public static BlockHitResult pick(ServerPlayer player, double maxDistance, boolean hitFluids) {
        Vec3 from = player.getEyePosition();
        Vec3 direction = player.getViewVector(1);
        Vec3 to = from.add(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance);

        return pick(player, to, hitFluids);
    }
}
