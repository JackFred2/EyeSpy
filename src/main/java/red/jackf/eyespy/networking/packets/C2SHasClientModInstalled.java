package red.jackf.eyespy.networking.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import red.jackf.eyespy.EyeSpy;

/**
 * <p>Used to tell the server we have the mod locally, disables the swap hand override.</p>
 *
 * <p>Nothing in the buffer.</p>
 */
public record C2SHasClientModInstalled() implements FabricPacket {
    public static final PacketType<C2SHasClientModInstalled> TYPE = PacketType.create(EyeSpy.id("has_client_mod_installed"), C2SHasClientModInstalled::new);

    public C2SHasClientModInstalled(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
