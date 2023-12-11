package red.jackf.eyespy.rangefinding;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.EyeSpyTexts;
import red.jackf.eyespy.lies.AnchoredText;
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

    private Component makeBlockText(BlockHitResult hit) {
        if (!EyeSpy.CONFIG.instance().rangefinder.showBlockName)
            return EyeSpyTexts.distance(this.viewer, hit.getLocation());
        else
            return EyeSpyTexts.block(this.viewer, hit.getLocation(), this.viewer.serverLevel().getBlockState(hit.getBlockPos()));
    }

    private Component makeEntityText(EntityHitResult hit) {
        if (!EyeSpy.CONFIG.instance().rangefinder.showEntityName)
            return EyeSpyTexts.distance(this.viewer, hit.getLocation());
        else
            return EyeSpyTexts.entity(this.viewer, hit.getLocation(), hit.getEntity());
    }

    public static void create(ServerPlayer player) {
        new RangefindLie(player);
    }
}
