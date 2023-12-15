package red.jackf.eyespy.networking.packets;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.config.EyeSpyConfig;

import java.util.Objects;

/**
 * <p>Packet for syncing mod settings</p>
 */
public final class S2CSettings implements FabricPacket {
    public static final PacketType<S2CSettings> TYPE = PacketType.create(EyeSpy.id("server_settings"), S2CSettings::new);
    public final boolean pingEnabled;
    public final EyeSpyConfig.Ping.PingRequirement pingRequirement;

    public S2CSettings(boolean pingEnabled, EyeSpyConfig.Ping.PingRequirement pingRequirement) {
        this.pingEnabled = pingEnabled;
        this.pingRequirement = pingRequirement;
    }

    public S2CSettings(FriendlyByteBuf buf) {
        var tag = Objects.requireNonNull(buf.readNbt());

        this.pingEnabled = tag.getBoolean("pingEnabled");
        this.pingRequirement = EyeSpyConfig.Ping.PingRequirement.valueOf(tag.getString("pingRequirement"));
    }


    public static S2CSettings create() {
        var config = EyeSpy.CONFIG.instance();
        return new S2CSettings(config.ping.enabled, config.ping.pingRequirement);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        var tag = new CompoundTag();
        tag.putBoolean("pingEnabled", pingEnabled);
        tag.putString("pingRequirement", pingRequirement.name());

        buf.writeNbt(tag);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
