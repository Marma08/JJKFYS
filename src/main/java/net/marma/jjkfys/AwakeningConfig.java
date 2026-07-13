package net.marma.jjkfys;

import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

public final class AwakeningConfig {

    public static final int MAX_CT_ID = 200;
    public static final int MAX_AWAKENING_OUTPUT = 30;
    public static final int DEFAULT_AWAKENING_POWER = 3;
    public static final int DEFAULT_AWAKENING_OUTPUT = -1;
    public static final int DEFAULT_ONE_TWENTY_DURATION = 1200;
    public static final int DEFAULT_ONE_TWENTY_POWER = 2;

    private AwakeningConfig() {
    }

    public static int getCurrentCtId(
            ServerPlayer player
    ) {

        return player.getCapability(
                JujutsucraftModVariables
                        .PLAYER_VARIABLES_CAPABILITY,
                Direction.DOWN
        ).map(vars -> {

            int secondary =
                    (int) Math.round(
                            vars.PlayerCurseTechnique2
                    );

            if (secondary > 0) {
                return secondary;
            }

            return (int) Math.round(
                    vars.PlayerCurseTechnique
            );
        }).orElse(0);
    }

    public static int getAwakeningDurationTicks(
            ServerPlayer player,
            int fallback
    ) {

        return getCtRuleValue(
                player,
                SettingType.DURATION,
                fallback
        );
    }

    public static int getAwakeningPower(
            ServerPlayer player
    ) {

        return getCtRuleValue(
                player,
                SettingType.POWER,
                DEFAULT_AWAKENING_POWER
        );
    }

    public static int getAwakeningOutput(
            ServerPlayer player
    ) {

        int configuredOutput =
                getCtRuleValue(
                player,
                SettingType.OUTPUT,
                DEFAULT_AWAKENING_OUTPUT
        );

        if (configuredOutput < 0) {
            return DEFAULT_AWAKENING_OUTPUT;
        }

        return Math.min(
                configuredOutput,
                MAX_AWAKENING_OUTPUT
        );
    }

    public static int getDefaultAwakeningOutputCap(
            ServerPlayer player
    ) {

        return getPrimaryCtId(
                player
        ) == 2 ? 30 : 15;
    }

    private static int getPrimaryCtId(
            ServerPlayer player
    ) {

        return player.getCapability(
                JujutsucraftModVariables
                        .PLAYER_VARIABLES_CAPABILITY,
                Direction.DOWN
        ).map(vars -> (int) Math.round(
                vars.PlayerCurseTechnique
        )).orElse(0);
    }

    public static int getOneTwentyDurationTicks(
            ServerPlayer player,
            int fallback
    ) {

        return getCtRuleValue(
                player,
                SettingType.ONE_TWENTY_DURATION,
                fallback
        );
    }

    public static int getOneTwentyPower(
            ServerPlayer player
    ) {

        return getCtRuleValue(
                player,
                SettingType.ONE_TWENTY_POWER,
                DEFAULT_ONE_TWENTY_POWER
        );
    }

    private static int getCtRuleValue(
            ServerPlayer player,
            SettingType type,
            int fallback
    ) {

        int ctId =
                getCurrentCtId(
                        player
                );

        if (ctId < 0
                || ctId > MAX_CT_ID
                || player.getServer() == null) {
            return fallback;
        }

        AwakeningSettingsData data =
                AwakeningSettingsData.get(
                        player.getServer()
                );

        return switch (type) {
            case DURATION -> data.getDuration(
                    ctId,
                    fallback
            );
            case POWER -> data.getPower(
                    ctId,
                    fallback
            );
            case OUTPUT -> data.getOutput(
                    ctId,
                    fallback
            );
            case ONE_TWENTY_DURATION -> data.getOneTwentyDuration(
                    ctId,
                    fallback
            );
            case ONE_TWENTY_POWER -> data.getOneTwentyPower(
                    ctId,
                    fallback
            );
        };
    }

    private enum SettingType {
        DURATION,
        POWER,
        OUTPUT,
        ONE_TWENTY_DURATION,
        ONE_TWENTY_POWER
    }
}
