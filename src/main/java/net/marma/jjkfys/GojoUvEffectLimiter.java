package net.marma.jjkfys;

import net.marma.jjkfys.init.JJKFysGameRules;
import net.mcreator.jujutsucraft.init.JujutsucraftModMobEffects;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.LogicAttackDomainProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = JJKFYS.MODID)
public class GojoUvEffectLimiter {
    private static final String UV_SOURCE_KEY = "jjkfysGojoUvSource";
    private static final String UV_UNTIL_KEY = "jjkfysGojoUvUntil";
    private static final String BRAIN_UNTIL_KEY = "jjkfysGojoBrainUntil";
    private static final String SKILL_SNAPSHOT_KEY = "jjkfysGojoUvSkillSnapshot";
    private static final String HAS_SKILL_SNAPSHOT_KEY = "jjkfysGojoUvHasSkillSnapshot";
    private static final String UV_RELEASED_KEY = "jjkfysGojoUvReleased";
    private static final ThreadLocal<Boolean> UNLIMITED_VOID_PASS = ThreadLocal.withInitial(() -> false);

    public static void beginUnlimitedVoidPass() {
        UNLIMITED_VOID_PASS.set(true);
    }

    public static void endUnlimitedVoidPass() {
        UNLIMITED_VOID_PASS.remove();
    }

    public static boolean shouldTreatAsOutsideUnlimitedVoid(Entity domain, Entity target) {
        return UNLIMITED_VOID_PASS.get() && shouldSkipUnlimitedVoidTarget(domain, target);
    }

    public static boolean shouldSkipUnlimitedVoidTarget(Entity domain, Entity target) {
        if (domain == null || !(target instanceof LivingEntity living)) {
            return false;
        }

        CompoundTag data = living.getPersistentData();
        return domain.getStringUUID().equals(data.getString(UV_SOURCE_KEY))
                && data.contains(UV_UNTIL_KEY)
                && living.level().getGameTime() >= data.getLong(UV_UNTIL_KEY);
    }

    public static void clearClosedDomainSession(LevelAccessor world, Entity domainOwner) {
        if (!(world instanceof ServerLevel level) || domainOwner == null) {
            return;
        }

        String source = domainOwner.getStringUUID();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity living) {
                CompoundTag data = living.getPersistentData();
                if (source.equals(data.getString(UV_SOURCE_KEY))) {
                    releaseUnlimitedVoidSession(living);
                }
            }
        }
    }

    public static void beforeUnlimitedVoid(LevelAccessor world, Entity domain) {
        if (!isUnlimitedVoidTimingLimited(world)) {
            clearUnlimitedVoidTargetsForDomain(world, domain);
            return;
        }

        forEachUnlimitedVoidTarget(world, domain, target -> {
            CompoundTag data = target.getPersistentData();
            String source = domain.getStringUUID();
            if (!source.equals(data.getString(UV_SOURCE_KEY)) || !data.getBoolean(HAS_SKILL_SNAPSHOT_KEY)) {
                data.putDouble(SKILL_SNAPSHOT_KEY, data.getDouble("skill"));
                data.putBoolean(HAS_SKILL_SNAPSHOT_KEY, true);
            }
        });
    }

    public static void markFromUnlimitedVoid(LevelAccessor world, Entity domain) {
        if (!isUnlimitedVoidTimingLimited(world)) {
            clearUnlimitedVoidTargetsForDomain(world, domain);
            return;
        }

        forEachUnlimitedVoidTarget(world, domain, target -> {
            mark(target, domain.getStringUUID(), world);
            enforce(target);
            restoreSkillIfReleased(target, domain.getStringUUID());
        });
    }

    public static void noteUnlimitedVoidEffect(LevelAccessor world, Entity domain, LivingEntity target) {
        if (!isUnlimitedVoidTimingLimited(world) || domain == null || target == null || target == domain) {
            return;
        }

        mark(target, domain.getStringUUID(), world);
        enforce(target);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            enforce(event.player);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target instanceof Player) && target.getPersistentData().contains(UV_UNTIL_KEY)) {
            enforce(target);
        }
    }

    private static void mark(LivingEntity target, String source, LevelAccessor world) {
        int duration = world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_UV_DURATION_TICKS);
        if (duration < 0) {
            return;
        }

        int brainDuration = world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_BRAIN_DAMAGE_DURATION_TICKS);
        long now = target.level().getGameTime();
        CompoundTag data = target.getPersistentData();
        if (domainSessionClosed(target)) {
            releaseUnlimitedVoidSession(target);
        }

        if (!source.equals(data.getString(UV_SOURCE_KEY)) || !data.contains(UV_UNTIL_KEY)) {
            data.putString(UV_SOURCE_KEY, source);
            data.putLong(UV_UNTIL_KEY, now + duration);
            if (brainDuration >= 0) {
                data.putLong(BRAIN_UNTIL_KEY, now + brainDuration);
            } else {
                data.remove(BRAIN_UNTIL_KEY);
            }
            data.putBoolean(UV_RELEASED_KEY, false);
        }
    }

    private static void enforce(Player player) {
        enforce((LivingEntity) player);
    }

    private static void enforce(LivingEntity target) {
        if (target.level().isClientSide()) {
            return;
        }

        CompoundTag data = target.getPersistentData();
        if (!isUnlimitedVoidTimingLimited(target.level())) {
            clearSession(data);
            if (!isBrainDamageTimingLimited(target.level())) {
                data.remove(BRAIN_UNTIL_KEY);
            }
            return;
        }

        if (isBrainDamageTimingLimited(target.level())) {
            enforceBrainDamageDuration(target, data);
        } else {
            data.remove(BRAIN_UNTIL_KEY);
        }

        if (!data.contains(UV_UNTIL_KEY)) {
            return;
        }

        if (domainSessionClosed(target)) {
            releaseUnlimitedVoidSession(target);
            return;
        }

        long now = target.level().getGameTime();
        if (now >= data.getLong(UV_UNTIL_KEY)) {
            String source = data.getString(UV_SOURCE_KEY);
            if (!data.getBoolean(UV_RELEASED_KEY)) {
                restoreSkillIfReleased(target, source);
                releaseUnlimitedVoidLock(target);
                data.putBoolean(UV_RELEASED_KEY, true);
            } else {
                restoreSkillIfReleased(target, source);
            }
        }
    }

    public static boolean isUnlimitedVoidTimingLimited(LevelAccessor world) {
        return world != null
                && world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_UV_DURATION_TICKS) >= 0;
    }

    public static boolean isBrainDamageTimingLimited(LevelAccessor world) {
        return world != null
                && world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_BRAIN_DAMAGE_DURATION_TICKS) >= 0;
    }

    private static void clearUnlimitedVoidTargetsForDomain(LevelAccessor world, Entity domain) {
        if (domain == null) {
            return;
        }

        forEachUnlimitedVoidTarget(world, domain, target -> {
            CompoundTag data = target.getPersistentData();
            if (domain.getStringUUID().equals(data.getString(UV_SOURCE_KEY))) {
                clearSession(data);
            }
        });
    }

    private static boolean domainSessionClosed(LivingEntity target) {
        Level level = target.level();

        CompoundTag data = target.getPersistentData();
        if (!data.contains(UV_SOURCE_KEY)) {
            return true;
        }

        Entity source = findEntity(level, data.getString(UV_SOURCE_KEY));
        if (source == null) {
            return true;
        }
        if (!(source instanceof LivingEntity livingSource)) {
            return !source.isAlive();
        }

        return !livingSource.hasEffect(JujutsucraftModMobEffects.DOMAIN_EXPANSION.get())
                || livingSource.getPersistentData().getBoolean("DomainDefeated")
                || livingSource.getPersistentData().getBoolean("Failed") && !livingSource.getPersistentData().getBoolean("Cover");
    }

    private static Entity findEntity(Level level, String uuidText) {
        try {
            return level instanceof net.minecraft.server.level.ServerLevel serverLevel
                    ? serverLevel.getEntity(UUID.fromString(uuidText))
                    : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static void clearSession(CompoundTag data) {
        data.remove(UV_SOURCE_KEY);
        data.remove(UV_UNTIL_KEY);
        data.remove(SKILL_SNAPSHOT_KEY);
        data.remove(HAS_SKILL_SNAPSHOT_KEY);
        data.remove(UV_RELEASED_KEY);
    }

    private static void releaseUnlimitedVoidSession(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        restoreUnlimitedVoidSkillSnapshot(target);
        releaseUnlimitedVoidLock(target);
        clearSession(data);
    }

    private static void enforceBrainDamageDuration(LivingEntity target, CompoundTag data) {
        if (!data.contains(BRAIN_UNTIL_KEY)) {
            return;
        }

        MobEffect brainDamage = JujutsucraftModMobEffects.BRAIN_DAMAGE.get();
        if (!target.hasEffect(brainDamage)) {
            data.remove(BRAIN_UNTIL_KEY);
            return;
        }

        long now = target.level().getGameTime();
        long remaining = data.getLong(BRAIN_UNTIL_KEY) - now;
        if (remaining <= 0L) {
            target.removeEffect(brainDamage);
            data.remove(BRAIN_UNTIL_KEY);
            return;
        }

        int clamped = remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
        var current = target.getEffect(brainDamage);
        if (current != null && current.getDuration() > clamped) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    brainDamage,
                    clamped,
                    current.getAmplifier(),
                    false,
                    false
            ));
        }
    }

    private static Vec3 domainCenter(Entity domain) {
        CompoundTag data = domain.getPersistentData();
        return new Vec3(
                data.getDouble("x_pos_doma") != 0.0D ? data.getDouble("x_pos_doma") : domain.getX(),
                data.getDouble("y_pos_doma") != 0.0D ? data.getDouble("y_pos_doma") : domain.getY(),
                data.getDouble("z_pos_doma") != 0.0D ? data.getDouble("z_pos_doma") : domain.getZ()
        );
    }

    private static boolean hasUnlimitedVoidEffect(LivingEntity target) {
        return target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                || target.hasEffect(MobEffects.CONFUSION)
                || target.hasEffect(MobEffects.DIG_SLOWDOWN)
                || target.hasEffect(MobEffects.BLINDNESS)
                || target.hasEffect(MobEffects.WEAKNESS);
    }

    private static void forEachUnlimitedVoidTarget(LevelAccessor world, Entity domain, java.util.function.Consumer<LivingEntity> action) {
        if (!(world instanceof Level level) || domain == null || level.isClientSide()) {
            return;
        }

        double radius = Math.max(1.0D, JujutsucraftModVariables.MapVariables.get(world).DomainExpansionRadius);
        Vec3 center = domainCenter(domain);
        AABB area = new AABB(center, center).inflate(Math.max(radius, 64.0D));

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == domain) {
                continue;
            }
            if (LogicAttackDomainProcedure.execute(world, domain, target) || hasUnlimitedVoidEffect(target)) {
                action.accept(target);
            }
        }
    }

    private static boolean uvExpiredForSource(LivingEntity target, String source) {
        CompoundTag data = target.getPersistentData();
        return source.equals(data.getString(UV_SOURCE_KEY))
                && data.contains(UV_UNTIL_KEY)
                && !domainSessionClosed(target)
                && target.level().getGameTime() >= data.getLong(UV_UNTIL_KEY);
    }

    public static boolean shouldSuppressUnlimitedVoidSkillWrite(CompoundTag data, String key, double value) {
        return data.getBoolean(UV_RELEASED_KEY) && "skill".equals(key) && value == -999.0D;
    }

    public static boolean shouldSuppressUnlimitedVoidEffect(LivingEntity target, MobEffectInstance effect) {
        if (!target.getPersistentData().getBoolean(UV_RELEASED_KEY)) {
            return false;
        }

        MobEffect mobEffect = effect.getEffect();
        return isUnlimitedVoidMovementEffect(mobEffect)
                || mobEffect == JujutsucraftModMobEffects.BRAIN_DAMAGE.get()
                || target instanceof Player && mobEffect == JujutsucraftModMobEffects.CURSED_TECHNIQUE.get()
                || mobEffect == JujutsucraftModMobEffects.COOLDOWN_TIME.get()
                || mobEffect == JujutsucraftModMobEffects.COOLDOWN_TIME_COMBAT.get();
    }

    public static boolean shouldSuppressUnlimitedVoidEffectRemoval(LivingEntity target, MobEffect effect) {
        return target.getPersistentData().getBoolean(UV_RELEASED_KEY)
                && effect == JujutsucraftModMobEffects.CURSED_TECHNIQUE.get();
    }

    public static boolean shouldIgnoreNeutralizationForCombat(LivingEntity target, MobEffect effect) {
        CompoundTag data = target.getPersistentData();
        return effect == JujutsucraftModMobEffects.NEUTRALIZATION.get()
                && data.getBoolean(UV_RELEASED_KEY)
                && data.contains(UV_UNTIL_KEY)
                && !domainSessionClosed(target)
                && target.level().getGameTime() >= data.getLong(UV_UNTIL_KEY);
    }

    private static void restoreSkillIfReleased(LivingEntity target, String source) {
        CompoundTag data = target.getPersistentData();
        if (!uvExpiredForSource(target, source) || !data.getBoolean(HAS_SKILL_SNAPSHOT_KEY)) {
            return;
        }

        restoreUnlimitedVoidSkillSnapshot(target);
    }

    private static void restoreUnlimitedVoidSkillSnapshot(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        if (data.getDouble("skill") != -999.0D) {
            return;
        }

        data.putDouble("skill", data.getBoolean(HAS_SKILL_SNAPSHOT_KEY) ? data.getDouble(SKILL_SNAPSHOT_KEY) : 0.0D);
    }

    private static void releaseUnlimitedVoidLock(LivingEntity target) {
        removeUnlimitedVoidMovementEffects(target);
        removeIfPresent(target, JujutsucraftModMobEffects.CURSED_TECHNIQUE.get());
        if (target instanceof Player) {
            resetTechniqueSwitchLock((Player) target);
        }
        removeIfPresent(target, JujutsucraftModMobEffects.COOLDOWN_TIME.get());
        removeIfPresent(target, JujutsucraftModMobEffects.COOLDOWN_TIME_COMBAT.get());
        if (target instanceof Player player) {
            player.getCooldowns().removeCooldown(Items.ENDER_PEARL);
            clearInventoryCooldowns(player);
        }
    }

    private static void resetTechniqueSwitchLock(Player player) {
        player.getCapability(JujutsucraftModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            if (variables.noChangeTechnique) {
                variables.noChangeTechnique = false;
                variables.syncPlayerVariables(player);
            }
        });
    }

    private static void clearInventoryCooldowns(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                player.getCooldowns().removeCooldown(stack.getItem());
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty()) {
                player.getCooldowns().removeCooldown(stack.getItem());
            }
        }
    }

    private static void removeUnlimitedVoidMovementEffects(LivingEntity target) {
        removeIfPresent(target, MobEffects.MOVEMENT_SLOWDOWN);
        removeIfPresent(target, MobEffects.CONFUSION);
        removeIfPresent(target, MobEffects.DIG_SLOWDOWN);
        removeIfPresent(target, MobEffects.BLINDNESS);
        removeIfPresent(target, MobEffects.WEAKNESS);
        // Neutralization is the domain sure-hit/infinity bypass, not part of UV stun timing.
    }

    private static boolean isUnlimitedVoidMovementEffect(MobEffect effect) {
        return effect == MobEffects.MOVEMENT_SLOWDOWN
                || effect == MobEffects.CONFUSION
                || effect == MobEffects.DIG_SLOWDOWN
                || effect == MobEffects.BLINDNESS
                || effect == MobEffects.WEAKNESS;
    }

    private static void removeIfPresent(LivingEntity target, MobEffect effect) {
        if (target.hasEffect(effect)) {
            target.removeEffect(effect);
        }
    }
}
