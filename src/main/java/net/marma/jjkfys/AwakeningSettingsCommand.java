package net.marma.jjkfys;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AwakeningSettingsCommand {

    @SubscribeEvent
    public static void registerCommand(
            RegisterCommandsEvent event
    ) {

        event.getDispatcher()
                .register(
                        Commands.literal(
                                        "jjkfys"
                                )
                                .requires(source -> source.hasPermission(2))
                                .then(
                                        Commands.literal(
                                                        "awakening"
                                                )
                                                .then(
                                                        Commands.literal(
                                                                        "current"
                                                                )
                                                                .then(
                                                                        Commands.argument(
                                                                                        "target",
                                                                                        EntityArgument.player()
                                                                                )
                                                                                .executes(context -> showCurrent(
                                                                                        context.getSource()
                                                                                                .getServer(),
                                                                                        EntityArgument.getPlayer(
                                                                                                context,
                                                                                                "target"
                                                                                        )
                                                                                ))
                                                                )
                                                )
                                                .then(
                                                        Commands.literal(
                                                                        "duration"
                                                                )
                                                                .then(
                                                                        Commands.argument(
                                                                                        "ctId",
                                                                                        IntegerArgumentType.integer(
                                                                                                0,
                                                                                                AwakeningConfig.MAX_CT_ID
                                                                                        )
                                                                                )
                                                                                .executes(context -> showDuration(
                                                                                        context.getSource()
                                                                                                .getServer(),
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "ctId"
                                                                                        )
                                                                                ))
                                                                                .then(
                                                                                        Commands.argument(
                                                                                                        "ticks",
                                                                                                        IntegerArgumentType.integer(
                                                                                                                -1
                                                                                                        )
                                                                                                )
                                                                                                .executes(context -> setDuration(
                                                                                                        context.getSource()
                                                                                                                .getServer(),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "ctId"
                                                                                                        ),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "ticks"
                                                                                                        )
                                                                                                ))
                                                                                )
                                                                )
                                                )
                                                .then(
                                                        Commands.literal(
                                                                        "power"
                                                                )
                                                                .then(
                                                                        Commands.argument(
                                                                                        "ctId",
                                                                                        IntegerArgumentType.integer(
                                                                                                0,
                                                                                                AwakeningConfig.MAX_CT_ID
                                                                                        )
                                                                                )
                                                                                .executes(context -> showPower(
                                                                                        context.getSource()
                                                                                                .getServer(),
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "ctId"
                                                                                        )
                                                                                ))
                                                                                .then(
                                                                                        Commands.argument(
                                                                                                        "power",
                                                                                                        IntegerArgumentType.integer(
                                                                                                                -1
                                                                                                        )
                                                                                                )
                                                                                                .executes(context -> setPower(
                                                                                                        context.getSource()
                                                                                                                .getServer(),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "ctId"
                                                                                                        ),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "power"
                                                                                                        )
                                                                                                ))
                                                                                )
                                                                )
                                                )
                                                .then(
                                                        Commands.literal(
                                                                        "output"
                                                                )
                                                                .then(
                                                                        Commands.argument(
                                                                                        "ctId",
                                                                                        IntegerArgumentType.integer(
                                                                                                0,
                                                                                                AwakeningConfig.MAX_CT_ID
                                                                                        )
                                                                                )
                                                                                .executes(context -> showOutput(
                                                                                        context.getSource()
                                                                                                .getServer(),
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "ctId"
                                                                                        )
                                                                                ))
                                                                                .then(
                                                                                        Commands.argument(
                                                                                                        "level",
                                                                                                        IntegerArgumentType.integer(
                                                                                                                -1,
                                                                                                                AwakeningConfig.MAX_AWAKENING_OUTPUT
                                                                                                        )
                                                                                                )
                                                                                                .executes(context -> setOutput(
                                                                                                        context.getSource()
                                                                                                                .getServer(),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "ctId"
                                                                                                        ),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "level"
                                                                                                        )
                                                                                                ))
                                                                                )
                                                                )
                                                )
                                                .then(
                                                        Commands.literal(
                                                                        "onetwenty"
                                                                )
                                                                .then(
                                                                        Commands.literal(
                                                                                        "duration"
                                                                                )
                                                                                .then(
                                                                                        Commands.argument(
                                                                                                        "ctId",
                                                                                                        IntegerArgumentType.integer(
                                                                                                                0,
                                                                                                                AwakeningConfig.MAX_CT_ID
                                                                                                        )
                                                                                                )
                                                                                                .executes(context -> showOneTwentyDuration(
                                                                                                        context.getSource()
                                                                                                                .getServer(),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "ctId"
                                                                                                        )
                                                                                                ))
                                                                                                .then(
                                                                                                        Commands.argument(
                                                                                                                        "ticks",
                                                                                                                        IntegerArgumentType.integer(
                                                                                                                                -1
                                                                                                                        )
                                                                                                                )
                                                                                                                .executes(context -> setOneTwentyDuration(
                                                                                                                        context.getSource()
                                                                                                                                .getServer(),
                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                context,
                                                                                                                                "ctId"
                                                                                                                        ),
                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                context,
                                                                                                                                "ticks"
                                                                                                                        )
                                                                                                                ))
                                                                                                )
                                                                                )
                                                                )
                                                                .then(
                                                                        Commands.literal(
                                                                                        "power"
                                                                                )
                                                                                .then(
                                                                                        Commands.argument(
                                                                                                        "ctId",
                                                                                                        IntegerArgumentType.integer(
                                                                                                                0,
                                                                                                                AwakeningConfig.MAX_CT_ID
                                                                                                        )
                                                                                                )
                                                                                                .executes(context -> showOneTwentyPower(
                                                                                                        context.getSource()
                                                                                                                .getServer(),
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "ctId"
                                                                                                        )
                                                                                                ))
                                                                                                .then(
                                                                                                        Commands.argument(
                                                                                                                        "power",
                                                                                                                        IntegerArgumentType.integer(
                                                                                                                                -1
                                                                                                                        )
                                                                                                                )
                                                                                                                .executes(context -> setOneTwentyPower(
                                                                                                                        context.getSource()
                                                                                                                                .getServer(),
                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                context,
                                                                                                                                "ctId"
                                                                                                                        ),
                                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                                context,
                                                                                                                                "power"
                                                                                                                        )
                                                                                                                ))
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                );
    }

    private static int showCurrent(
            MinecraftServer server,
            ServerPlayer player
    ) {

        int ctId =
                AwakeningConfig.getCurrentCtId(
                        player
                );

        AwakeningSettingsData data =
                AwakeningSettingsData.get(
                        server
                );

        player.sendSystemMessage(
                Component.literal(
                        "Current CT ID: "
                                + ctId
                                + ". Duration ticks: "
                                + data.getDuration(
                                        ctId,
                                        -1
                                )
                                + ". Power: "
                                + data.getPower(
                                        ctId,
                                        -1
                                )
                                + ". Output cap: "
                                + data.getOutput(
                                        ctId,
                                        -1
                                )
                                + ". 120 duration ticks: "
                                + data.getOneTwentyDuration(
                                        ctId,
                                        -1
                                )
                                + ". 120 power: "
                                + data.getOneTwentyPower(
                                        ctId,
                                        -1
                                )
                                + ". -1 means default."
                )
        );

        return 1;
    }

    private static int showDuration(
            MinecraftServer server,
            int ctId
    ) {

        return sendToOps(
                server,
                "Awakening duration for CT "
                        + ctId
                        + " is "
                        + AwakeningSettingsData.get(
                                server
                        ).getDuration(
                                ctId,
                                -1
                        )
                        + " ticks. -1 means default."
        );
    }

    private static int showOutput(
            MinecraftServer server,
            int ctId
    ) {

        return sendToOps(
                server,
                "Awakening output cap for CT "
                        + ctId
                        + " is "
                        + AwakeningSettingsData.get(
                                server
                        ).getOutput(
                                ctId,
                                -1
                        )
                        + ". -1 means JJKU default, 0 disables awakening output scaling and charge acceleration, 1-"
                        + AwakeningConfig.MAX_AWAKENING_OUTPUT
                        + " sets max cnt6 for damage/range and charge acceleration."
        );
    }

    private static int showPower(
            MinecraftServer server,
            int ctId
    ) {

        return sendToOps(
                server,
                "Awakening power for CT "
                        + ctId
                        + " is "
                        + AwakeningSettingsData.get(
                                server
                        ).getPower(
                                ctId,
                                -1
                        )
                        + ". -1 means default."
        );
    }

    private static int setOutput(
            MinecraftServer server,
            int ctId,
            int level
    ) {

        AwakeningSettingsData.get(
                server
        ).setOutput(
                ctId,
                level
        );

        return sendToOps(
                server,
                "Awakening output cap for CT "
                        + ctId
                        + " set to "
                        + level
                        + ". -1 means JJKU default, 0 disables awakening output scaling and charge acceleration, 1-"
                        + AwakeningConfig.MAX_AWAKENING_OUTPUT
                        + " sets max cnt6 for damage/range and charge acceleration."
        );
    }

    private static int showOneTwentyDuration(
            MinecraftServer server,
            int ctId
    ) {

        return sendToOps(
                server,
                "120% duration for CT "
                        + ctId
                        + " is "
                        + AwakeningSettingsData.get(
                                server
                        ).getOneTwentyDuration(
                                ctId,
                                -1
                        )
                        + " ticks. -1 means default."
        );
    }

    private static int showOneTwentyPower(
            MinecraftServer server,
            int ctId
    ) {

        return sendToOps(
                server,
                "120% power for CT "
                        + ctId
                        + " is "
                        + AwakeningSettingsData.get(
                                server
                        ).getOneTwentyPower(
                                ctId,
                                -1
                        )
                        + ". -1 means default."
        );
    }

    private static int setDuration(
            MinecraftServer server,
            int ctId,
            int ticks
    ) {

        AwakeningSettingsData.get(
                server
        ).setDuration(
                ctId,
                ticks
        );

        return sendToOps(
                server,
                "Awakening duration for CT "
                        + ctId
                        + " set to "
                        + ticks
                        + " ticks. -1 means default."
        );
    }

    private static int setPower(
            MinecraftServer server,
            int ctId,
            int power
    ) {

        AwakeningSettingsData.get(
                server
        ).setPower(
                ctId,
                power
        );

        return sendToOps(
                server,
                "Awakening power for CT "
                        + ctId
                        + " set to "
                        + power
                        + ". -1 means default."
        );
    }

    private static int setOneTwentyDuration(
            MinecraftServer server,
            int ctId,
            int ticks
    ) {

        AwakeningSettingsData.get(
                server
        ).setOneTwentyDuration(
                ctId,
                ticks
        );

        return sendToOps(
                server,
                "120% duration for CT "
                        + ctId
                        + " set to "
                        + ticks
                        + " ticks. -1 means default."
        );
    }

    private static int setOneTwentyPower(
            MinecraftServer server,
            int ctId,
            int power
    ) {

        AwakeningSettingsData.get(
                server
        ).setOneTwentyPower(
                ctId,
                power
        );

        return sendToOps(
                server,
                "120% power for CT "
                        + ctId
                        + " set to "
                        + power
                        + ". -1 means default."
        );
    }

    private static int sendToOps(
            MinecraftServer server,
            String message
    ) {

        server.getPlayerList()
                .getPlayers()
                .forEach(player -> {
                    if (server.getPlayerList()
                            .isOp(
                                    player.getGameProfile()
                            )) {
                        player.sendSystemMessage(
                                Component.literal(
                                        message
                                )
                        );
                    }
                });

        return 1;
    }
}
