package red.jackf.eyespy.lies;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

public class BlockHighlight {
    private final ServerPlayer owner;
    private final EntityLie<Display.BlockDisplay> lie;

    public BlockHighlight(ServerPlayer owner, EntityLie<Display.BlockDisplay> lie) {
        this.owner = owner;
        this.lie = lie;
    }

    public EntityLie<Display.BlockDisplay> lie() {
        return lie;
    }

    public void refreshLifetime() {
        Debris.INSTANCE.schedule(lie, EyeSpy.HIGHLIGHT_LIFETIME);
    }
}
