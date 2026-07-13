package net.marma.jjkfys;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CursedSpiritNightVisionFix {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        if (!(event.player instanceof ServerPlayer player)) return;

        boolean cursedSpirit =
                player.getPersistentData()
                        .getBoolean("CursedSpirit");

        if (!cursedSpirit) return;

        MobEffectInstance current =
                player.getEffect(MobEffects.NIGHT_VISION);

        if (current == null || current.getDuration() <= 210) {

            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.NIGHT_VISION,
                            999999999,
                            0,
                            false,
                            false
                    )
            );
        }
    }
}