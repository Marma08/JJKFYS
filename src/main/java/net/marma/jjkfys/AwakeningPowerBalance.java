package net.marma.jjkfys;

import net.minecraft.server.level.ServerPlayer;

public final class AwakeningPowerBalance {

    private AwakeningPowerBalance() {
    }

    public static int getBonus(
            ServerPlayer player,
            String awakeningId
    ) {

        if (awakeningId == null
                || awakeningId.isEmpty()) {
            return 0;
        }

        return AwakeningConfig.getAwakeningPower(
                player
        );
    }
}
