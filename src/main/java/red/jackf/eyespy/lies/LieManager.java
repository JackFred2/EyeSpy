package red.jackf.eyespy.lies;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

import java.util.Optional;

public class LieManager {
    private static final Multimap<GameProfile, BlockHighlight> BLOCKS =
            MultimapBuilder.hashKeys().hashSetValues().build();
    private static final Multimap<GameProfile, EntityHighlight> ENTITIES =
            MultimapBuilder.hashKeys().hashSetValues().build();

    public static Optional<BlockHighlight> getBlockHighlight(ServerPlayer player, BlockPos pos) {
        return BLOCKS.get(player.getGameProfile())
                     .stream()
                     .filter(holder -> holder.lie().entity().blockPosition().equals(pos))
                     .findFirst();
    }

    public static Optional<EntityHighlight> getEntityHighlight(ServerPlayer player, Entity entity) {
        return ENTITIES.get(player.getGameProfile())
                     .stream()
                     .filter(holder -> holder.lie().entity() == entity)
                     .findFirst();
    }

    public static void fadeEverything(ServerPlayer player) {
        BLOCKS.removeAll(player.getGameProfile()).forEach(highlight -> highlight.lie().fade());
        ENTITIES.removeAll(player.getGameProfile()).forEach(highlight -> highlight.lie().fade());
    }

    public static void createBlock(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);

        var colour = Colour.fromInt(state.getBlock().defaultMapColor().col).scaleBrightness(1.25F);

        var display = EntityBuilders.blockDisplay(level)
                                    .state(state)
                                    .positionCentered(pos)
                                    .scaleAndCenter(0.99f)
                                    .brightness(15, 15)
                                    .viewRangeModifier(5f)
                                    .glowing(true, colour)
                                    .build();

        var lie = EntityLie.builder(display)
                           .onFade(LieManager::onBlockFade)
                           .createAndShow(player);

        var holder = new BlockHighlight(player, lie);
        holder.refreshLifetime();

        BLOCKS.put(player.getGameProfile(), holder);
    }

    public static void createEntity(ServerPlayer player, Entity entity) {
        var colour = ChatFormatting.GREEN;

        var lie = EntityGlowLie.builder(entity)
                               .colour(colour)
                               .onFade(LieManager::onEntityFade)
                               .createAndShow(player);

        var holder = new EntityHighlight(player, lie);
        holder.refreshLifetime();

        ENTITIES.put(player.getGameProfile(), holder);
    }

    private static void onBlockFade(ServerPlayer player, EntityLie<Display.BlockDisplay> lie) {
        BLOCKS.get(player.getGameProfile()).removeIf(highlight -> highlight.lie() == lie);
    }

    private static void onEntityFade(ServerPlayer player, EntityGlowLie<? extends Entity> lie) {
        ENTITIES.get(player.getGameProfile()).removeIf(highlight -> highlight.lie() == lie);
    }
}
