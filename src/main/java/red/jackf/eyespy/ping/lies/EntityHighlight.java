package red.jackf.eyespy.ping.lies;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyColours;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.glowing.EntityGlowLie;

import java.util.Collection;

public final class EntityHighlight implements Highlight {
    private final Entity entity;
    private final EntityGlowLie<? extends Entity> lie;
    private long lastRefreshed = -1;

    private EntityHighlight(
            Entity entity,
            ServerPlayer pinger,
            Collection<ServerPlayer> viewers,
            boolean warning) {
        this.entity = entity;
        this.lie = EntityGlowLie.builder(entity)
                                .colour(warning ? ChatFormatting.RED : EyeSpyColours.getForEntity(entity))
                                .onTick(warning ? this::flashWarning : null)
                                .onFade((viewer, lie2) -> LieManager.onFade(pinger, viewer, this))
                                .createAndShow(viewers);

        this.refreshLifetime();
    }

    private void flashWarning(ServerPlayer player, EntityGlowLie<Entity> entityEntityGlowLie) {
        long timeSinceLast = player.getLevel().getGameTime() - this.lastRefreshed;
        this.lie.setGlowColour((timeSinceLast / Constants.FLASH_INTERVAL) % 2 == 0 ? ChatFormatting.RED : null);
    }

    public static Highlight create(
            Entity entity,
            ServerPlayer pinger,
            Collection<ServerPlayer> viewers,
            boolean warning) {
        return new EntityHighlight(entity, pinger, viewers, warning);
    }

    public void fade() {
        this.lie.fade();
    }

    public void refreshLifetime() {
        this.lastRefreshed = lie.entity().getLevel().getGameTime();
        Debris.INSTANCE.schedule(lie, EyeSpy.CONFIG.instance().ping.lifetimeTicks);
    }

    public long getLastRefreshed() {
        return lastRefreshed;
    }

    public Entity entity() {
        return entity;
    }
}
