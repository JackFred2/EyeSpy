package red.jackf.eyespy.lies;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

public class EntityHighlight {
    private final ServerPlayer owner;
    private final EntityGlowLie<? extends Entity> lie;

    public EntityHighlight(ServerPlayer owner, EntityGlowLie<? extends Entity> lie) {
        this.owner = owner;
        this.lie = lie;
    }

    public EntityGlowLie<? extends Entity> lie() {
        return lie;
    }

    public void refreshLifetime() {
        Debris.INSTANCE.schedule(lie, EyeSpy.HIGHLIGHT_LIFETIME);
    }
}
