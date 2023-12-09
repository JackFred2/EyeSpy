package red.jackf.eyespy.ping.lies;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyColours;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;

import java.util.Collection;

public final class BlockHighlight implements Highlight {
    private final BlockPos pos;
    private final EntityLie<Display.BlockDisplay> lie;
    private long lastRefreshed = -1;

    private BlockHighlight(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer pinger,
            Collection<ServerPlayer> viewers,
            boolean warning) {
        this.pos = pos;
        this.lie = EntityLie.builder(makeDisplay(level, pos, warning))
                            .onTick(warning ? this::flashWarning : null)
                            .onFade((viewer, lie2) -> LieManager.onFade(pinger, viewer, this))
                            .createAndShow(viewers);

        this.refreshLifetime();
    }

    private static Display.BlockDisplay makeDisplay(ServerLevel level, BlockPos pos, boolean warning) {
        BlockState state = level.getBlockState(pos);

        //noinspection DataFlowIssue
        return EntityBuilders.blockDisplay(level)
                             .state(state)
                             .positionCentered(pos)
                             .scaleAndCenter(0.98f)
                             .brightness(15, 15)
                             .viewRangeModifier(5f)
                             .glowing(true, warning ? ChatFormatting.RED.getColor() : EyeSpyColours.getForBlock(state).toARGB())
                             .build();
    }

    public static BlockHighlight create(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer pinger,
            Collection<ServerPlayer> viewers,
            boolean warning) {
        return new BlockHighlight(level, pos, pinger, viewers, warning);
    }

    private void flashWarning(ServerPlayer player, EntityLie<Display.BlockDisplay> lie) {
        long timeSinceLast = player.level().getGameTime() - this.lastRefreshed;
        this.lie.entity().setGlowingTag((timeSinceLast / Constants.FLASH_INTERVAL) % 2 == 0);
    }

    public void fade() {
        this.lie.fade();
    }

    public void refreshLifetime() {
        this.lastRefreshed = lie.entity().level().getGameTime();
        Debris.INSTANCE.schedule(lie, EyeSpy.CONFIG.instance().ping.lifetimeTicks);
    }

    public long getLastRefreshed() {
        return lastRefreshed;
    }

    public BlockPos pos() {
        return pos;
    }
}
