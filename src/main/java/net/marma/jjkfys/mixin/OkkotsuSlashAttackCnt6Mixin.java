package net.marma.jjkfys.mixin;

import net.marma.jjkfys.SlashAttackBlackFlashSupport;
import net.mcreator.jujutsucraft.procedures.RangeAttackProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangeAttackProcedure.class)
public class OkkotsuSlashAttackCnt6Mixin {
    private static final double OKKOTSU_TECHNIQUE = 5.0D;
    private static final String MARKER = "jjkfys_okkotsu_slash_cnt6";

    @Inject(method = "execute", at = @At("HEAD"), remap = false, require = 0)
    private static void jjkfys$prepareOkkotsuSlashCnt6(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        SlashAttackBlackFlashSupport.prepareSlashCnt6(
                world,
                entity,
                OKKOTSU_TECHNIQUE,
                SlashAttackBlackFlashSupport.WeaponRule.OKKOTSU_SWORD,
                MARKER
        );
    }

    @Inject(method = "execute", at = @At("RETURN"), remap = false, require = 0)
    private static void jjkfys$restoreOkkotsuSlashCnt6(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        SlashAttackBlackFlashSupport.restoreSlashCnt6(entity, MARKER);
    }
}
