package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.network.SkillTreeSPButtonMessage;

import net.minecraftforge.network.NetworkEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(SkillTreeSPButtonMessage.class)
public class SkillTreeSPButtonFixMixin {

    @Inject(method = "handler", at = @At("HEAD"), cancellable = true, remap = false)
    private static void fixHandler(SkillTreeSPButtonMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {

        ci.cancel();

        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {

            Player entity = context.getSender();
            if (entity == null) return;

            int buttonID = ((SkillTreeMessageAccessor) message).getButtonID();
            int x = ((SkillTreeMessageAccessor) message).getX();
            int y = ((SkillTreeMessageAccessor) message).getY();
            int z = ((SkillTreeMessageAccessor) message).getZ();

            SkillTreeSPButtonMessage.handleButtonAction(entity, buttonID, x, y, z);
        });

        context.setPacketHandled(true);
    }
}