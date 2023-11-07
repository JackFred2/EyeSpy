package red.jackf.eyespy;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

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

    private static boolean isValidEntity(Entity entity, ServerPlayer player) {
        return entity.isPickable() && !entity.isInvisibleTo(player);
    }

    private static BlockHitResult tryBlock(ServerPlayer player) {
        return pick(player, EyeSpy.CONFIG.instance().ping.maxRangeBlocks);
    }

    private static BlockHitResult pick(ServerPlayer player, double maxDistance) {
        Vec3 vec3 = player.getEyePosition();
        Vec3 vec32 = player.getViewVector(0);
        Vec3 vec33 = vec3.add(vec32.x * maxDistance, vec32.y * maxDistance, vec32.z * maxDistance);

        ClipContext.Block blockHit = ClipContext.Block.VISUAL;
        ClipContext.Fluid fluidHit = ClipContext.Fluid.NONE;

        return player.level().clip(new ClipContext(vec3, vec33, blockHit, fluidHit, player));
    }
}
