package net.marma.jjkfys.mixin;

import net.marma.jjkfys.SlashAttackBlackFlashSupport;
import net.mcreator.jujutsucraft.procedures.RangeAttackProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RangeAttackProcedure.class)
public class MeiMeiSlashAttackDamageGateMixin {
    private static final double MEI_MEI_TECHNIQUE = 11.0D;

    @ModifyConstant(
            method = "execute",
            constant = @Constant(doubleValue = 9.0D, ordinal = 0),
            remap = false,
            require = 0
    )
    private static double jjkfys$allowMeiMeiSlashDamageThroughBlackFlashGate(
            double threshold,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        return SlashAttackBlackFlashSupport.slashDamageGateThreshold(
                threshold,
                world,
                entity,
                MEI_MEI_TECHNIQUE,
                SlashAttackBlackFlashSupport.WeaponRule.MEI_MEI_AXE
        );
    }
}
