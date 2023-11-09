package red.jackf.eyespy.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EyeSpyEntityInvoker {

    @Invoker("getTypeName")
    Component eyespy$getTypeName();
}
