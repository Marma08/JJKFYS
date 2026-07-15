package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;
import com.jujutsu.jujutsucraftaddon.network.NewTabButtonMessage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NewTabButtonMessage.class, remap = false)
public abstract class ClanSlotSaveGuardMixin {

    @Inject(
            method = "handleButtonAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void jjkfys$keepLegacyClanSlotRules(
            Player entity,
            int buttonID,
            int x,
            int y,
            int z,
            CallbackInfo ci
    ) {

        if (entity == null
                || buttonID < 60
                || buttonID > 62
                || entity.level().isClientSide()) {
            return;
        }

        String clan = entity.getCapability(
                JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY,
                null
        ).map(vars -> vars.Clans).orElse("");

        if (!isLegacyBlockedClan(
                clan
        )) {
            return;
        }

        entity.displayClientMessage(
                Component.literal(
                        "You can't store a legendary clan, just roll it"
                ),
                false
        );
        ci.cancel();
    }

    private static boolean isLegacyBlockedClan(
            String clan
    ) {

        return "Itadori".equals(clan)
                || "Kenjaku".equals(clan)
                || "Gojo".equals(clan)
                || "Rejected Zenin".equals(clan)
                || "Sukuna".equals(clan)
                || "Okkotsu".equals(clan);
    }
}
