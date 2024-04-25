package red.jackf.eyespy.networking.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import red.jackf.eyespy.EyeSpy;

/**
 * <p>Used to tell the server we have the mod locally, disables the swap hand override.</p>
 *
 * <p>Nothing in the buffer.</p>
 */
public final class C2SHasClientModInstalled implements CustomPacketPayload {
    public static final C2SHasClientModInstalled INSTANCE = new C2SHasClientModInstalled();

    public static final Type<C2SHasClientModInstalled> TYPE = new Type<>(EyeSpy.id("has_client_mod_installed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SHasClientModInstalled> CODEC = StreamCodec.unit(INSTANCE);

    private C2SHasClientModInstalled() {
    }

    @Override
    public @NotNull Type<C2SHasClientModInstalled> type() {
        return TYPE;
    }
}
