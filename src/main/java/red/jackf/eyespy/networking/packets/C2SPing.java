package red.jackf.eyespy.networking.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import red.jackf.eyespy.EyeSpy;

/**
 * <p>Ask the server to ping a location. Currently raycasted, in future may supply position in packet</p>
 *
 * <p>Nothing in buffer</p>
 */
public record C2SPing() implements FabricPacket {
    public static PacketType<C2SPing> TYPE = PacketType.create(EyeSpy.id("ping"), C2SPing::new);

    public C2SPing(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
