package red.jackf.eyespy.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class EyeSpyCommand
{
    public EyeSpyCommand(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext,
            Commands.CommandSelection ignored) {
        var root = Commands.literal("eyespy");

        root.requires(ctx -> ctx.hasPermission(4));

        root.then(CommandConfig.createCommandNode(buildContext));

        dispatcher.register(root);
    }
}
