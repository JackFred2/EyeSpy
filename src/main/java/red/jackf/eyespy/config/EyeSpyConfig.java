package red.jackf.eyespy.config;

import blue.endless.jankson.Comment;
import net.minecraft.util.Mth;
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
    }

    public Rangefinder rangefinder = new Rangefinder();

    public static class Rangefinder {
        @Comment("""
                Whether to enable rangefinding functionality for spyglasses.
                Options: true, false
                Default: true""")
        public boolean enabled = true;

        @Comment("""
                Whether to use highlight colours for labels when looking at entities or blocks.
                Options: true, false
                Default: true""")
        public boolean useColours = true;

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

    @Override
    public void validate() {
        this.maxRangeBlocks = Mth.clamp(this.maxRangeBlocks, 16, 384);
        this.ping.notifyRangeBlocks = Mth.clamp(this.ping.notifyRangeBlocks, 8, 256);
        this.ping.lifetimeTicks = Mth.clamp(this.ping.lifetimeTicks, 60, 400);
    }
}
