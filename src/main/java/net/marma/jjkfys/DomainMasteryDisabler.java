package net.marma.jjkfys;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;

import net.marma.jjkfys.init.JJKFysGameRules;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JJKFYS.MODID)
public final class DomainMasteryDisabler {

    private DomainMasteryDisabler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.level().isClientSide()
                || !isDisabled(
                        player.level()
                )) {
            return;
        }

        forceNormalDomain(
                player
        );
    }

    public static boolean isDisabled(
            Entity entity
    ) {

        return entity != null
                && isDisabled(
                        entity.level()
                );
    }

    public static boolean isDisabled(
            Level level
    ) {

        return level != null
                && level.getGameRules()
                        .getBoolean(
                                JJKFysGameRules.DISABLE_DOMAIN_MASTERY
                        );
    }

    public static void forceNormalDomain(
            Entity entity
    ) {

        if (entity == null) {
            return;
        }

        entity.getCapability(
                JujutsucraftaddonModVariables
                        .PLAYER_VARIABLES_CAPABILITY,
                null
        ).ifPresent(vars -> {
            boolean changed =
                    vars.DomainType != 0.0D
                            || vars.domainStyle != 0
                            || vars.BarrierlessDomain;

            if (!changed) {
                return;
            }

            vars.DomainType = 0.0D;
            vars.domainStyle = 0;
            vars.BarrierlessDomain = false;
            vars.syncPlayerVariables(
                    entity
            );
        });
    }
}
