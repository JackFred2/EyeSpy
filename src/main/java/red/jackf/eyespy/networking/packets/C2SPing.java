package red.jackf.eyespy.networking.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import red.jackf.eyespy.EyeSpy;

/**
 * <p>Ask the server to ping a location. Currently raycasted, in future may supply position in packet</p>
 *
 * <p>Nothing in buffer</p>
 */
public final class C2SPing implements CustomPacketPayload {
    public static final C2SPing INSTANCE = new C2SPing();

    public static final Type<C2SPing> TYPE = new Type<>(EyeSpy.id("ping"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPing> CODEC = StreamCodec.unit(INSTANCE);

    private C2SPing() {
    }

    @Override
    public @NotNull Type<C2SPing> type() {
        return TYPE;
    }
}
