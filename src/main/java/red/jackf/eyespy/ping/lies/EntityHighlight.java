package red.jackf.eyespy.ping.lies;

import net.minecraft.world.entity.Entity;
import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

public final class EntityHighlight implements Highlight {
    private final Entity entity;
    private final EntityGlowLie<? extends Entity> lie;
    private long lastRefreshed = -1;

    public EntityHighlight(Entity entity, EntityGlowLie<? extends Entity> lie) {
        this.entity = entity;
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

    public Entity entity() {
        return entity;
    }
}
