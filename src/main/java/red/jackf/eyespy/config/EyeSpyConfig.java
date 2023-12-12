package red.jackf.eyespy.config;

import blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.networking.packets.S2CSettings;
import red.jackf.jackfredlib.api.config.Config;

public class EyeSpyConfig implements Config<EyeSpyConfig> {
    @Comment("""
                Maximum range that pings and rangefinding will function at. This is capped by the world / server's render
                distance.
                Range: [16, 384]
                Default: 256""")
    public int maxRangeBlocks = 256;

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
                Whether pinging functionality requires the player to be zoomed in (using the spyglass), or if just having
                it in their hand will suffice.
                Options: true, false
                Default: true""")
        public boolean requiresZoomIn = true;

        @Comment("""
                Maximum range of nearby players that will recieve the ping highlight and sound.
                Range: [8, 256]
                Default: 64""")
        public int notifyRangeBlocks = 64;

        @Comment("""
                Lifetime of ping highlights, in ticks.
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
                Default: true""")
        public boolean showDescriptionText = true;

        @Comment("""
                Whether to show distance text for pings.
                Options: true, false
                Default: true""")
        public boolean showDistanceText = true;
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
        this.maxRangeBlocks = Mth.clamp(this.maxRangeBlocks, 16, 384);
        this.ping.notifyRangeBlocks = Mth.clamp(this.ping.notifyRangeBlocks, 8, 256);
        this.ping.lifetimeTicks = Mth.clamp(this.ping.lifetimeTicks, 60, 400);
        this.ping.maxPings = Mth.clamp(this.ping.maxPings, 1, 32);
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
