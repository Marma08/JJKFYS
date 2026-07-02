package net.marma.jjkfys;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class CursedSpiritRaceHelper {

    private CursedSpiritRaceHelper() {
    }

    public static boolean isCursedSpiritRace(
            Entity entity
    ) {

        if (!(entity instanceof Player)) {
            return false;
        }

        boolean addonRace =
                entity.getCapability(
                JujutsucraftaddonModVariables
                        .PLAYER_VARIABLES_CAPABILITY,
                null
        ).map(vars -> vars.IsCursedSpirit)
                .orElse(false);

        return addonRace
                || entity.getPersistentData()
                        .getBoolean("CursedSpirit")
                || entity.getPersistentData()
                        .getDouble("CursedSpirit") == 1;
    }
}
