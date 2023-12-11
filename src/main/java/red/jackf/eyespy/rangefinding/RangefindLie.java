package red.jackf.eyespy.rangefinding;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyColours;
import red.jackf.eyespy.lies.AnchoredText;
import red.jackf.eyespy.mixins.EyeSpyEntityInvoker;
import red.jackf.eyespy.raycasting.Raycasting;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;

public class RangefindLie extends AnchoredText {
    protected RangefindLie(ServerPlayer viewer) {
        super(viewer, 2f, -0.75f);
    }

    @Override
    protected Vec3 getTargetPos() {
        return this.viewer.getEyePosition().add(this.viewer.getLookAngle().scale(RAYCAST_DISTANCE_CURVE_FACTOR));
    }

    @Override
    protected Component getCurrentMessage() {
        HitResult hit = Raycasting.cast(viewer);
        return switch (hit.getType()) {
            case MISS -> Component.translatable("eyespy.rangefinder.outOfRange");
            case BLOCK -> makeBlockText((BlockHitResult) hit);
            case ENTITY -> makeEntityText((EntityHitResult) hit);
        };
    }

    protected void tick(ServerPlayer viewer, EntityLie<Display.TextDisplay> lie) {
        if (this.viewer.isRemoved() || !this.viewer.getUseItem().is(Items.SPYGLASS) || this.viewer.hasDisconnected()) {
            this.stop();
        } else {
            super.tick(viewer, lie);
        }
    }

    private MutableComponent makeDistanceText(HitResult hit) {
        return Component.literal("%.2f".formatted(hit.getLocation().distanceTo(this.viewer.getEyePosition())) + "m");
    }

    private Component makeBlockText(BlockHitResult hit) {
        if (!EyeSpy.CONFIG.instance().rangefinder.showBlockName) return makeDistanceText(hit);

        BlockState state = this.viewer.serverLevel().getBlockState(hit.getBlockPos());
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

    public static void create(ServerPlayer player) {
        new RangefindLie(player);
    }
}
