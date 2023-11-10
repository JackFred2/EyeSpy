package red.jackf.eyespy.raycasting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * Custom clip context used to have custom ray functions outside of the Block enum
 */
public class CustomClipContext extends ClipContext {

    public CustomClipContext(
            Vec3 from,
            Vec3 to,
            Entity entity,
            boolean hitFluids) {
        super(from, to, Block.OUTLINE, hitFluids ? Fluid.ANY : Fluid.NONE, entity);
    }

    @Override
    public @NotNull VoxelShape getBlockShape(BlockState blockState, BlockGetter level, BlockPos pos) {
        if (blockState.getBlock() instanceof BushBlock || blockState.getBlock() instanceof GrowingPlantBlock) {
            return Shapes.empty();
        } else {
            return super.getBlockShape(blockState, level, pos);
        }
    }
}
