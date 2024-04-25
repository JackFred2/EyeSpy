package red.jackf.eyespy.networking.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.config.EyeSpyConfig;

/**
 * <p>Packet for syncing mod settings</p>
 */
public record S2CSettings(boolean pingEnabled, EyeSpyConfig.Ping.PingRequirement pingRequirement) implements CustomPacketPayload {
    public static final Type<S2CSettings> TYPE = new Type<>(EyeSpy.id("server_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSettings> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            settings -> settings.pingEnabled,
            ByteBufCodecs.idMapper(index -> EyeSpyConfig.Ping.PingRequirement.values()[index], EyeSpyConfig.Ping.PingRequirement::ordinal),
            settings -> settings.pingRequirement,
            S2CSettings::new
    );


    public static S2CSettings create() {
        var config = EyeSpy.CONFIG.instance();
        return new S2CSettings(config.ping.enabled, config.ping.pingRequirement);
    }

    @Override
    public @NotNull Type<S2CSettings> type() {
        return TYPE;
    }
}
