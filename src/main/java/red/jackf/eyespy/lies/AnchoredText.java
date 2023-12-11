package red.jackf.eyespy.lies;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.raycasting.Raycasting;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;

/**
 * Utils for creating a text display that faces a player for a given position. Positioned closer to the player every tick
 * for transparency problems.
 */
public abstract class AnchoredText {
    // Message is updated every X ticks
    private static final long REFRESH_INTERVAL_TICKS = 4L;
    // used to bring text display a bit further away from the wall
    protected static final int RAYCAST_DISTANCE_CURVE_FACTOR = 2;
    protected final ServerPlayer viewer;
    private final EntityLie<Display.TextDisplay> lie;

    private Vec3 lastOffset;
    private final float maxDistanceFromPlayer;
    private final float yOffset;

    /**
     * @param viewer Player viewing this text
     * @param maxDistanceFromPlayer Distance the text is from the player's eyes
     * @param yOffset Vertical offset from directly on the target
     */
    protected AnchoredText(ServerPlayer viewer, float maxDistanceFromPlayer, float yOffset) {
        this.viewer = viewer;
        this.lie = EntityLie.builder(EntityBuilders.textDisplay(viewer.serverLevel())
                                                   .billboard(Display.BillboardConstraints.FIXED)
                                                   .backgroundColour(0x80, 0x00, 0x00, 0x00)
                                                   .seeThrough(false)
                                                   .textAlign(Display.TextDisplay.Align.CENTER)
                                                   .transformInterpolationDuration(1)
                                                   .brightness(15, 15)
                                                   .position(viewer.getEyePosition())
                                                   .build())
                            .onTick(this::tick)
                            .createAndShow(viewer);
        this.maxDistanceFromPlayer = maxDistanceFromPlayer;
        this.yOffset = yOffset;

        this.lastOffset = viewer.getEyePosition().add(viewer.getLookAngle().scale(maxDistanceFromPlayer));
        this.refreshPosAndScale();
    }

    protected abstract Vec3 getTargetPos();

    protected abstract Component getCurrentMessage();

    protected void stop() {
        this.lie.fade();
    }

    protected void tick(ServerPlayer viewer, EntityLie<Display.TextDisplay> lie) {
        this.refreshPosAndScale();

        if ((this.viewer.serverLevel().getGameTime() % REFRESH_INTERVAL_TICKS) == 0) {
            EntityUtils.setDisplayText(this.lie.entity(), getCurrentMessage());
        }
    }

    private float getScale() {
        return 0.020f * maxDistanceFromPlayer * EyeSpy.CONFIG.instance().rangefinder.textScale;
    }

    private void refreshPosAndScale() {
        float maxDistance = maxDistanceFromPlayer;
        float scale = getScale();

        // dilemma: if we have enable see-through, then it renders behind fluids, but if it's false it doesn't render in
        // terrain. solution: we dont enable it but hopefully bring it in front of any terrain
        float collisionDistance = (float) Raycasting.pick(viewer, getTargetPos(), true)
                                                    .getLocation()
                                                    .distanceTo(this.viewer.getEyePosition());
        float distance = Mth.clamp(collisionDistance / RAYCAST_DISTANCE_CURVE_FACTOR, 0.1f, maxDistance);
        float scaleFactorRelativeToMax = distance / maxDistance;

        Vec3 thisTickOffset = this.viewer.getEyePosition().add(this.viewer.getLookAngle().scale(distance)).subtract(this.lie.entity().position());

        Vector3f translate = thisTickOffset.toVector3f().add(new Vector3f(0, scaleFactorRelativeToMax * scale * yOffset, 0));

        EntityUtils.updateDisplayTransformation(this.lie.entity(),
                                                translate,
                                                null,
                                                new Vector3f(scaleFactorRelativeToMax * scale),
                                                makeRotation()
        );

        boolean changed = thisTickOffset.subtract(this.lastOffset).length() > 0.001;

        if (changed) EntityUtils.startInterpolationIn(this.lie.entity(), 0);
        this.lastOffset = thisTickOffset;
    }

    // not kept up on my quaternion lore so this may not be the most efficient
    private Quaternionf makeRotation() {
        Quaternionf yRot = new Quaternionf(new AxisAngle4f((float) (Math.PI + Math.toRadians(-this.viewer.getYHeadRot())), 0, 1, 0));
        Quaternionf xRot = new Quaternionf(new AxisAngle4f((float) Math.toRadians(-this.viewer.getXRot()), 1, 0, 0));

        return yRot.mul(xRot);
    }
}
