package red.jackf.eyespy.ping.lies;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyColours;
import red.jackf.eyespy.ping.Ping;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BlockHighlight implements Highlight {
    private final ServerLevel level;
    private final BlockPos pos;
    private final boolean warning;
    private final EntityLie<Display.BlockDisplay> lie;
    private final Map<ServerPlayer, PingLieText> texts = new HashMap<>();
    private Colour baseColour = Colours.WHITE;
    private long lastRefreshed = -1;

    public static BlockHighlight create(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer pinger,
            Collection<ServerPlayer> viewers,
            boolean warning) {
        return new BlockHighlight(level, pos, pinger, viewers, warning);
    }

    private BlockHighlight(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer pinger,
            Collection<ServerPlayer> viewers,
            boolean warning) {
        this.level = level;
        this.pos = pos;
        this.warning = warning;
        this.lie = EntityLie.builder(makeDisplay(level, pos, warning))
                            .onTick(this::tick)
                            .onFade((viewer, lie2) -> {
                                LieManager.onFade(pinger, viewer, this);
                                var playerText = this.texts.get(viewer);
                                if (playerText != null) playerText.stop();
                            })
                            .createAndShow(viewers);

        if (Ping.pingTextEnabled())
            for (ServerPlayer viewer : viewers) {
                texts.put(viewer, new PingLieText(viewer, pos, level.getBlockState(pos)));
            }

        this.refreshLifetime();
        this.setLatestColour();
    }

    private Display.BlockDisplay makeDisplay(ServerLevel level, BlockPos pos, boolean warning) {
        BlockState state = level.getBlockState(pos);

        //noinspection DataFlowIssue
        this.baseColour = warning ? Colour.fromInt(ChatFormatting.RED.getColor()) : EyeSpyColours.getForBlock(state);

        return EntityBuilders.blockDisplay(level)
                             .state(state)
                             .positionCentered(pos)
                             .scaleAndCenter(0.98f)
                             .brightness(15, 15)
                             .viewRangeModifier(5f)
                             .glowing(true, baseColour)
                             .build();
    }

    private void flashWarning(ServerPlayer player) {
        long timeSinceLast = player.level().getGameTime() - this.lastRefreshed;
        this.lie.entity().setGlowingTag((timeSinceLast / Constants.FLASH_INTERVAL) % 2 == 0);
    }

    private void tick(ServerPlayer player, EntityLie<Display.BlockDisplay> lie) {
        if (this.warning) this.flashWarning(player);

        EntityUtils.setDisplayBlockState(this.lie.entity(), this.level.getBlockState(this.pos));
    }

    public void fade() {
        this.lie.fade();
        this.texts.values().forEach(PingLieText::stop);
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

    public void setNormalColour() {
        EntityUtils.setDisplayGlowOverride(this.lie.entity(), this.baseColour);
    }

    public void setLatestColour() {
        if (this.warning) this.setNormalColour();
        else EntityUtils.setDisplayGlowOverride(this.lie.entity(), this.baseColour.lerp(Colours.WHITE, 0.4f));
    }
}
