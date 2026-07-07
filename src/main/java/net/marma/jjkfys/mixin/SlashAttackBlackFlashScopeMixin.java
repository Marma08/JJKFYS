package net.marma.jjkfys.mixin;

import net.marma.jjkfys.BlackFlashEligibilityScope;
import net.mcreator.jujutsucraft.procedures.AISlashProcedure;
import net.mcreator.jujutsucraft.procedures.RangeAttackProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AISlashProcedure.class)
public class SlashAttackBlackFlashScopeMixin {

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/jujutsucraft/procedures/RangeAttackProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    ordinal = 0
            ),
            remap = false,
            require = 0
    )
    private static void jjkfys$markSlashAttackRangeAttack(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        BlackFlashEligibilityScope.pushSlashAttack();
        try {
            RangeAttackProcedure.execute(world, x, y, z, entity);
        } finally {
            BlackFlashEligibilityScope.popSlashAttack();
        }
    }
}
