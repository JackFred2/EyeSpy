package red.jackf.eyespy.ping.lies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

public final class BlockHighlight implements Highlight {
    private final BlockPos pos;
    private final EntityLie<Display.BlockDisplay> lie;
    private long lastRefreshed = -1;

    public BlockHighlight(BlockPos pos, EntityLie<Display.BlockDisplay> lie) {
        this.pos = pos;
        this.lie = lie;
    }

    public EntityLie<Display.BlockDisplay> lie() {
        return lie;
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
