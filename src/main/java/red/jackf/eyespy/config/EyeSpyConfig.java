package red.jackf.eyespy.config;

import blue.endless.jankson.Comment;
import net.minecraft.util.Mth;
import red.jackf.jackfredlib.api.config.Config;

public class EyeSpyConfig implements Config<EyeSpyConfig> {

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
                Maximum range that the pings will function at. This is capped by the world / server's render distance.
                Range: [16, 384]
                Default: 256""")
        public int maxRangeBlocks = 256;

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

    @Override
    public void validate() {
        this.ping.maxRangeBlocks = Mth.clamp(this.ping.maxRangeBlocks, 16, 384);
        this.ping.notifyRangeBlocks = Mth.clamp(this.ping.notifyRangeBlocks, 8, 256);
        this.ping.lifetimeTicks = Mth.clamp(this.ping.lifetimeTicks, 60, 400);
    }
}
