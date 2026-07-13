package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModItems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class HigurumaExecutionerSwordHandGuardMixin {

    @Inject(
            method = "setItemInHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jjkfys$preventExecutionerSwordReplacement(
            InteractionHand hand,
            ItemStack replacement,
            CallbackInfo ci
    ) {

        LivingEntity self =
                (LivingEntity) (Object) this;

        ItemStack current =
                self.getItemInHand(
                        hand
                );

        if (isExecutionerSword(current)
                && isUnsafeReplacement(replacement)) {
            ci.cancel();
        }
    }

    private static boolean isExecutionerSword(
            ItemStack stack
    ) {

        return !stack.isEmpty()
                && stack.getItem() == JujutsucraftModItems.EXECUTIONERS_SWORD.get();
    }

    private static boolean isUnsafeReplacement(
            ItemStack stack
    ) {

        if (stack.isEmpty()) {
            return true;
        }

        return stack.getItem() == JujutsucraftModItems.GAVEL.get()
                || stack.getItem() == JujutsucraftModItems.GAVEL_LONG.get()
                || stack.getItem() == JujutsucraftModItems.GAVEL_BIG.get();
    }
}
