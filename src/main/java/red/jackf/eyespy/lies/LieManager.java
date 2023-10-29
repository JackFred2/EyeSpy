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
import org.jetbrains.annotations.Nullable;
import red.jackf.eyespy.Colours;
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

    public static void createBlock(ServerPlayer player, BlockPos pos, boolean flashRed) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);

        boolean startGlowing = !flashRed || getCurrentColour(level) != null;

        //noinspection DataFlowIssue
        var display = EntityBuilders.blockDisplay(level)
                                    .state(state)
                                    .positionCentered(pos)
                                    .scaleAndCenter(0.98f)
                                    .brightness(15, 15)
                                    .viewRangeModifier(5f)
                                    .glowing(startGlowing, flashRed ? ChatFormatting.RED.getColor() : Colours.getForBlock(state).toARGB())
                                    .build();

        var lie = EntityLie.builder(display)
                           .onFade(LieManager::onBlockFade)
                           .onTick(flashRed ? LieManager::flashRed : null)
                           .createAndShow(player);

        var holder = new BlockHighlight(lie);
        holder.refreshLifetime();

        BLOCKS.put(player.getGameProfile(), holder);
    }

    public static void createEntity(ServerPlayer player, Entity entity, boolean flashRed) {
        var colour = flashRed ? getCurrentColour(player.serverLevel()) : Colours.getForEntity(entity);

        var lie = EntityGlowLie.builder(entity)
                               .colour(colour)
                               .onFade(LieManager::onEntityFade)
                               .onTick(flashRed ? LieManager::flashRed : null)
                               .createAndShow(player);

        var holder = new EntityHighlight(lie);
        holder.refreshLifetime();

        ENTITIES.put(player.getGameProfile(), holder);
    }

    private static @Nullable ChatFormatting getCurrentColour(ServerLevel level) {
        return (level.getGameTime() / Constants.FLASH_INTERVAL) % 2 == 0 ? ChatFormatting.RED : null;
    }

    private static void flashRed(ServerPlayer player, EntityLie<Display.BlockDisplay> lie) {
        var colour = getCurrentColour(player.serverLevel());

        // TODO: make JackFredLib change display entity colour via override instead of teams, should prevent desync when
        //       flashing displays
        lie.entity().setGlowingTag(colour != null);
    }

    private static void flashRed(ServerPlayer player, EntityGlowLie<? extends Entity> lie) {
        lie.setGlowColour(getCurrentColour(player.serverLevel()));
    }

    private static void onBlockFade(ServerPlayer player, EntityLie<Display.BlockDisplay> lie) {
        BLOCKS.get(player.getGameProfile()).removeIf(highlight -> highlight.lie() == lie);
    }

    private static void onEntityFade(ServerPlayer player, EntityGlowLie<? extends Entity> lie) {
        ENTITIES.get(player.getGameProfile()).removeIf(highlight -> highlight.lie() == lie);
    }
}
