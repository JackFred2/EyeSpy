package red.jackf.eyespy.lies;

import net.minecraft.world.entity.Display;
import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

public class BlockHighlight {
    private final EntityLie<Display.BlockDisplay> lie;
    private long lastRefreshed = -1;

    public BlockHighlight(EntityLie<Display.BlockDisplay> lie) {
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
}
