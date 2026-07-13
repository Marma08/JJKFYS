package net.marma.jjkfys.mixin;

import net.marma.jjkfys.SlashAttackBlackFlashSupport;
import net.mcreator.jujutsucraft.procedures.RangeAttackProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RangeAttackProcedure.class)
public class MeiMeiSlashAttackBlackFlashMixin {
    private static final double MEI_MEI_TECHNIQUE = 11.0D;

    @ModifyConstant(
            method = "execute",
            constant = @Constant(doubleValue = 0.998D),
            remap = false,
            require = 0
    )
    private static double jjkfys$useMeiMeiOwnerBlackFlashChanceForSlash(
            double constant,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        return SlashAttackBlackFlashSupport.applyOwnerChance(
                constant,
                world,
                entity,
                MEI_MEI_TECHNIQUE,
                SlashAttackBlackFlashSupport.WeaponRule.MEI_MEI_AXE
        );
    }
}
