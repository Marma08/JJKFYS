package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.util.RebirthManager;

import net.marma.jjkfys.init.JJKFysGameRules;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraftforge.server.ServerLifecycleHooks;

@Mixin(RebirthManager.class)
public class RebirthMaxLevelMixin {

    @Inject(
            method = "getMaxRebirthLevel",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void jjkfys$useGameRule(
            CallbackInfoReturnable<Integer> cir
    ) {

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) return;

        int max = server.getGameRules()
                .getInt(JJKFysGameRules.MAX_REBIRTHS);

        cir.setReturnValue(max);
    }
}