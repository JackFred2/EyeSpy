package red.jackf.eyespy.rangefinding;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyColours;
import red.jackf.eyespy.mixins.EyeSpyEntityInvoker;
import red.jackf.eyespy.raycasting.Raycasting;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.EntityUtils;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;

public class RangefindLie {
    private static final float ENTITY_DISTANCE = 8f;
    private static final float ENTITY_DISTANCE_SQR = ENTITY_DISTANCE * ENTITY_DISTANCE;
    private static final float SCALE = 0.12f;
    private static final float OFFSET = 0.75f;

    private static final long REFRESH_INTERVAL_TICKS = 4L;

    private final ServerPlayer player;
    private final EntityLie<Display.TextDisplay> lie;

    private RangefindLie(ServerPlayer player) {
        this.player = player;
        this.lie = EntityLie.builder(EntityBuilders.textDisplay(player.serverLevel())
                                                   .billboard(Display.BillboardConstraints.FIXED)
                                                   .backgroundColour(0x80, 0x00, 0x00, 0x00)
                                                   .seeThrough(true)
                                                   .textAlign(Display.TextDisplay.Align.CENTER)
                                                   .brightness(15, 15)
                                                   .teleportInterpolationDuration(1)
                                                   .scale(new Vector3f(SCALE))
                                                   .addTranslation(new Vector3f(0, -SCALE * OFFSET, 0))
                                                   .position(getRelativePos())
                                                   .facing(player)
                                                   .build())
                            .onTick(this::tickLie)
                            .createAndShow(player);
    }

    private void tickLie(ServerPlayer player, EntityLie<Display.TextDisplay> lie) {
        if (this.player.isRemoved() || !this.player.getUseItem().is(Items.SPYGLASS) || this.player.hasDisconnected()) {
            this.stop();
        } else {
            this.lie.entity().setPos(getRelativePos());
            EntityUtils.face(this.lie.entity(), this.player);

            ServerLevel level = this.player.serverLevel();

            if ((level.getGameTime() & REFRESH_INTERVAL_TICKS) == 0) {
                HitResult hit = Raycasting.cast(player);
                Component text = switch (hit.getType()) {
                    case MISS -> Component.literal("∞");
                    case BLOCK -> makeBlockText((BlockHitResult) hit);
                    case ENTITY -> makeEntityText((EntityHitResult) hit);
                };

                EntityUtils.setDisplayTextSeeThrough(
                        this.lie.entity(),
                        hit.getLocation().distanceToSqr(this.player.getEyePosition()) < ENTITY_DISTANCE_SQR
                );

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

    private Vec3 getRelativePos() {
        return this.player.getEyePosition().add(this.player.getLookAngle().scale(ENTITY_DISTANCE));
    }

    public static void create(ServerPlayer player) {
        new RangefindLie(player);
    }
}
