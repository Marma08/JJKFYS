package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.procedures.DomainTypeOnKeyPressedProcedure;

import net.marma.jjkfys.DomainMasteryDisabler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DomainTypeOnKeyPressedProcedure.class, remap = false)
public abstract class DomainMasteryTypeKeyMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void jjkfys$blockDomainMasteryTypeChange(
            Entity entity,
            CallbackInfo ci
    ) {

        if (!DomainMasteryDisabler.isDisabled(
                entity
        )) {
            return;
        }

        DomainMasteryDisabler.forceNormalDomain(
                entity
        );

        if (entity instanceof Player player
                && !player.level()
                        .isClientSide()) {
            player.sendSystemMessage(
                    Component.literal(
                            "Domain mastery is disabled on this server."
                    )
            );
        }

        ci.cancel();
    }
}
