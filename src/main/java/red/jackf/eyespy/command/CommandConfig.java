package red.jackf.eyespy.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import red.jackf.eyespy.EyeSpy;
import red.jackf.eyespy.config.EyeSpyConfig;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

@SuppressWarnings({"SameParameterValue"})
public class CommandConfig {
    private CommandConfig() {}

    private static final String BASE_WIKI_URL = "https://github.com/JackFred2/EyeSpy/wiki/";

    private static String makeWikiLink(String basePage, String optionName) {
        return BASE_WIKI_URL + basePage + "#" + optionName.toLowerCase(Locale.ROOT).replace(".", "");
    }

    private interface WikiPage {
        String GLOBAL = "Home";
        String PING = "Ping";
        String RANGEFINDER = "Rangefinder";
    }

    private static EyeSpyConfig getConfig() {
        return EyeSpy.CONFIG.instance();
    }

    private static void verifySafeAndLoad() {
        getConfig().validate();
        EyeSpy.CONFIG.save();
        EyeSpy.CONFIG.load();
    }

    private static Component makeHover(String name, String fullName, String baseWikiPage) {
        return Formatting.variable(literal(name).withStyle(Style.EMPTY.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                               Component.empty()
                                        .append(Formatting.variable("$." + fullName))
                                        .append(CommonComponents.NEW_LINE)
                                        .append(translatable("eyespy.command.config.clickToOpenWiki")))
        ).withClickEvent(
                new ClickEvent(ClickEvent.Action.OPEN_URL, makeWikiLink(baseWikiPage, fullName))
        )));
    }

    //////////////
    // BUILDERS //
    //////////////
    private static LiteralArgumentBuilder<CommandSourceStack> makeBoolean(String name,
                                                                          String fullName,
                                                                          String baseWikiPage,
                                                                          Function<EyeSpyConfig, Boolean> get,
                                                                          BiConsumer<EyeSpyConfig, Boolean> set) {
        return Commands.literal(name)
                       .executes(ctx -> {
                           ctx.getSource().sendSuccess(Formatting.infoLine(
                                   translatable("eyespy.command.config.check",
                                                makeHover(name, fullName, baseWikiPage),
                                                Formatting.bool(get.apply(getConfig())))
                           ), false);

                           return 1;
                       }).then(Commands.literal("true")
                                       .executes(ctx -> {
                                                     if (get.apply(getConfig())) {
                                                         ctx.getSource().sendFailure(Formatting.infoLine(
                                                                 translatable("eyespy.command.config.unchanged",
                                                                              makeHover(name, fullName, baseWikiPage),
                                                                              Formatting.bool(true))
                                                         ));

                                                         return 0;
                                                     } else {
                                                         set.accept(getConfig(), true);
                                                         verifySafeAndLoad();
                                                         ctx.getSource().sendSuccess(Formatting.infoLine(
                                                                 translatable("eyespy.command.config.change",
                                                                              makeHover(name, fullName, baseWikiPage),
                                                                              Formatting.bool(false),
                                                                              Formatting.bool(true))
                                                         ), true);

                                                         return 1;
                                                     }
                                                 }
                                       )).then(Commands.literal("false")
                                                       .executes(ctx -> {
                                                                     if (!get.apply(getConfig())) {
                                                                         ctx.getSource().sendFailure(Formatting.infoLine(
                                                                                 translatable("eyespy.command.config.unchanged",
                                                                                              makeHover(name, fullName, baseWikiPage),
                                                                                              Formatting.bool(false))
                                                                         ));

                                                                         return 0;
                                                                     } else {
                                                                         set.accept(getConfig(), false);
                                                                         verifySafeAndLoad();
                                                                         ctx.getSource().sendSuccess(Formatting.infoLine(
                                                                                 translatable("eyespy.command.config.change",
                                                                                              makeHover(name, fullName, baseWikiPage),
                                                                                              Formatting.bool(true),
                                                                                              Formatting.bool(false))
                                                                         ), true);

                                                                         return 1;
                                                                     }
                                                                 }
                                                       ));
    }/*

    private static <E extends Enum<E>> LiteralArgumentBuilder<CommandSourceStack> makeEnum(String name,
                                                                                           String fullName,
                                                                                           String baseWikiPage,
                                                                                           Class<E> enumClass,
                                                                                           Function<EyeSpyConfig, E> get,
                                                                                           BiConsumer<EyeSpyConfig, E> set) {
        var node = Commands.literal(name)
                           .executes(ctx -> {
                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                       translatable("eyespy.command.config.check",
                                                    makeHover(name, fullName, baseWikiPage),
                                                    Formatting.string(get.apply(getConfig()).name()))
                               ), false);

                               return 1;
                           });

        for (E constant : enumClass.getEnumConstants()) {
            node.then(Commands.literal(constant.name())
                              .executes(ctx -> {
                                  var old = get.apply(getConfig());
                                  if (old == constant) {
                                      ctx.getSource().sendFailure(Formatting.infoLine(
                                              translatable("eyespy.command.config.unchanged",
                                                           makeHover(name, fullName, baseWikiPage),
                                                           Formatting.string(constant.name()))
                                      ));

                                      return 0;
                                  } else {
                                      set.accept(getConfig(), constant);
                                      verifySafeAndLoad();
                                      ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                              translatable("eyespy.command.config.change",
                                                           makeHover(name, fullName, baseWikiPage),
                                                           Formatting.string(old.name()),
                                                           Formatting.string(constant.name()))
                                      ), true);

                                      return 1;
                                  }
                              })
            );
        }

        return node;
    }*/

    private static LiteralArgumentBuilder<CommandSourceStack> makeIntRange(String name,
                                                                           String fullName,
                                                                           String baseWikiPage,
                                                                           int min,
                                                                           int max,
                                                                           Function<EyeSpyConfig, Integer> get,
                                                                           BiConsumer<EyeSpyConfig, Integer> set) {
        return Commands.literal(name)
                       .executes(ctx -> {
                           ctx.getSource().sendSuccess(Formatting.infoLine(
                                   translatable("eyespy.command.config.check",
                                                makeHover(name, fullName, baseWikiPage),
                                                Formatting.integer(get.apply(getConfig())))
                           ), false);

                           return 1;
                       }).then(Commands.argument(name, IntegerArgumentType.integer(min, max))
                                       .executes(ctx -> {
                                           var old = get.apply(getConfig());
                                           var newValue = IntegerArgumentType.getInteger(ctx, name);
                                           if (old == newValue) {
                                               ctx.getSource().sendFailure(Formatting.infoLine(
                                                       translatable("eyespy.command.config.unchanged",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.integer(old))
                                               ));

                                               return 0;
                                           } else {
                                               set.accept(getConfig(), newValue);
                                               verifySafeAndLoad();
                                               ctx.getSource().sendSuccess(Formatting.infoLine(
                                                       translatable("eyespy.command.config.change",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.integer(old),
                                                                    Formatting.integer(newValue))
                                               ), true);

                                               return 1;
                                           }
                                       })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeFloatRange(String name,
                                                                             String fullName,
                                                                             String baseWikiPage,
                                                                             float min,
                                                                             float max,
                                                                             Function<EyeSpyConfig, Float> get,
                                                                             BiConsumer<EyeSpyConfig, Float> set) {
        return Commands.literal(name)
                       .executes(ctx -> {
                           ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                   translatable("eyespy.command.config.check",
                                                makeHover(name, fullName, baseWikiPage),
                                                Formatting.floating(get.apply(getConfig())))
                           ), false);

                           return 1;
                       }).then(Commands.argument(name, FloatArgumentType.floatArg(min, max))
                                       .executes(ctx -> {
                                           var old = get.apply(getConfig());
                                           var newValue = FloatArgumentType.getFloat(ctx, name);
                                           if (old == newValue) {
                                               ctx.getSource().sendFailure(Formatting.infoLine(
                                                       translatable("eyespy.command.config.unchanged",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.floating(old))
                                               ));

                                               return 0;
                                           } else {
                                               set.accept(getConfig(), newValue);
                                               verifySafeAndLoad();
                                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                                       translatable("eyespy.command.config.change",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.floating(old),
                                                                    Formatting.floating(newValue))
                                               ), true);

                                               return 1;
                                           }
                                       })
                );
    }

/*
    private static LiteralArgumentBuilder<CommandSourceStack> makeWord(String name,
                                                                       String fullName,
                                                                       String baseWikiPage,
                                                                       Function<EyeSpyConfig, String> get,
                                                                       BiConsumer<EyeSpyConfig, String> set) {
        return Commands.literal(name)
                       .executes(ctx -> {
                           ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                   translatable("eyespy.command.config.check",
                                                makeHover(name, fullName, baseWikiPage),
                                                Formatting.string(get.apply(getConfig())))
                           ), false);

                           return 1;
                       }).then(Commands.argument(name, StringArgumentType.word())
                                       .executes(ctx -> {
                                           var old = get.apply(getConfig());
                                           var newValue = StringArgumentType.getString(ctx, name);
                                           if (old.equals(newValue)) {
                                               ctx.getSource().sendFailure(Formatting.infoLine(
                                                       translatable("eyespy.command.config.unchanged",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.string(old))
                                               ));

                                               return 0;
                                           } else {
                                               set.accept(getConfig(), newValue);
                                               verifySafeAndLoad();

                                               ctx.getSource().sendSuccess(() -> Formatting.infoLine(
                                                       translatable("eyespy.command.config.change",
                                                                    makeHover(name, fullName, baseWikiPage),
                                                                    Formatting.string(old),
                                                                    Formatting.string(newValue))
                                               ), true);

                                               return 1;
                                           }
                                       })
                );
    }*/

    ///////////
    // NODES //
    ///////////

    public static LiteralArgumentBuilder<CommandSourceStack> createCommandNode(CommandBuildContext context) {
        var root = Commands.literal("config")
                           .requires(ctx -> ctx.hasPermission(4));

        root.then(makeIntRange("maxRangeBlocks",
                               "maxRangeBlocks",
                               WikiPage.GLOBAL,
                               16,
                               384,
                               config -> config.maxRangeBlocks,
                               (config, newVal) -> config.maxRangeBlocks = newVal));

        root.then(makePingNode());
        root.then(makeRangefinderNode());

        return root;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> makePingNode() {
        var root = Commands.literal("ping");

        root.then(makeBoolean("enabled",
                              "ping.enabled",
                              WikiPage.PING,
                              config -> config.ping.enabled,
                              (config, newVal) -> config.ping.enabled = newVal));

        root.then(makeBoolean("requiresZoomIn",
                              "ping.requiresZoomIn",
                              WikiPage.PING,
                              config -> config.ping.requiresZoomIn,
                              (config, newVal) -> config.ping.requiresZoomIn = newVal));

        root.then(makeIntRange("notifyRangeBlocks",
                               "ping.notifyRangeBlocks",
                               WikiPage.PING,
                               8,
                               256,
                               config -> config.ping.notifyRangeBlocks,
                               (config, newVal) -> config.ping.notifyRangeBlocks = newVal));

        root.then(makeIntRange("lifetimeTicks",
                               "ping.lifetimeTicks",
                               WikiPage.PING,
                               60,
                               400,
                               config -> config.ping.lifetimeTicks,
                               (config, newVal) -> config.ping.lifetimeTicks = newVal));

        root.then(makeIntRange("maxPings",
                               "ping.maxPings",
                               WikiPage.PING,
                               1,
                               32,
                               config -> config.ping.maxPings,
                               (config, newVal) -> config.ping.maxPings = newVal));

        return root;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> makeRangefinderNode() {
        var root = Commands.literal("rangefinder");

        root.then(makeBoolean("enabled",
                              "rangefinder.enabled",
                              WikiPage.RANGEFINDER,
                              config -> config.rangefinder.enabled,
                              (config, newVal) -> config.rangefinder.enabled = newVal));

        root.then(makeBoolean("useColours",
                              "rangefinder.useColours",
                              WikiPage.RANGEFINDER,
                              config -> config.rangefinder.useColours,
                              (config, newVal) -> config.rangefinder.useColours = newVal));

        root.then(makeBoolean("showBlockName",
                              "rangefinder.showBlockName",
                              WikiPage.RANGEFINDER,
                              config -> config.rangefinder.showBlockName,
                              (config, newVal) -> config.rangefinder.showBlockName = newVal));

        root.then(makeBoolean("showEntityName",
                              "rangefinder.showEntityName",
                              WikiPage.RANGEFINDER,
                              config -> config.rangefinder.showEntityName,
                              (config, newVal) -> config.rangefinder.showEntityName = newVal));

        root.then(makeFloatRange("textScale",
                                 "rangefinder.textScale",
                                 WikiPage.RANGEFINDER,
                                 0.25f, 2f,
                                 config -> config.rangefinder.textScale,
                                 (config, newVal) -> config.rangefinder.textScale = newVal));

        return root;
    }
}