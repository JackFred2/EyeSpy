package red.jackf.eyespy.ping.lies;

import net.minecraft.world.entity.Entity;
import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

public class EntityHighlight {
    private final EntityGlowLie<? extends Entity> lie;
    private long lastRefreshed = -1;

    public EntityHighlight(EntityGlowLie<? extends Entity> lie) {
        this.lie = lie;
    }

    public EntityGlowLie<? extends Entity> lie() {
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
