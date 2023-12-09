package red.jackf.eyespy.ping.lies;

import com.mojang.authlib.GameProfile;
import io.netty.util.internal.EmptyPriorityQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyColours;
import red.jackf.jackfredlib.api.lying.Lie;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

import java.util.*;

public class LieManager {
    public static final Map<GameProfile, Queue<Highlight>> LAST_PINGS = new HashMap<>();

    private static Queue<Highlight> getHighlights(ServerPlayer player) {
        return LAST_PINGS.getOrDefault(player.getGameProfile(), EmptyPriorityQueue.instance());
    }

    public static Optional<BlockHighlight> getBlockHighlight(ServerPlayer sourcePlayer, BlockPos pos) {
        return getHighlights(sourcePlayer).stream()
                .filter(highlight -> highlight instanceof BlockHighlight block && block.pos().equals(pos))
                .map(highlight -> (BlockHighlight) highlight)
                .findFirst();
    }

    public static Optional<EntityHighlight> getEntityHighlight(ServerPlayer sourcePlayer, Entity entity) {
        return getHighlights(sourcePlayer).stream()
                          .filter(highlight -> highlight instanceof EntityHighlight entityHighlight && entityHighlight.entity() == entity)
                          .map(highlight -> (EntityHighlight) highlight)
                          .findFirst();
    }

    public static void fadeEverything(ServerPlayer player) {
        var playerQueue = LAST_PINGS.remove(player.getGameProfile());
        if (playerQueue == null) return;

        playerQueue.forEach(highlight -> highlight.lie().fade());
    }

    public static void createBlock(ServerLevel level, ServerPlayer pinger, Collection<ServerPlayer> viewers, BlockPos pos, boolean flashRed) {
        BlockState state = level.getBlockState(pos);

        boolean startGlowing = !flashRed || getCurrentColour(level) != null;

        //noinspection DataFlowIssue
        var display = EntityBuilders.blockDisplay(level)
                                    .state(state)
                                    .positionCentered(pos)
                                    .scaleAndCenter(0.98f)
                                    .brightness(15, 15)
                                    .viewRangeModifier(5f)
                                    .glowing(startGlowing, flashRed ? ChatFormatting.RED.getColor() : EyeSpyColours.getForBlock(state).toARGB())
                                    .build();

        var lie = EntityLie.builder(display)
                           .onFade((viewer, lie2) -> onFade(pinger, viewer, lie2))
                           .onTick(flashRed ? LieManager::flashRed : null)
                           .createAndShow(viewers);

        addHighlight(pinger, new BlockHighlight(pos, lie));
    }

    public static void createEntity(ServerLevel level, ServerPlayer pinger, Collection<ServerPlayer> viewers, Entity entity, boolean flashRed) {
        var colour = flashRed ? getCurrentColour(level) : EyeSpyColours.getForEntity(entity);

        var lie = EntityGlowLie.builder(entity)
                               .colour(colour)
                               .onFade((viewer, lie2) -> onFade(pinger, viewer, lie2))
                               .onTick(flashRed ? LieManager::flashRed : null)
                               .createAndShow(viewers);

        addHighlight(pinger, new EntityHighlight(entity, lie));
    }

    private static void addHighlight(ServerPlayer pinger, Highlight highlight) {
        var queue = LAST_PINGS.computeIfAbsent(pinger.getGameProfile(), prof -> new ArrayDeque<>());

        queue.add(highlight);

        highlight.refreshLifetime();

        while (queue.size() > EyeSpy.CONFIG.instance().ping.maxPings) {
            var oversized = queue.poll();
            if (oversized == null) break;
            oversized.lie().fade();
        }
    }

    private static @Nullable ChatFormatting getCurrentColour(ServerLevel level) {
        return (level.getGameTime() / Constants.FLASH_INTERVAL) % 2 == 0 ? ChatFormatting.RED : null;
    }

    private static void flashRed(ServerPlayer player, EntityLie<Display.BlockDisplay> lie) {
        var colour = getCurrentColour(player.getLevel());

        // TODO: make JackFredLib change display entity colour via override instead of teams, should prevent desync when
        //       flashing displays
        lie.entity().setGlowingTag(colour != null);
    }

    private static void flashRed(ServerPlayer player, EntityGlowLie<? extends Entity> lie) {
        lie.setGlowColour(getCurrentColour(player.getLevel()));
    }

    private static void onFade(ServerPlayer pinger, ServerPlayer viewer, Lie lie) {
        if (lie.hasFaded() && pinger == viewer) {
            var iter = getHighlights(pinger).iterator();
            Highlight highlight;
            while (iter.hasNext()) {
                highlight = iter.next();
                if (highlight.lie() == lie) {
                    iter.remove();
                }
            }
        }
    }

    public static void bump(ServerPlayer pinger, Highlight highlight) {
        var highlights = getHighlights(pinger);
        if (highlights.remove(highlight)) {
            highlights.add(highlight);
            highlight.refreshLifetime();
        }
    }
}
