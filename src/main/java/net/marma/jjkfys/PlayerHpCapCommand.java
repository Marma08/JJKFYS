package net.marma.jjkfys;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerHpCapCommand {

    private static final String SAVE_TAG =
            "jjkfys_save";

    private static final String PLAYER_HP_CAP_TAG =
            "jjkfys_player_hp_cap";

    @SubscribeEvent
    public static void registerCommand(
            RegisterCommandsEvent event
    ) {

        event.getDispatcher()
                .register(
                        Commands.literal(
                                        "jjkfysplayerhpcap"
                                )
                                .requires(source -> source.hasPermission(2))
                                .then(
                                        Commands.argument(
                                                        "target",
                                                        EntityArgument.player()
                                                )
                                                .then(
                                                        Commands.argument(
                                                                        "cap",
                                                                        IntegerArgumentType.integer(0)
                                                                )
                                                                .executes(context -> {

                                                                    ServerPlayer player =
                                                                            EntityArgument.getPlayer(
                                                                                    context,
                                                                                    "target"
                                                                            );

                                                                    int cap =
                                                                            IntegerArgumentType.getInteger(
                                                                                    context,
                                                                                    "cap"
                                                                            );

                                                                    if (cap > 0) {
                                                                        player.getPersistentData()
                                                                                .putInt(
                                                                                        PLAYER_HP_CAP_TAG,
                                                                                        cap
                                                                                );
                                                                        applyHpCap(
                                                                                player
                                                                        );
                                                                    } else {
                                                                        player.getPersistentData()
                                                                                .remove(
                                                                                        PLAYER_HP_CAP_TAG
                                                                                );
                                                                    }

                                                                    context.getSource()
                                                                            .sendSuccess(
                                                                                    () -> Component.literal(
                                                                                            getFeedbackMessage(
                                                                                                    player,
                                                                                                    cap
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        applyHpCap(
                player
        );
    }

    @SubscribeEvent
    public static void onPlayerClone(
            PlayerEvent.Clone event
    ) {

        if (!event.isWasDeath()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer newPlayer)
                || !(event.getOriginal() instanceof ServerPlayer oldPlayer)) {
            return;
        }

        if (oldPlayer.getPersistentData()
                .contains(PLAYER_HP_CAP_TAG)) {

            newPlayer.getPersistentData()
                    .putInt(
                            PLAYER_HP_CAP_TAG,
                            oldPlayer.getPersistentData()
                                    .getInt(PLAYER_HP_CAP_TAG)
                    );
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        applyHpCap(
                player
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSave(
            PlayerEvent.SaveToFile event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        applyHpCap(
                player
        );
    }

    private static void applyHpCap(
            ServerPlayer player
    ) {

        int cap =
                player.getPersistentData()
                        .getInt(PLAYER_HP_CAP_TAG);

        if (cap <= 0) {
            return;
        }

        player.getCapability(
                JujutsucraftaddonModVariables
                        .PLAYER_VARIABLES_CAPABILITY
        ).ifPresent(vars -> {

            boolean changed =
                    false;

            if (vars.HealthAttribute > cap) {
                vars.HealthAttribute =
                        cap;
                changed =
                        true;
            }

            CompoundTag data =
                    player.getPersistentData()
                            .getCompound(SAVE_TAG);

            if (data.getDouble("hp") > cap) {
                data.putDouble(
                        "hp",
                        cap
                );

                player.getPersistentData()
                        .put(
                                SAVE_TAG,
                                data
                        );

                changed =
                        true;
            }

            if (player.getAttribute(
                    Attributes.MAX_HEALTH
            ) != null
                    && player.getAttribute(
                    Attributes.MAX_HEALTH
            ).getBaseValue() > cap) {

                player.getAttribute(
                        Attributes.MAX_HEALTH
                ).setBaseValue(
                        cap
                );

                changed =
                        true;
            }

            if (player.getHealth() > cap) {
                player.setHealth(
                        cap
                );
            }

            if (changed) {
                vars.syncPlayerVariables(
                        player
                );
            }
        });
    }

    private static String getFeedbackMessage(
            ServerPlayer player,
            int cap
    ) {

        if (cap > 0) {
            return "Permanent HP cap for "
                    + player.getGameProfile()
                    .getName()
                    + " set to "
                    + cap
                    + ". Current saved HP was clamped if needed.";
        }

        return "Permanent HP cap for "
                + player.getGameProfile()
                .getName()
                + " cleared.";
    }
}
