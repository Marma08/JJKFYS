package net.marma.jjkfys;

import com.jujutsu.jujutsucraftaddon.init.JujutsucraftaddonModMobEffects;
import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;

import net.marma.jjkfys.mixin.MobEffectInstanceAccessor;

import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import net.minecraft.world.entity.LivingEntity;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PowerHandler {

    private static final String EXTERNAL_TAG =
            "jjkfys_external_bonus";

    private static final String EXTERNAL_TTL_TAG =
            "jjkfys_external_bonus_ttl";

    private static final String LAST_APPLIED_TAG =
            "jjkfys_last_applied_power";

    private static final String AWAKENING_EFFECT_SEEN_TAG =
            "jjkfys_awakening_effect_seen";

    private static final String AWAKENING_UNTIL_TAG =
            "jjkfys_awakening_until";

    private static final String WARRIOR_OUTPUT_SEEN_TAG =
            "jjkfys_warrior_output_seen";

    private static final String WARRIOR_UNTIL_TAG =
            "jjkfys_warrior_until";

    private static final String CONFIGURED_AWAKENING_ID_TAG =
            "jjkfys_configured_awakening_id";

    private static final String CONFIGURED_AWAKENING_UNTIL_TAG =
            "jjkfys_configured_awakening_until";

    private static final String CONFIGURED_LEGACY_AWAKENING_UNTIL_TAG =
            "jjkfys_configured_legacy_awakening_until";

    private static final int EXTERNAL_TTL =
            20;

    private static final int WARRIOR_DURATION =
            1200;

    @SubscribeEvent
    public static void onPlayerTick(
            TickEvent.PlayerTickEvent event
    ) {

        if (event.phase != Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        applyMergedPower(player);
    }

    @SubscribeEvent
    public static void onRespawn(
            PlayerEvent.PlayerRespawnEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        clearExternalBonus(
                player
        );

        clearWarriorBonus(
                player
        );

        clearAwakeningTimer(
                player
        );

        applyMergedPower(player);
    }

    private static void applyMergedPower(
            ServerPlayer player
    ) {

        player.getCapability(
                JujutsucraftaddonModVariables
                        .PLAYER_VARIABLES_CAPABILITY,
                Direction.DOWN
        ).ifPresent(vars -> {

            int rebirthMinimum =
                    getRebirthMinimum(
                            player,
                            vars.rebirthLevel
                    );

            enforceMinimumLevel(
                    player,
                    rebirthMinimum
            );

            applyAwakeningTiming(
                    player,
                    vars
            );

            applyOneTwentyTiming(
                    player
            );

            applyMergedAmplifier(
                    player,
                    vars,
                    rebirthMinimum
            );
        });
    }

    public static void refreshPower(
            ServerPlayer player
    ) {

        applyMergedPower(
                player
        );
    }

    private static int getRebirthMinimum(
            ServerPlayer player,
            int rebirths
    ) {

        int rebirthMinimum = 19;

        if (rebirths >= 1) {
            rebirthMinimum = 27;
        }

        if (rebirths >= 2) {
            rebirthMinimum = 30;
        }

        if (rebirths >= 3) {
            rebirthMinimum = 32;
        }

        int manualPower =
                player.getPersistentData()
                        .getInt(
                                "jjkfys_manualpower"
                        );

        if (manualPower > 0) {
            rebirthMinimum =
                    manualPower;
        }

        return rebirthMinimum;
    }

    private static void enforceMinimumLevel(
            ServerPlayer player,
            int rebirthMinimum
    ) {

        player.getCapability(
                JujutsucraftModVariables
                        .PLAYER_VARIABLES_CAPABILITY,
                Direction.DOWN
        ).ifPresent(baseVars -> {

            if (baseVars.PlayerLevel
                    < rebirthMinimum) {

                baseVars.PlayerLevel =
                        rebirthMinimum;

                baseVars.syncPlayerVariables(
                        player
                );
            }
        });
    }

    private static void applyMergedAmplifier(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars,
            int rebirthMinimum
    ) {

        MobEffectInstance current =
                player.getEffect(
                        MobEffects.DAMAGE_BOOST
                );

        int currentAmp =
                current != null
                        ? current.getAmplifier()
                        : 0;

        int knownExternal =
                getKnownExternalBonus(
                        player,
                        vars
                );

        updateObservedExternalBonus(
                player,
                currentAmp,
                toAmplifier(
                        rebirthMinimum
                ),
                knownExternal > 0
        );

        int externalBonus =
                knownExternal;

        int finalAmp =
                toAmplifier(
                        rebirthMinimum
                                + externalBonus
                );

        if (externalBonus > 0) {
            player.getPersistentData()
                    .putInt(
                            EXTERNAL_TAG,
                            externalBonus
                    );

            player.getPersistentData()
                    .putInt(
                            EXTERNAL_TTL_TAG,
                            0
                    );
        } else {
            clearExternalBonus(
                    player
            );
        }

        if (current != null
                && current.getAmplifier()
                == finalAmp
                && current.getDuration() > 10) {

            player.getPersistentData()
                    .putInt(
                            LAST_APPLIED_TAG,
                            finalAmp
                    );

            return;
        }

        boolean downshifting =
                current != null
                        && current.getAmplifier()
                        > finalAmp;

        if (downshifting) {
            player.removeEffect(
                    MobEffects.DAMAGE_BOOST
            );
        }

        int duration =
                current != null
                        && !downshifting
                        ? Math.max(
                                current.getDuration(),
                                40
                        )
                        : 40;

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        duration,
                        finalAmp,
                        false,
                        false
                )
        );

        player.getPersistentData()
                .putInt(
                        LAST_APPLIED_TAG,
                        finalAmp
                );
    }

    private static int updateObservedExternalBonus(
            ServerPlayer player,
            int currentAmp,
            int rebirthMinimumAmp,
            boolean hasKnownExternal
    ) {

        clearExternalBonus(
                player
        );

        return 0;
    }

    private static void clearExternalBonus(
            ServerPlayer player
    ) {

        player.getPersistentData()
                .putInt(
                        EXTERNAL_TAG,
                        0
                );

        player.getPersistentData()
                .putInt(
                        EXTERNAL_TTL_TAG,
                        0
                );
    }

    private static int toAmplifier(
            int visiblePower
    ) {

        return Math.max(
                0,
                visiblePower
        );
    }

    private static int getKnownExternalBonus(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        int bonus = 0;
        int awakeningBonus = 0;

        if (vars.awakening != null
                && vars.awakening.active
                && isAwakeningTimeActive(
                        player,
                        vars
                )
                && vars.awakening.activeAwakeningId != null
                && !vars.awakening.activeAwakeningId.isEmpty()) {

            awakeningBonus =
                            Math.max(
                            awakeningBonus,
                            getAwakeningBonus(
                                    player,
                                    vars.awakening.activeAwakeningId
                            )
                    );
        }

        awakeningBonus =
                Math.max(
                        awakeningBonus,
                        getLegacyAwakeningBonus(
                                player,
                                vars
                        )
                );

        bonus +=
                awakeningBonus;

        bonus +=
                getWarriorBonus(
                        player,
                        vars
                );

        return bonus;
    }

    private static int getLegacyAwakeningBonus(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        if (isLegacyAwakeningTimeActive(
                player,
                vars
        )) {
            return AwakeningConfig.getAwakeningPower(
                    player
            );
        }

        return getLegacyAwakeningEffectBonus(
                player
        );
    }

    private static int getLegacyAwakeningEffectBonus(
            ServerPlayer player
    ) {

        if (hasLegacyAwakeningEffect(
                player
        )) {
            return AwakeningConfig.getAwakeningPower(
                    player
            );
        }

        clearAwakeningTimer(
                player
        );

        return 0;
    }

    private static boolean hasLegacyAwakeningEffect(
            ServerPlayer player
    ) {

        return hasEffect(
                player,
                JujutsucraftaddonModMobEffects.GOJO_AWAKENING.get()
        ) || hasEffect(
                player,
                JujutsucraftaddonModMobEffects.GOJO_AWAKENING_1.get()
        ) || hasEffect(
                player,
                JujutsucraftaddonModMobEffects.ITADORI_AWAKENING.get()
        ) || hasEffect(
                player,
                JujutsucraftaddonModMobEffects.NAOYA_AWAKENING.get()
        ) || hasEffect(
                player,
                JujutsucraftaddonModMobEffects.SUKUNA_AWAKEN_2.get()
        ) || hasEffect(
                player,
                JujutsucraftaddonModMobEffects.SUKUNA_POWERS.get()
        );
    }

    private static void clearAwakeningTimer(
            ServerPlayer player
    ) {

        player.getPersistentData()
                .putBoolean(
                        AWAKENING_EFFECT_SEEN_TAG,
                        false
                );

        player.getPersistentData()
                .putLong(
                        AWAKENING_UNTIL_TAG,
                        0
                );
    }

    private static boolean isLegacyAwakeningTimeActive(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        long now =
                player.level().getGameTime();

        return vars.AwakeningType > 0
                && vars.AwakeningActiveUntil > now;
    }

    private static boolean isAwakeningTimeActive(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        return vars.awakening.activeUntilGameTime > 0
                && player.level().getGameTime()
                < vars.awakening.activeUntilGameTime;
    }

    private static int getWarriorBonus(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        MobEffectInstance oneTwenty =
                player.getEffect(
                        JujutsucraftaddonModMobEffects
                                .ONE_HUNDRED_AND_TWENTY
                                .get()
                );

        if (oneTwenty == null) {
            clearWarriorBonus(
                    player
            );

            return 0;
        }

        if (!"Warrior".equals(
                vars.Profession
        )) {
            return 0;
        }

        player.getPersistentData()
                .putBoolean(
                        WARRIOR_OUTPUT_SEEN_TAG,
                        true
                );

        return AwakeningConfig.getOneTwentyPower(
                player
        );
    }

    private static int getEffectDuration(
            LivingEntity living,
            MobEffect effect
    ) {

        if (effect == null) {
            return 0;
        }

        MobEffectInstance instance =
                living.getEffect(
                        effect
                );

        return instance != null
                ? instance.getDuration()
                : 0;
    }

    private static void clearWarriorBonus(
            ServerPlayer player
    ) {

        player.getPersistentData()
                .putBoolean(
                        WARRIOR_OUTPUT_SEEN_TAG,
                        false
                );

        player.getPersistentData()
                .putLong(
                        WARRIOR_UNTIL_TAG,
                        0
                );
    }

    private static void applyOneTwentyTiming(
            ServerPlayer player
    ) {

        MobEffect oneTwentyEffect =
                JujutsucraftaddonModMobEffects
                        .ONE_HUNDRED_AND_TWENTY
                        .get();

        MobEffectInstance oneTwenty =
                player.getEffect(
                        oneTwentyEffect
                );

        if (oneTwenty == null) {
            clearWarriorBonus(
                    player
            );
            return;
        }

        long now =
                player.level()
                        .getGameTime();

        long configuredUntil =
                player.getPersistentData()
                        .getLong(
                                WARRIOR_UNTIL_TAG
                        );

        if (configuredUntil <= 0) {
            int configuredDuration =
                    AwakeningConfig.getOneTwentyDurationTicks(
                            player,
                            oneTwenty.getDuration()
                    );

            configuredUntil =
                    now + Math.max(
                            0,
                            configuredDuration
                    );

            player.getPersistentData()
                    .putLong(
                            WARRIOR_UNTIL_TAG,
                            configuredUntil
                    );
        }

        int remaining =
                (int) Math.max(
                        0,
                        configuredUntil - now
                );

        if (remaining <= 0) {
            player.removeEffect(
                    oneTwentyEffect
            );

            clearWarriorBonus(
                    player
            );
            return;
        }

        ((MobEffectInstanceAccessor) oneTwenty)
                .jjkfys$setDuration(
                        remaining
                );
    }

    private static int getAwakeningBonus(
            ServerPlayer player,
            String awakeningId
    ) {

        return AwakeningPowerBalance.getBonus(
                player,
                awakeningId
        );
    }

    private static void applyAwakeningTiming(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        boolean changed = false;

        if (vars.awakening != null
                && vars.awakening.active
                && vars.awakening.activeUntilGameTime > 0
                && vars.awakening.activeAwakeningId != null
                && !vars.awakening.activeAwakeningId.isEmpty()) {

            changed =
                    applyNewAwakeningDuration(
                            player,
                            vars
                    ) || changed;
        } else {
            player.getPersistentData()
                    .putString(
                            CONFIGURED_AWAKENING_ID_TAG,
                            ""
                    );
            player.getPersistentData()
                    .putLong(
                            CONFIGURED_AWAKENING_UNTIL_TAG,
                            0
                    );
        }

        changed =
                applyLegacyAwakeningDuration(
                        player,
                        vars
                ) || changed;

        if (changed) {
            vars.syncPlayerVariables(
                    player
            );
        }
    }

    private static boolean applyNewAwakeningDuration(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        String id =
                vars.awakening.activeAwakeningId;

        long currentUntil =
                vars.awakening.activeUntilGameTime;

        String seenId =
                player.getPersistentData()
                        .getString(
                                CONFIGURED_AWAKENING_ID_TAG
                        );

        long seenUntil =
                player.getPersistentData()
                        .getLong(
                                CONFIGURED_AWAKENING_UNTIL_TAG
                        );

        if (id.equals(seenId)
                && currentUntil == seenUntil) {
            return false;
        }

        long now =
                player.level().getGameTime();

        int fallback =
                (int) Math.max(
                        0,
                        currentUntil - now
                );

        int configuredDuration =
                AwakeningConfig.getAwakeningDurationTicks(
                        player,
                        fallback
                );

        long configuredUntil =
                now + Math.max(
                        0,
                        configuredDuration
                );

        vars.awakening.activeUntilGameTime =
                configuredUntil;

        player.getPersistentData()
                .putString(
                        CONFIGURED_AWAKENING_ID_TAG,
                        id
                );

        player.getPersistentData()
                .putLong(
                        CONFIGURED_AWAKENING_UNTIL_TAG,
                        configuredUntil
                );

        return configuredUntil != currentUntil;
    }

    private static boolean applyLegacyAwakeningDuration(
            ServerPlayer player,
            JujutsucraftaddonModVariables.PlayerVariables vars
    ) {

        if (!hasLegacyAwakeningEffect(player)
                && !isLegacyAwakeningTimeActive(
                        player,
                        vars
                )) {
            player.getPersistentData()
                    .putLong(
                            CONFIGURED_LEGACY_AWAKENING_UNTIL_TAG,
                            0
                    );
            return false;
        }

        long now =
                player.level().getGameTime();

        long configuredUntil =
                player.getPersistentData()
                        .getLong(
                                CONFIGURED_LEGACY_AWAKENING_UNTIL_TAG
                        );

        if (configuredUntil > 0
                && now >= configuredUntil) {
            clearLegacyAwakeningEffects(
                    player
            );

            vars.AwakeningActiveUntil = 0;

            player.getPersistentData()
                    .putLong(
                            CONFIGURED_LEGACY_AWAKENING_UNTIL_TAG,
                            0
                    );

            return true;
        }

        if (configuredUntil <= 0) {
            int fallback =
                    (int) Math.max(
                            0,
                            vars.AwakeningActiveUntil - now
                    );

            if (fallback <= 0) {
                fallback =
                        getLongestLegacyAwakeningEffectDuration(
                                player
                        );
            }

            int configuredDuration =
                    AwakeningConfig.getAwakeningDurationTicks(
                            player,
                            fallback
                    );

            configuredUntil =
                    now + Math.max(
                            0,
                            configuredDuration
                    );

            player.getPersistentData()
                    .putLong(
                            CONFIGURED_LEGACY_AWAKENING_UNTIL_TAG,
                            configuredUntil
                    );

            vars.AwakeningActiveUntil =
                    configuredUntil;

            syncLegacyAwakeningEffectDurations(
                    player,
                    configuredUntil,
                    now
            );

            return true;
        }

        if (vars.AwakeningActiveUntil != configuredUntil) {
            vars.AwakeningActiveUntil =
                    configuredUntil;
            syncLegacyAwakeningEffectDurations(
                    player,
                    configuredUntil,
                    now
            );
            return true;
        }

        syncLegacyAwakeningEffectDurations(
                player,
                configuredUntil,
                now
        );

        return false;
    }

    private static void syncLegacyAwakeningEffectDurations(
            ServerPlayer player,
            long configuredUntil,
            long now
    ) {

        int remaining =
                (int) Math.max(
                        0,
                        configuredUntil - now
                );

        syncEffectDuration(
                player,
                JujutsucraftaddonModMobEffects.GOJO_AWAKENING.get(),
                remaining
        );
        syncEffectDuration(
                player,
                JujutsucraftaddonModMobEffects.GOJO_AWAKENING_1.get(),
                remaining
        );
        syncEffectDuration(
                player,
                JujutsucraftaddonModMobEffects.ITADORI_AWAKENING.get(),
                remaining
        );
        syncEffectDuration(
                player,
                JujutsucraftaddonModMobEffects.NAOYA_AWAKENING.get(),
                remaining
        );
        syncEffectDuration(
                player,
                JujutsucraftaddonModMobEffects.SUKUNA_AWAKEN_2.get(),
                remaining
        );
        syncEffectDuration(
                player,
                JujutsucraftaddonModMobEffects.SUKUNA_POWERS.get(),
                remaining
        );
    }

    private static void syncEffectDuration(
            ServerPlayer player,
            MobEffect effect,
            int duration
    ) {

        MobEffectInstance instance =
                player.getEffect(
                        effect
                );

        if (instance == null) {
            return;
        }

        if (duration <= 0) {
            player.removeEffect(
                    effect
            );
            return;
        }

        ((MobEffectInstanceAccessor) instance)
                .jjkfys$setDuration(
                        duration
                );
    }

    private static int getLongestLegacyAwakeningEffectDuration(
            ServerPlayer player
    ) {

        int longest = 0;

        longest =
                Math.max(
                        longest,
                        getEffectDuration(
                                player,
                                JujutsucraftaddonModMobEffects.GOJO_AWAKENING.get()
                        )
                );

        longest =
                Math.max(
                        longest,
                        getEffectDuration(
                                player,
                                JujutsucraftaddonModMobEffects.GOJO_AWAKENING_1.get()
                        )
                );

        longest =
                Math.max(
                        longest,
                        getEffectDuration(
                                player,
                                JujutsucraftaddonModMobEffects.ITADORI_AWAKENING.get()
                        )
                );

        longest =
                Math.max(
                        longest,
                        getEffectDuration(
                                player,
                                JujutsucraftaddonModMobEffects.NAOYA_AWAKENING.get()
                        )
                );

        longest =
                Math.max(
                        longest,
                        getEffectDuration(
                                player,
                                JujutsucraftaddonModMobEffects.SUKUNA_AWAKEN_2.get()
                        )
                );

        longest =
                Math.max(
                        longest,
                        getEffectDuration(
                                player,
                                JujutsucraftaddonModMobEffects.SUKUNA_POWERS.get()
                        )
                );

        return longest;
    }

    private static void clearLegacyAwakeningEffects(
            ServerPlayer player
    ) {

        player.removeEffect(
                JujutsucraftaddonModMobEffects.GOJO_AWAKENING.get()
        );
        player.removeEffect(
                JujutsucraftaddonModMobEffects.GOJO_AWAKENING_1.get()
        );
        player.removeEffect(
                JujutsucraftaddonModMobEffects.ITADORI_AWAKENING.get()
        );
        player.removeEffect(
                JujutsucraftaddonModMobEffects.NAOYA_AWAKENING.get()
        );
        player.removeEffect(
                JujutsucraftaddonModMobEffects.SUKUNA_AWAKEN_2.get()
        );
        player.removeEffect(
                JujutsucraftaddonModMobEffects.SUKUNA_POWERS.get()
        );
    }

    private static boolean hasEffect(
            LivingEntity living,
            MobEffect effect
    ) {

        return effect != null
                && living.hasEffect(effect);
    }
}
