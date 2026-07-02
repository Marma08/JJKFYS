package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;
import com.jujutsu.jujutsucraftaddon.init.JujutsucraftaddonModMobEffects;

import net.marma.jjkfys.AwakeningConfig;

import net.mcreator.jujutsucraft.procedures.KeyStartTechniqueOnKeyPressedProcedure;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyStartTechniqueOnKeyPressedProcedure.class, priority = -20000)
public abstract class AwakeningChargeBonusMixin {

    @Unique
    private static final ThreadLocal<Double> jjkfys$cnt6Before =
            new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<Double> jjkfys$outputBefore =
            new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<Integer> jjkfys$configuredOutput =
            new ThreadLocal<>();

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            remap = false
    )
    private static void jjkfys$captureChargeState(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        jjkfys$cnt6Before.remove();
        jjkfys$outputBefore.remove();
        jjkfys$configuredOutput.remove();

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        if (!player.hasEffect(
                JujutsucraftaddonModMobEffects
                        .GOJO_AWAKENING
                        .get()
        )) {
            return;
        }

        int configuredOutput =
                AwakeningConfig.getAwakeningOutput(
                        player
                );

        if (configuredOutput < 0) {
            return;
        }

        double output =
                player.getCapability(
                        JujutsucraftaddonModVariables
                                .PLAYER_VARIABLES_CAPABILITY,
                        null
                ).map(vars -> vars.Output)
                        .orElse(0.0D);

        if (output <= 0.0D) {
            output = 0.0D;
        }

        jjkfys$cnt6Before.set(
                player.getPersistentData()
                        .getDouble(
                                "cnt6"
                        )
        );

        jjkfys$outputBefore.set(
                output
        );
        jjkfys$configuredOutput.set(
                configuredOutput
        );
    }

    @Inject(
            method = "execute",
            at = @At("TAIL"),
            remap = false
    )
    private static void jjkfys$applyConfiguredChargeBonus(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        try {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            if (!player.hasEffect(
                    JujutsucraftaddonModMobEffects
                            .GOJO_AWAKENING
                            .get()
            )) {
                return;
            }

            Double cnt6Before =
                    jjkfys$cnt6Before.get();

            Double outputBefore =
                    jjkfys$outputBefore.get();
            Integer configuredOutput =
                    jjkfys$configuredOutput.get();

            if (cnt6Before == null
                    || outputBefore == null
                    || configuredOutput == null
                    || configuredOutput < 0) {
                return;
            }

            double currentCnt6 =
                    player.getPersistentData()
                            .getDouble(
                                    "cnt6"
                            );

            double adjustedCnt6 = currentCnt6;

            if (configuredOutput >= 0
                    && outputBefore > 0.0D
                    && cnt6Before <= outputBefore
                    && currentCnt6 > cnt6Before) {
                adjustedCnt6 =
                        currentCnt6
                                - outputBefore
                                + configuredOutput;
            }

            player.getPersistentData()
                    .putDouble(
                            "cnt6",
                            Math.max(
                                    0.0D,
                                    adjustedCnt6
                            )
                    );
        } finally {
            jjkfys$cnt6Before.remove();
            jjkfys$outputBefore.remove();
            jjkfys$configuredOutput.remove();
        }
    }
}
