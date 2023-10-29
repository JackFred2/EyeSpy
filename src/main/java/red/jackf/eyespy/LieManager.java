package red.jackf.eyespy;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import red.jackf.jackfredlib.api.lying.Lie;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

import java.util.Optional;

public class LieManager {
    private static final Multimap<GameProfile, EntityLie<Display.BlockDisplay>> BLOCKS =
            MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<GameProfile, EntityGlowLie<? extends Entity>> ENTITIES =
            MultimapBuilder.hashKeys().hashSetValues().build();

    public static Optional<EntityLie<Display.BlockDisplay>> getBlockHighlight(ServerPlayer player, BlockPos pos) {
        return BLOCKS.get(player.getGameProfile())
                     .stream()
                     .filter(lie -> lie.entity().blockPosition().equals(pos))
                     .findFirst();
    }

    public static Optional<EntityGlowLie<? extends Entity>> getEntityHighlight(ServerPlayer player, Entity entity) {
        return ENTITIES.get(player.getGameProfile())
                     .stream()
                     .filter(lie -> lie.entity() == entity)
                     .findFirst();
    }

    public static void fade(ServerPlayer player) {
        BLOCKS.removeAll(player.getGameProfile()).forEach(Lie::fade);
        ENTITIES.removeAll(player.getGameProfile()).forEach(Lie::fade);
    }

    public static void addBlock(ServerPlayer player, EntityLie<Display.BlockDisplay> entityLie) {
        BLOCKS.put(player.getGameProfile(), entityLie);
    }

    public static void addEntity(ServerPlayer player, EntityGlowLie<? extends Entity> entityLie) {
        ENTITIES.put(player.getGameProfile(), entityLie);
    }

    public static void removeBlock(ServerPlayer player, EntityLie<Display.BlockDisplay> entityLie) {
        BLOCKS.remove(player.getGameProfile(), entityLie);
    }

    public static void removeEntity(ServerPlayer player, EntityGlowLie<? extends Entity> entityLie) {
        ENTITIES.remove(player.getGameProfile(), entityLie);
    }
}
