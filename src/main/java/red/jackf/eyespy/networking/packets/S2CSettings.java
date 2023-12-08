package red.jackf.eyespy.networking.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import red.jackf.eyespy.EyeSpy;

/**
 * <p>Tells the client what the current server settings are; this is used for the toast saying how to use the mod.</p>
 *
 * <p>Packet Structure:
 * <ol>
 *     <li>pingEnabled: boolean</li>
 *     <li>pingRequiresZoom: boolean</li>
 * </ol></p>
 * @param pingEnabled
 * @param pingRequiresZoom
 */
public record S2CSettings(boolean pingEnabled, boolean pingRequiresZoom) implements FabricPacket {
    public static final PacketType<S2CSettings> TYPE = PacketType.create(EyeSpy.id("server_settings"), S2CSettings::new);

    public S2CSettings(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean());
    }

    public static S2CSettings create() {
        var config = EyeSpy.CONFIG.instance();
        return new S2CSettings(config.ping.enabled, config.ping.requiresZoomIn);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(pingEnabled);
        buf.writeBoolean(pingRequiresZoom);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
