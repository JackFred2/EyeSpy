package red.jackf.eyespy.rangefinding;

import net.minecraft.network.chat.CommonComponents;
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
        return this.viewer.getEyePosition().add(this.viewer.getLookAngle().scale(2f));
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
        MutableComponent text = EyeSpyTexts.distance(this.viewer, hit.getLocation());

        if (EyeSpy.CONFIG.instance().rangefinder.showBlockName)
            text = text.append(CommonComponents.NEW_LINE)
                       .append(EyeSpyTexts.block(this.viewer.getLevel().getBlockState(hit.getBlockPos())));

        return text;
    }

    private Component makeEntityText(EntityHitResult hit) {
        MutableComponent text = EyeSpyTexts.distance(this.viewer, hit.getLocation());

        if (EyeSpy.CONFIG.instance().rangefinder.showEntityName)
            text = text.append(CommonComponents.NEW_LINE)
                       .append(EyeSpyTexts.entity(hit.getEntity()));

        return text;
    }

    public static void create(ServerPlayer player) {
        new RangefindLie(player);
    }
}
