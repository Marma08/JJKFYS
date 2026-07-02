package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModItems;
import net.mcreator.jujutsucraft.procedures.GavelRightClicked2Procedure;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GavelRightClicked2Procedure.class, remap = false)
public class HigurumaExecutionerSwordGavelCycleMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void jjkfys$keepExecutionerSwordOutOfGavelCycle(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        if (isExecutionerSword(living.getMainHandItem())
                || isExecutionerSword(living.getOffhandItem())) {
            ci.cancel();
        }
    }

    private static boolean isExecutionerSword(
            ItemStack stack
    ) {

        return !stack.isEmpty()
                && stack.getItem() == JujutsucraftModItems.EXECUTIONERS_SWORD.get();
    }
}
