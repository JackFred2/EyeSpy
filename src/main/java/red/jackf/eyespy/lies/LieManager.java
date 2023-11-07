package red.jackf.eyespy.lies;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import red.jackf.eyespy.HighlightColours;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LieManager {
    public static final Map<BlockPos, BlockHighlight> BLOCKS = new HashMap<>();
    public static final Map<Entity, EntityHighlight> ENTITIES = new HashMap<>();

    public static Optional<BlockHighlight> getBlockHighlight(BlockPos pos) {
        return Optional.ofNullable(BLOCKS.get(pos));
    }

    public static Optional<EntityHighlight> getEntityHighlight(Entity entity) {
        return Optional.ofNullable(ENTITIES.get(entity));
    }

    public static void fadeEverything(ServerPlayer player) {
        BLOCKS.values().stream()
              .filter(highlight -> highlight.lie().getViewingPlayers().contains(player))
              .forEach(highlight -> highlight.lie().removePlayer(player));
        ENTITIES.values().stream()
              .filter(highlight -> highlight.lie().getViewingPlayers().contains(player))
              .forEach(highlight -> highlight.lie().removePlayer(player));
    }

    public static void createBlock(ServerLevel level, Collection<ServerPlayer> players, BlockPos pos, boolean flashRed) {
        BlockState state = level.getBlockState(pos);

        boolean startGlowing = !flashRed || getCurrentColour(level) != null;

        //noinspection DataFlowIssue
        var display = EntityBuilders.blockDisplay(level)
                                    .state(state)
                                    .positionCentered(pos)
                                    .scaleAndCenter(0.98f)
                                    .brightness(15, 15)
                                    .viewRangeModifier(5f)
                                    .glowing(startGlowing, flashRed ? ChatFormatting.RED.getColor() : HighlightColours.getForBlock(state).toARGB())
                                    .build();

        var lie = EntityLie.builder(display)
                           .onFade(LieManager::onBlockFade)
                           .onTick(flashRed ? LieManager::flashRed : null)
                           .createAndShow(players);

        var holder = new BlockHighlight(lie);
        holder.refreshLifetime();

        BLOCKS.put(pos, holder);
    }

    public static void createEntity(ServerLevel level, Collection<ServerPlayer> players, Entity entity, boolean flashRed) {
        var colour = flashRed ? getCurrentColour(level) : HighlightColours.getForEntity(entity);

        var lie = EntityGlowLie.builder(entity)
                               .colour(colour)
                               .onFade(LieManager::onEntityFade)
                               .onTick(flashRed ? LieManager::flashRed : null)
                               .createAndShow(players);

        var holder = new EntityHighlight(lie);
        holder.refreshLifetime();

        ENTITIES.put(entity, holder);
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

    private static void onBlockFade(ServerPlayer ignored, EntityLie<Display.BlockDisplay> lie) {
        if (lie.hasFaded())
            BLOCKS.values().removeIf(highlight -> highlight.lie() == lie);
    }

    private static void onEntityFade(ServerPlayer ignored, EntityGlowLie<? extends Entity> lie) {
        if (lie.hasFaded())
            ENTITIES.values().removeIf(highlight -> highlight.lie() == lie);
    }
}
