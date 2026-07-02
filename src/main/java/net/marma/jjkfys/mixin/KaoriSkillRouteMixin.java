package net.marma.jjkfys.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.mcreator.jujutsucrafts.procedures.KaoriSkillProcedure", remap = false)
public class KaoriSkillRouteMixin {

    private static final int KAORI_DOMAIN_SKILL = 20;
    private static final int KAORI_SKILL_BASE = 4100;

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void jjkfys$keepBaseKaoriDomain(
            LevelAccessor world,
            double x,
            double y,
            double z,
            LivingEntity entity,
            int skill,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (skill % KAORI_SKILL_BASE == KAORI_DOMAIN_SKILL) {
            cir.setReturnValue(false);
        }
    }
}
