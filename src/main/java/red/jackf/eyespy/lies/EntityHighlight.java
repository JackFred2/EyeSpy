package red.jackf.eyespy.lies;

import net.minecraft.world.entity.Entity;
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
        Debris.INSTANCE.schedule(lie, Constants.HIGHLIGHT_LIFETIME);
    }

    public long getLastRefreshed() {
        return lastRefreshed;
    }
}
