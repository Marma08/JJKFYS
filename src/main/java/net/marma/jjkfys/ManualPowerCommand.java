package net.marma.jjkfys;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ManualPowerCommand {

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("jjkfyspower")
                        .requires(source -> source.hasPermission(2))

                        .then(
                                Commands.argument("target",
                                                EntityArgument.player())

                                        .then(
                                                Commands.argument("power",
                                                                IntegerArgumentType.integer(0))

                                                        .executes(context -> {

                                                            ServerPlayer player =
                                                                    EntityArgument.getPlayer(
                                                                            context,
                                                                            "target"
                                                                    );

                                                            int power =
                                                                    IntegerArgumentType.getInteger(
                                                                            context,
                                                                            "power"
                                                                    );

                                                            if (power > 0) {
                                                                player.getPersistentData()
                                                                        .putInt(
                                                                                "jjkfys_manualpower",
                                                                                power
                                                                        );
                                                            } else {
                                                                player.getPersistentData()
                                                                        .remove(
                                                                                "jjkfys_manualpower"
                                                                        );
                                                            }

                                                            PowerHandler.refreshPower(
                                                                    player
                                                            );

                                                            context.getSource()
                                                                    .sendSuccess(
                                                                            () -> Component.literal(
                                                                                    getFeedbackMessage(
                                                                                            player,
                                                                                            power
                                                                                    )
                                                                            ),
                                                                            true
                                                                    );

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }

    private static String getFeedbackMessage(
            ServerPlayer player,
            int power
    ) {

        if (power > 0) {
            return "Manual base power for "
                    + player.getGameProfile().getName()
                    + " set to "
                    + power
                    + ". Active bonuses will stack on top.";
        }

        return "Manual base power for "
                + player.getGameProfile().getName()
                + " cleared. Rebirth base power restored.";
    }
}
