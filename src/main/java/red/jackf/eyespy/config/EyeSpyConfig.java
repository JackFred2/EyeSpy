package red.jackf.eyespy.config;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.networking.packets.S2CSettings;
import red.jackf.jackfredlib.api.config.Config;

import java.util.function.Predicate;

public class EyeSpyConfig implements Config<EyeSpyConfig> {
    @Comment("""
            Maximum range that pings and rangefinding will function at. This is capped by the world / server's render
            distance.
            Range: [16, 512]
            Default: 256""")
    public int maxRangeBlocks = 256;

    @Comment("""
            Whether raycasting for rangefinding and pings should load chunks if the target is unloaded. This may cause
            additional lag.
            Options: true, false
            Default: false""")
    public boolean loadChunks = false;

    @Comment("""
            Settings related to the pinging function added to spyglasses - to use, press the swap hands key,
            default 'F'.""")
    public Ping ping = new Ping();

    public static class Ping {
        @Comment("""
                Whether to enable the ping functionality for spyglasses.
                Options: true, false
                Default: true""")
        public boolean enabled = true;

        @Comment("""
                What requirements there are to pinging with the spyglass. For more information, see the wikipage
                Options: zoomed_with_spyglass, holding_spyglass, none
                Default: zoomed_with_spyglass""")
        public PingRequirement pingRequirement = PingRequirement.zoomed_with_spyglass;

        @Comment("""
                Maximum range of nearby players that will recieve the ping highlight and sound.
                Range: [8, 256]
                Default: 64""")
        public int notifyRangeBlocks = 64;

        @Comment("""
                Lifetime of ping highlights, in ticks (1 tick = 50ms).
                Range: [60, 400] [3 seconds, 20 seconds]
                Default: 160 (8 seconds)""")
        public int lifetimeTicks = 160;

        @Comment("""
                Maximum number of pings allowed per-player at a single point in time.
                Range: [1, 32]
                Default: 5""")
        public int maxPings = 5;

        @Comment("""
                Whether to show description text (block and entity names) for pings.
                Options: true, false
                Default: false""")
        public boolean showDescriptionText = false;

        @Comment("""
                Whether to show distance text for pings.
                Options: true, false
                Default: true""")
        public boolean showDistanceText = true;

        @Comment("""
                Whether to show the name of the player who caused the ping, on the text for pings.
                Options: true, false
                Default: true""")
        public boolean showPingerName = true;

        @Comment("""
                How small the ping text should scale down to when the player is looking away, as a factor of the full size.
                See also: $.text.textScale.
                Options: [0, 1]
                Default: 0.4""")
        public float minimumScale = 0.4f;

        public enum PingRequirement {
            zoomed_with_spyglass(player -> player.getUseItem().is(Items.SPYGLASS)),
            holding_spyglass(player -> player.getMainHandItem().is(Items.SPYGLASS) || player.getOffhandItem().is(Items.SPYGLASS)),
            none(player -> true);

            private final Predicate<ServerPlayer> pingPredicate;

            PingRequirement(Predicate<ServerPlayer> pingPredicate) {
                this.pingPredicate = pingPredicate;
            }

            public boolean test(ServerPlayer player) {
                return pingPredicate.test(player);
            }
        }
    }

    @Comment("""
            Settings related to the rangefinding feature added to spyglasses - when looking at a block, or entity, the
            distance will be displayed, as well as their names.""")
    public Rangefinder rangefinder = new Rangefinder();

    public static class Rangefinder {
        @Comment("""
                Whether to enable rangefinding functionality for spyglasses.
                Options: true, false
                Default: true""")
        public boolean enabled = true;

        @Comment("""
                Whether to show the name of the block being looked at, if applicable.
                Options: true, false
                Default: true""")
        public boolean showBlockName = true;

        @Comment("""
                Whether to show the name of the entity being looked at, if applicable.
                Options: true, false
                Default: true""")
        public boolean showEntityName = true;
    }

    @Comment("""
            Settings related to the text displayed by Eye Spy. This includes texts shown by pings when enabled, as well
            as the rangefinder.""")
    public Text text = new Text();

    public static class Text {
        @Comment("""
                Whether to use highlight colours for labels when looking at entities or blocks.
                Options: true, false
                Default: true""")
        public boolean useColours = true;

        @Comment("""
                Modifier for the size of the rangefinder text.
                Options: [0.25, 2]
                Default: 1""")
        public float textScale = 1;
    }

    @Override
    public void validate() {
        this.maxRangeBlocks = Mth.clamp(this.maxRangeBlocks, 16, 512);
        this.ping.notifyRangeBlocks = Mth.clamp(this.ping.notifyRangeBlocks, 8, 256);
        this.ping.lifetimeTicks = Mth.clamp(this.ping.lifetimeTicks, 60, 400);
        this.ping.maxPings = Mth.clamp(this.ping.maxPings, 1, 32);
        this.ping.minimumScale = Mth.clamp(this.ping.minimumScale, 0, 1);
        this.text.textScale = Mth.clamp(this.text.textScale, 0.25f, 2f);
    }

    @Override
    public void onLoad(@Nullable EyeSpyConfig old) {
        var server = EyeSpy.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> {
                if (ServerPlayNetworking.canSend(player, S2CSettings.TYPE)) {
                    ServerPlayNetworking.send(player, S2CSettings.create());
                }
            });
        }
    }
}
