package net.marma.jjkfys.mixin;

import net.marma.jjkfys.AwakeningOutputLimiter;

import net.mcreator.jujutsucraft.procedures.AIBlueProcedure;
import net.mcreator.jujutsucraft.procedures.AIPurpleProcedure;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
        AIBlueProcedure.class,
        AIPurpleProcedure.class
})
public abstract class AwakeningOutputAttackEntityMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            remap = false
    )
    private static void jjkfys$capAwakeningOutputEntity(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        if (entity == null
                || !(world instanceof Level level)
                || level.isClientSide()) {
            return;
        }

        AwakeningOutputLimiter.capAttackEntity(
                entity,
                level
        );
    }
}
