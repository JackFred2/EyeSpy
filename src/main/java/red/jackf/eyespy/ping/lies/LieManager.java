package red.jackf.eyespy.ping.lies;

import com.mojang.authlib.GameProfile;
import io.netty.util.internal.EmptyPriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import red.jackf.eyespy.EyeSpy;

import java.util.*;

public class LieManager {
    private static final Map<GameProfile, Queue<Highlight>> LAST_PINGS = new HashMap<>();

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

    public static void createBlock(ServerLevel level, BlockPos pos, ServerPlayer pinger, Collection<ServerPlayer> viewers, boolean warning) {
        addHighlight(pinger, BlockHighlight.create(level, pos, pinger, viewers, warning));
    }

    public static void createEntity(ServerPlayer pinger, Collection<ServerPlayer> viewers, Entity entity, boolean warning) {
        addHighlight(pinger, EntityHighlight.create(entity, pinger, viewers, warning));
    }

    private static void addHighlight(ServerPlayer pinger, Highlight highlight) {
        var queue = LAST_PINGS.computeIfAbsent(pinger.getGameProfile(), prof -> new ArrayDeque<>());

        queue.add(highlight);

        while (queue.size() > EyeSpy.CONFIG.instance().ping.maxPings) {
            var oversized = queue.poll();
            if (oversized == null) break;
            oversized.fade();
        }
    }

    public static void bump(ServerPlayer pinger, Highlight highlight) {
        var highlights = getHighlights(pinger);
        if (highlights.remove(highlight)) {
            highlights.add(highlight);
            highlight.refreshLifetime();
        }
    }

    public static void onFade(ServerPlayer pinger, ServerPlayer viewer, Highlight highlight) {
        if (viewer == pinger) {
            var queue = getHighlights(pinger);
            queue.remove(highlight);
            if (queue.isEmpty()) {
                LAST_PINGS.remove(pinger.getGameProfile());
            }
        }
    }
}
