package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.procedures.GojoAwakeningOnEffectActiveTickProcedure;

import net.marma.jjkfys.AwakeningConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GojoAwakeningOnEffectActiveTickProcedure.class, remap = false)
public abstract class GojoAwakeningChargeTickMixin {

    @Unique
    private static final String jjkfys$CNT6_TAG = "cnt6";

    @Unique
    private static final String jjkfys$BONUS_TAG =
            "jjkfys_awakening_cnt6_bonus";

    @Unique
    private static final String jjkfys$LAST_TAG =
            "jjkfys_awakening_cnt6_last";

    @Unique
    private static final double jjkfys$EPSILON = 0.001D;

    @Unique
    private static final ThreadLocal<Double> jjkfys$cnt6Before =
            new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<Double> jjkfys$bonusBefore =
            new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<Integer> jjkfys$configuredOutput =
            new ThreadLocal<>();

    @Inject(
            method = "execute",
            at = @At("HEAD")
    )
    private static void jjkfys$captureCnt6(
            Entity entity,
            CallbackInfo ci
    ) {

        jjkfys$cnt6Before.remove();
        jjkfys$bonusBefore.remove();
        jjkfys$configuredOutput.remove();

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        int configuredOutput =
                AwakeningConfig.getAwakeningOutput(
                        player
                );

        if (configuredOutput < 0) {
            return;
        }

        CompoundTag data =
                player.getPersistentData();

        double currentCnt6 =
                data.getDouble(
                        jjkfys$CNT6_TAG
                );

        double trackedBonus =
                0.0D;

        if (data.contains(
                jjkfys$LAST_TAG
        )) {
            double lastControlledCnt6 =
                    data.getDouble(
                            jjkfys$LAST_TAG
                    );

            if (Math.abs(
                    currentCnt6 - lastControlledCnt6
            ) <= jjkfys$EPSILON) {
                trackedBonus =
                        Math.max(
                                0.0D,
                                data.getDouble(
                                        jjkfys$BONUS_TAG
                                )
                        );
            }
        }

        jjkfys$cnt6Before.set(
                currentCnt6
        );
        jjkfys$bonusBefore.set(
                trackedBonus
        );
        jjkfys$configuredOutput.set(
                configuredOutput
        );
    }

    @Inject(
            method = "execute",
            at = @At("TAIL")
    )
    private static void jjkfys$restoreConfiguredCnt6(
            Entity entity,
            CallbackInfo ci
    ) {

        try {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            Double before =
                    jjkfys$cnt6Before.get();
            Double bonusBefore =
                    jjkfys$bonusBefore.get();
            Integer configuredOutput =
                    jjkfys$configuredOutput.get();

            if (before == null
                    || bonusBefore == null
                    || configuredOutput == null) {
                return;
            }

            CompoundTag data =
                    player.getPersistentData();

            double after =
                    data.getDouble(
                            jjkfys$CNT6_TAG
                    );

            double nativeIncrease =
                    Math.max(
                            0.0D,
                            after - before
                    );

            double bonusAfter =
                    Math.min(
                            bonusBefore + nativeIncrease,
                            configuredOutput
                    );

            double baseCnt6 =
                    Math.max(
                            0.0D,
                            before - bonusBefore
                    );

            double controlledCnt6 =
                    Math.max(
                            0.0D,
                            baseCnt6 + bonusAfter
                    );

            data.putDouble(
                    jjkfys$CNT6_TAG,
                    controlledCnt6
            );
            data.putDouble(
                    jjkfys$BONUS_TAG,
                    bonusAfter
            );
            data.putDouble(
                    jjkfys$LAST_TAG,
                    controlledCnt6
            );
        } finally {
            jjkfys$cnt6Before.remove();
            jjkfys$bonusBefore.remove();
            jjkfys$configuredOutput.remove();
        }
    }
}
