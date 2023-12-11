package red.jackf.eyespy.ping.lies;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import red.jackf.eyespy.EyeSpyTexts;
import red.jackf.eyespy.lies.AnchoredText;

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
             () -> EyeSpyTexts.entity(viewer, entity.position(), entity));
    }

    protected PingLieText(ServerPlayer viewer, BlockPos pos, BlockState state) {
        this(viewer,
             () -> pos.getCenter().add(0, 1f, 0),
             () -> EyeSpyTexts.block(viewer, pos.getCenter(), state));
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
