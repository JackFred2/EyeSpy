package red.jackf.eyespy.ping.lies;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyTexts;
import red.jackf.eyespy.lies.AnchoredText;

import java.util.function.Supplier;

public class PingLieText extends AnchoredText {
    private static final double MAX_CROSSHAIR_ANGLE = Math.PI / 12;
    private static final double CROSSHAIR_SCALE_ANGLE = Math.PI / 16;
    private static final long FULLSIZE_PERIOD_TICKS = 30;
    private static final long FULLSIZE_SCALE_PERIOD = 10;
    private final Supplier<Vec3> posSupplier;
    private final Supplier<Component> textSuppler;
    private final long worldStartTime;

    private PingLieText(ServerPlayer viewer, Supplier<Vec3> posSupplier, Supplier<Component> textSuppler) {
        super(viewer, 4f, 0);
        this.posSupplier = posSupplier;
        this.textSuppler = textSuppler;
        this.worldStartTime = viewer.level().getGameTime();
    }

    protected PingLieText(ServerPlayer viewer, Entity entity) {
        this(viewer,
             () -> entity.position().add(0, entity.getBbHeight() + 0.5f, 0),
             () -> getEntityText(viewer, entity));
    }

    private static Component getEntityText(ServerPlayer viewer, Entity entity) {
        Vec3 target = entity.position();

        boolean distance = EyeSpy.CONFIG.instance().ping.showDistanceText;
        boolean description = EyeSpy.CONFIG.instance().ping.showDescriptionText;

        if (distance) {
            if (description) {
                return EyeSpyTexts.distance(viewer, target).append(CommonComponents.NEW_LINE).append(EyeSpyTexts.entity(entity));
            } else {
                return EyeSpyTexts.distance(viewer, target);
            }
        } else {
            if (description) {
                return EyeSpyTexts.entity(entity);
            } else { // hopefully never happens but never know
                return Component.empty();
            }
        }
    }

    protected PingLieText(ServerPlayer viewer, BlockPos pos, BlockState state) {
        this(viewer,
             () -> pos.getCenter().add(0, 1f, 0),
             () -> getBlockText(viewer, pos, state));
    }

    private static Component getBlockText(ServerPlayer viewer, BlockPos pos, BlockState state) {
        Vec3 target = pos.getCenter();

        boolean distance = EyeSpy.CONFIG.instance().ping.showDistanceText;
        boolean description = EyeSpy.CONFIG.instance().ping.showDescriptionText;

        if (distance) {
            if (description) {
                return EyeSpyTexts.distance(viewer, target).append(CommonComponents.NEW_LINE).append(EyeSpyTexts.block(state));
            } else {
                return EyeSpyTexts.distance(viewer, target);
            }
        } else {
            if (description) {
                return EyeSpyTexts.block(state);
            } else { // hopefully never happens but never know
                return Component.empty();
            }
        }
    }

    protected float getScaleMultiplier() {
        Vec3 a = viewer.getLookAngle();
        Vec3 b = getTargetPos().subtract(viewer.getEyePosition()).normalize();
        double angle = Math.acos(a.dot(b));
        final float minScale = EyeSpy.CONFIG.instance().ping.minimumScale;

        float cursorFactor = (float) (Mth.clamp(angle - CROSSHAIR_SCALE_ANGLE, 0, MAX_CROSSHAIR_ANGLE) / MAX_CROSSHAIR_ANGLE);
        float cursorScale = Mth.lerp(cursorFactor, 1f, minScale);

        long timeSinceStart = viewer.level().getGameTime() - worldStartTime;
        float timeFactor = Mth.clamp(((float) timeSinceStart - FULLSIZE_PERIOD_TICKS) / FULLSIZE_SCALE_PERIOD, 0f, 1f);
        float timeScale = Mth.lerp(timeFactor, 1f, minScale);

        return Math.max(cursorScale, timeScale);
    }

    @Override
    protected Vec3 getTargetPos() {
        return posSupplier != null ? posSupplier.get() : viewer.getEyePosition();
    }

    @Override
    protected Component getCurrentMessage() {
        return textSuppler.get();
    }
}
