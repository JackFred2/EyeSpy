package red.jackf.eyespy.ping.lies;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyTexts;
import red.jackf.eyespy.lies.AnchoredText;

import java.util.Objects;
import java.util.function.Supplier;

public class PingLieText extends AnchoredText {
    private final Supplier<Vec3> posSupplier;
    private final Supplier<Component> textSuppler;

    private PingLieText(ServerPlayer viewer, Supplier<Vec3> posSupplier, Supplier<Component> textSuppler) {
        super(viewer, 4f, 0);
        this.posSupplier = posSupplier;
        this.textSuppler = textSuppler;
    }

    protected PingLieText(ServerPlayer viewer, Entity entity) {
        this(viewer,
             () -> entity.position().add(0, entity.getBbHeight() + 0.5f, 0),
             () -> getEntityText(viewer, entity));
    }

    private static Component getEntityText(ServerPlayer viewer, Entity entity) {
        MutableComponent text = null;
        if (EyeSpy.CONFIG.instance().ping.showDistanceText)
            text = EyeSpyTexts.distance(viewer, entity.position());
        if (EyeSpy.CONFIG.instance().ping.showDescriptionText) {
            var description = EyeSpyTexts.entity(entity);
            if (text != null) {
                return text.append(CommonComponents.NEW_LINE).append(description);
            } else {
                return description;
            }
        }

        return Objects.requireNonNull(text);
    }

    protected PingLieText(ServerPlayer viewer, BlockPos pos, BlockState state) {
        this(viewer,
             () -> pos.getCenter().add(0, 1f, 0),
             () -> getBlockText(viewer, pos, state));
    }

    private static Component getBlockText(ServerPlayer viewer, BlockPos pos, BlockState state) {
        MutableComponent text = null;
        if (EyeSpy.CONFIG.instance().ping.showDistanceText)
            text = EyeSpyTexts.distance(viewer, pos.getCenter());
        if (EyeSpy.CONFIG.instance().ping.showDescriptionText) {
            var description = EyeSpyTexts.block(state);
            if (text != null) {
                return text.append(CommonComponents.NEW_LINE).append(description);
            } else {
                return description;
            }
        }

        return Objects.requireNonNull(text);
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
