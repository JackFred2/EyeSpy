package red.jackf.eyespy.rangefinding;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyColours;
import red.jackf.eyespy.mixins.EyeSpyEntityInvoker;
import red.jackf.eyespy.raycasting.Raycasting;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;

public class RangefindLie {
    private static final float MAX_DISTANCE = 3f;
    private static final float SCALE = 0.020f * MAX_DISTANCE;
    private static final float OFFSET = 0.75f;

    private static final long REFRESH_INTERVAL_TICKS = 4L;

    private final ServerPlayer player;
    private final EntityLie<Display.TextDisplay> lie;
    private Vec3 lastTickOffset;

    private RangefindLie(ServerPlayer player) {
        this.player = player;
        this.lie = EntityLie.builder(EntityBuilders.textDisplay(player.serverLevel())
                                                   .billboard(Display.BillboardConstraints.FIXED)
                                                   .backgroundColour(0x80, 0x00, 0x00, 0x00)
                                                   .seeThrough(false)
                                                   .textAlign(Display.TextDisplay.Align.CENTER)
                                                   .transformInterpolationDuration(1)
                                                   .brightness(15, 15)
                                                   .position(player.getEyePosition())
                                                   .build())
                            .onTick(this::tickLie)
                            .createAndShow(player);
        this.lastTickOffset = player.getEyePosition().add(player.getLookAngle().scale(MAX_DISTANCE));
        this.refreshPosAngleAndScale();
    }

    private void tickLie(ServerPlayer player, EntityLie<Display.TextDisplay> lie) {
        if (this.player.isRemoved() || !this.player.getUseItem().is(Items.SPYGLASS) || this.player.hasDisconnected()) {
            this.stop();
        } else {
            this.refreshPosAngleAndScale();

            ServerLevel level = this.player.serverLevel();

            if ((level.getGameTime() & REFRESH_INTERVAL_TICKS) == 0) {
                HitResult hit = Raycasting.cast(player);
                Component text = switch (hit.getType()) {
                    case MISS -> Component.translatable("eyespy.rangefinder.outOfRange");
                    case BLOCK -> makeBlockText((BlockHitResult) hit);
                    case ENTITY -> makeEntityText((EntityHitResult) hit);
                };

                EntityUtils.setDisplayText(this.lie.entity(), text);
            }
        }
    }

    private MutableComponent makeDistanceText(HitResult hit) {
        return Component.literal("%.2f".formatted(hit.getLocation().distanceTo(this.player.getEyePosition())) + "m");
    }

    private Component makeBlockText(BlockHitResult hit) {
        if (!EyeSpy.CONFIG.instance().rangefinder.showBlockName) return makeDistanceText(hit);

        BlockState state = this.player.serverLevel().getBlockState(hit.getBlockPos());
        Style style = EyeSpy.CONFIG.instance().rangefinder.useColours ?
                Style.EMPTY.withColor(EyeSpyColours.getForBlock(state).toARGB()) : Style.EMPTY;

        return makeDistanceText(hit)
                .append(CommonComponents.NEW_LINE)
                .append(state.getBlock().getName().setStyle(style));
    }

    private Component makeEntityText(EntityHitResult hit) {
        if (!EyeSpy.CONFIG.instance().rangefinder.showEntityName) return makeDistanceText(hit);

        Style style = EyeSpy.CONFIG.instance().rangefinder.useColours ?
                Style.EMPTY.withColor(EyeSpyColours.getForEntity(hit.getEntity())) : Style.EMPTY;
        Component name = ((EyeSpyEntityInvoker) hit.getEntity()).eyespy$getTypeName().copy().setStyle(style);

        if (hit.getEntity().hasCustomName()) {
            name = Component.literal("").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                            .append(hit.getEntity().getCustomName())
                            .append(" (")
                            .append(name)
                            .append(")");
        }
        return makeDistanceText(hit)
                .append(CommonComponents.NEW_LINE)
                .append(name);
    }

    private void stop() {
        this.lie.fade();
    }

    private void refreshPosAngleAndScale() {
        // dilemma: if we have enable see-through, then it renders behind fluids, but if it's false it doesn't render in
        // terrain. solution: we dont enable it but hopefully bring it in front of any terrain
        float collisionDistance = (float) Raycasting.pick(player, MAX_DISTANCE * 2, true)
                                                    .getLocation()
                                                    .distanceTo(this.player.getEyePosition());
        float distance = Mth.clamp(collisionDistance / 2, 0.1f, MAX_DISTANCE);
        float scaleFactorRelativeToMax = distance / MAX_DISTANCE;

        Vec3 thisTickOffset = this.player.getEyePosition().add(this.player.getLookAngle().scale(distance)).subtract(this.lie.entity().position());

        Vector3f translate = thisTickOffset.toVector3f().add(new Vector3f(0, scaleFactorRelativeToMax * -SCALE * OFFSET, 0));

        EntityUtils.updateDisplayTransformation(this.lie.entity(),
                                                translate,
                                                null,
                                                new Vector3f(scaleFactorRelativeToMax * SCALE),
                                                makeRotation()
        );

        boolean changed = thisTickOffset.subtract(this.lastTickOffset).length() > 0.001;

        if (changed) EntityUtils.startInterpolationIn(this.lie.entity(), 0);
        this.lastTickOffset = thisTickOffset;
    }

    // not kept up on my quaternion lore so this may not be the most efficient
    private Quaternionf makeRotation() {
        Quaternionf yRot = new Quaternionf(new AxisAngle4f((float) (Math.PI + Math.toRadians(-this.player.getYHeadRot())), 0, 1, 0));
        Quaternionf xRot = new Quaternionf(new AxisAngle4f((float) Math.toRadians(-this.player.getXRot()), 1, 0, 0));

        return yRot.mul(xRot);
    }

    public static void create(ServerPlayer player) {
        new RangefindLie(player);
    }
}
