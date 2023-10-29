package red.jackf.eyespy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Raycasting {
    private static final int MAX_DISTANCE = 128;

    public static HitResult cast(ServerPlayer player) {
        return tryEntity(player).orElseGet(() -> tryBlock(player));
    }

    private static Optional<HitResult> tryEntity(ServerPlayer player) {
        Vec3 start = player.getEyePosition(1f);
        Vec3 offset = player.getViewVector(1f).scale(MAX_DISTANCE);
        Vec3 end = start.add(offset);
        AABB aabb = player.getBoundingBox().expandTowards(offset).inflate(1);
        return Optional.ofNullable(ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                aabb,
                entity -> isValidEntity(entity, player),
                MAX_DISTANCE * MAX_DISTANCE
        ));
    }

    private static HitResult tryBlock(ServerPlayer player) {
        return player.pick(MAX_DISTANCE, 1f, false);
    }

    private static boolean isValidEntity(Entity entity, ServerPlayer player) {
        return entity.isPickable() && !entity.isInvisibleTo(player);
    }
}
