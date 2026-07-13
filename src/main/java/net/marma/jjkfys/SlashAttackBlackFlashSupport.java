package net.marma.jjkfys;

import com.jujutsu.jujutsucraftaddon.init.JujutsucraftaddonModGameRules;
import com.jujutsu.jujutsucraftaddon.init.JujutsucraftaddonModMobEffects;
import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;
import net.mcreator.jujutsucraft.entity.EntitySlashEntity;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.GetEntityFromUUIDProcedure;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;

public final class SlashAttackBlackFlashSupport {
    private static final double BLACK_FLASH_DAMAGE_FLOOR = 9.0D;
    private static final double SLASH_DAMAGE_GATE_FLOOR = 8.0D;
    private static final double BLACK_FLASH_FORCE_THRESHOLD = 0.001D;
    private static final double BLACK_FLASH_DISABLED_THRESHOLD = 1.0D;
    private static final String OWNER_BLACK_FLASH_MARKER = "jjkfys_slash_owner_black_flash";
    private static final String JJC_OKKOTSU_SWORD = "jujutsucraft:sword_okkotsu_yuta";
    private static final String JJKU_OKKOTSU_SWORD = "jujutsucraftaddon:sword_okkotsu";
    private static final String JJKU_OKKOTSU_SWORD_TWO = "jujutsucraftaddon:sword_okkotsu_two";
    private static final String JJKU_OKKOTSU_SWORD_THREE = "jujutsucraftaddon:sword_okkotsu_three";
    private static final String MEI_MEI_AXE = "jujutsucraft:mei_mei_axe";
    private static final String MAHITO_TRANSFIGURED_ARM = "jujutsucraft:mahito_hand_1";

    private SlashAttackBlackFlashSupport() {
    }

    public enum WeaponRule {
        OKKOTSU_SWORD,
        MEI_MEI_AXE,
        MAHITO_TRANSFIGURED_ARM
    }

    public static double applyOwnerChance(
            double constant,
            LevelAccessor world,
            Entity slash,
            double curseTechnique,
            WeaponRule weaponRule
    ) {

        if (constant == BLACK_FLASH_FORCE_THRESHOLD || world == null) {
            return constant;
        }

        Entity owner = actorOf(world, slash);
        if (!hasEligibleOwner(owner, curseTechnique, weaponRule)) {
            return constant;
        }

        if (!rollOwnerBlackFlash(world, owner)) {
            return BLACK_FLASH_DISABLED_THRESHOLD;
        }

        if (slash instanceof EntitySlashEntity) {
            slash.getPersistentData().putBoolean(OWNER_BLACK_FLASH_MARKER, true);
        }
        return BLACK_FLASH_FORCE_THRESHOLD;
    }

    public static boolean hasEligibleSlashOwner(
            LevelAccessor world,
            Entity slash,
            double curseTechnique,
            WeaponRule weaponRule
    ) {

        if (world == null || !isSlashAttackCarrier(slash)) {
            return false;
        }

        return hasEligibleOwner(actorOf(world, slash), curseTechnique, weaponRule);
    }

    public static void prepareSlashCnt6(
            LevelAccessor world,
            Entity slash,
            double curseTechnique,
            WeaponRule weaponRule,
            String marker
    ) {

        if (!hasEligibleSlashOwner(world, slash, curseTechnique, weaponRule)) {
            return;
        }

        double cnt6 = slash.getPersistentData().getDouble("cnt6");
        if (cnt6 >= 0.0D || slash.getPersistentData().getBoolean(marker)) {
            return;
        }

        slash.getPersistentData().putBoolean(marker, true);
        slash.getPersistentData().putDouble(marker + "_old_cnt6", cnt6);
        slash.getPersistentData().putDouble("cnt6", 0.0D);
    }

    public static void restoreSlashCnt6(Entity slash, String marker) {
        if (slash == null || !slash.getPersistentData().getBoolean(marker)) {
            return;
        }

        slash.getPersistentData().putDouble("cnt6", slash.getPersistentData().getDouble(marker + "_old_cnt6"));
        slash.getPersistentData().remove(marker);
        slash.getPersistentData().remove(marker + "_old_cnt6");
    }

    public static double slashDamageGateThreshold(
            double threshold,
            LevelAccessor world,
            Entity slash,
            double curseTechnique,
            WeaponRule weaponRule
    ) {

        if (threshold != BLACK_FLASH_DAMAGE_FLOOR) {
            return threshold;
        }

        if (!hasEligibleSlashOwner(world, slash, curseTechnique, weaponRule)) {
            return threshold;
        }

        double damage = slash.getPersistentData().getDouble("Damage");
        return damage >= SLASH_DAMAGE_GATE_FLOOR ? SLASH_DAMAGE_GATE_FLOOR : threshold;
    }

    public static boolean consumeOwnerBlackFlashMarker(Entity slash) {
        if (slash == null || !slash.getPersistentData().getBoolean(OWNER_BLACK_FLASH_MARKER)) {
            return false;
        }

        slash.getPersistentData().remove(OWNER_BLACK_FLASH_MARKER);
        return true;
    }

    public static Entity slashOwner(LevelAccessor world, Entity slash) {
        if (world == null || !(slash instanceof EntitySlashEntity)) {
            return null;
        }

        return ownerOf(world, slash);
    }

    private static boolean isSlashAttackCarrier(Entity entity) {
        if (entity instanceof EntitySlashEntity) {
            return true;
        }

        if (entity == null) {
            return false;
        }

        return entity.getPersistentData().getBoolean("attack")
                && entity.getPersistentData().getDouble("projectile_type") == 1.0D
                && entity.getPersistentData().getDouble("effect") == 5.0D;
    }

    private static Entity actorOf(LevelAccessor world, Entity entity) {
        if (entity instanceof EntitySlashEntity) {
            return ownerOf(world, entity);
        }

        return isSlashAttackCarrier(entity) ? entity : null;
    }

    private static Entity ownerOf(LevelAccessor world, Entity slash) {
        String ownerUuid = slash.getPersistentData().getString("OWNER_UUID");
        if (ownerUuid.isEmpty()) {
            return null;
        }

        return GetEntityFromUUIDProcedure.execute(world, ownerUuid);
    }

    private static boolean hasTechnique(Entity entity, double curseTechnique) {
        if (entity == null) {
            return false;
        }

        JujutsucraftModVariables.PlayerVariables vars =
                entity.getCapability(
                        JujutsucraftModVariables.PLAYER_VARIABLES_CAPABILITY,
                        null
                ).orElse(new JujutsucraftModVariables.PlayerVariables());

        return vars.PlayerCurseTechnique == curseTechnique
                || vars.PlayerCurseTechnique2 == curseTechnique;
    }

    private static boolean hasEligibleOwner(Entity owner, double curseTechnique, WeaponRule weaponRule) {
        return hasTechnique(owner, curseTechnique) && matchesWeapon(owner, weaponRule);
    }

    private static boolean matchesWeapon(Entity entity, WeaponRule weaponRule) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        return switch (weaponRule) {
            case OKKOTSU_SWORD -> hasOkkotsuSword(living);
            case MEI_MEI_AXE -> hasItem(living, MEI_MEI_AXE);
            case MAHITO_TRANSFIGURED_ARM -> hasItem(living, MAHITO_TRANSFIGURED_ARM);
        };
    }

    private static boolean hasOkkotsuSword(LivingEntity living) {
        return hasItem(living, JJC_OKKOTSU_SWORD)
                || hasItem(living, JJKU_OKKOTSU_SWORD)
                || hasItem(living, JJKU_OKKOTSU_SWORD_TWO)
                || hasItem(living, JJKU_OKKOTSU_SWORD_THREE);
    }

    private static boolean hasItem(LivingEntity living, String registryId) {
        return hasItem(living.getMainHandItem(), registryId) || hasItem(living.getOffhandItem(), registryId);
    }

    private static boolean hasItem(ItemStack stack, String registryId) {
        if (stack.isEmpty()) {
            return false;
        }

        return registryId.equals(String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem())));
    }

    private static boolean rollOwnerBlackFlash(LevelAccessor world, Entity owner) {
        if (hasBlackFlashFatigue(owner)) {
            return false;
        }

        JujutsucraftaddonModVariables.PlayerVariables vars =
                owner.getCapability(
                        JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY,
                        null
                ).orElse(new JujutsucraftaddonModVariables.PlayerVariables());

        float chance = (float) vars.BFChance;
        if (vars.TraitVesselOne) {
            chance = Math.max(chance, 500.0F);
        }

        if (Math.random() >= chance / 1000.0D) {
            return false;
        }

        boolean reworked =
                world.getLevelData().getGameRules().getBoolean(JujutsucraftaddonModGameRules.JJKU_BLACK_FLASH_REWORKED);
        return !reworked || owner.getPersistentData().getDouble("cnt_bf") >= 50.0D;
    }

    private static boolean hasBlackFlashFatigue(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        MobEffect fatigue = JujutsucraftaddonModMobEffects.FATIGUE_BLACK_FLASH.get();
        return living.hasEffect(fatigue);
    }
}
