package net.marma.jjkfys.mixin;

import net.marma.jjkfys.CursedSpiritRaceHelper;

import com.jujutsu.jujutsucraftaddon.init.JujutsucraftaddonModGameRules;
import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;

import net.mcreator.jujutsucraft.init.JujutsucraftModMobEffects;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.KeyReverseCursedTechniqueOnKeyPressedProcedure;

import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        value = KeyReverseCursedTechniqueOnKeyPressedProcedure.class,
        priority = -20000
)
public class CursedSpiritRctKeyMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private static void jjkfys$usePlayerAdvancementPath(
            Entity entity,
            CallbackInfo ci
    ) {

        if (!(entity instanceof Player player)
                || !CursedSpiritRaceHelper.isCursedSpiritRace(entity)) {
            return;
        }

        ci.cancel();

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        if (entity.level()
                .getGameRules()
                .getBoolean(
                        JujutsucraftaddonModGameRules
                                .JJKU_DISABLE_RCT
                )) {
            return;
        }

        if (living.hasEffect(
                JujutsucraftModMobEffects
                        .REVERSE_CURSED_TECHNIQUE
                        .get()
        )) {
            return;
        }

        if (living.hasEffect(
                JujutsucraftModMobEffects
                        .CURSED_TECHNIQUE
                        .get()
        ) || entity.getPersistentData()
                .getDouble("skill") != 0) {

            player.displayClientMessage(
                    Component.translatable(
                            "jujutsu.message.dont_use"
                    ),
                    false
            );
            return;
        }

        if (!(entity instanceof ServerPlayer serverPlayer)
                || !hasRctAdvancement(serverPlayer)) {

            player.displayClientMessage(
                    Component.translatable(
                            "jujutsu.message.not_mastered"
                    ),
                    false
            );
            return;
        }

        JujutsucraftModVariables.PlayerVariables baseVars =
                entity.getCapability(
                        JujutsucraftModVariables
                                .PLAYER_VARIABLES_CAPABILITY,
                        null
                ).orElse(
                        new JujutsucraftModVariables.PlayerVariables()
                );

        if (baseVars.PlayerCursePowerFormer <= 150
                || baseVars.PlayerCursePower < 10) {

            player.displayClientMessage(
                    Component.translatable(
                            "jujutsu.message.not_mastered"
                    ),
                    false
            );
            return;
        }

        entity.getPersistentData()
                .putBoolean(
                        "PRESS_M",
                        true
                );

        living.removeEffect(
                JujutsucraftModMobEffects
                        .GUARD
                        .get()
        );

        JujutsucraftaddonModVariables.PlayerVariables addonVars =
                entity.getCapability(
                        JujutsucraftaddonModVariables
                                .PLAYER_VARIABLES_CAPABILITY,
                        null
                ).orElse(
                        new JujutsucraftaddonModVariables.PlayerVariables()
                );

        int rctLevel =
                Math.max(
                        1,
                        (int) (addonVars.RCTCount / 5000)
                );

        if (addonVars.RCTLimitLevel > 0) {
            rctLevel =
                    Math.min(
                            rctLevel,
                            (int) addonVars.RCTLimitLevel
                    );
        }

        living.addEffect(
                new MobEffectInstance(
                        JujutsucraftModMobEffects
                                .REVERSE_CURSED_TECHNIQUE
                                .get(),
                        Integer.MAX_VALUE,
                        rctLevel,
                        false,
                        false
                )
        );
    }

    private static boolean hasRctAdvancement(
            ServerPlayer player
    ) {

        return hasAdvancement(
                player,
                "reverse_cursed_technique_1"
        ) || hasAdvancement(
                player,
                "reverse_cursed_technique_2"
        ) || player.hasEffect(
                JujutsucraftModMobEffects
                        .SUKUNA_EFFECT
                        .get()
        );
    }

    private static boolean hasAdvancement(
            ServerPlayer player,
            String path
    ) {

        Advancement advancement =
                player.server
                        .getAdvancements()
                        .getAdvancement(
                                new ResourceLocation(
                                        "jujutsucraft",
                                        path
                                )
                        );

        return advancement != null
                && player.getAdvancements()
                        .getOrStartProgress(advancement)
                        .isDone();
    }
}
