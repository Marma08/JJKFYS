package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.procedures.BFMasteryProcedure;
import com.jujutsu.jujutsucraftaddon.procedures.BlackFlashNerfedProcedure;
import com.jujutsu.jujutsucraftaddon.procedures.BlackFlashedProcedure;
import com.jujutsu.jujutsucraftaddon.procedures.ItadoriClan2Procedure;
import net.marma.jjkfys.SlashAttackBlackFlashSupport;
import net.mcreator.jujutsucraft.procedures.RangeAttackProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangeAttackProcedure.class)
public class SlashAttackOwnerBlackFlashMixin {
    @Inject(method = "execute", at = @At("RETURN"), remap = false, require = 0)
    private static void jjkfys$applySlashBlackFlashToOwner(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        if (!SlashAttackBlackFlashSupport.consumeOwnerBlackFlashMarker(entity)) {
            return;
        }

        Entity owner = SlashAttackBlackFlashSupport.slashOwner(world, entity);
        if (owner == null) {
            return;
        }

        BlackFlashedProcedure.execute(world, x, y, z, owner);
        BFMasteryProcedure.execute(world, owner);
        ItadoriClan2Procedure.execute(world, owner);
        BlackFlashNerfedProcedure.execute(world, owner);
        owner.getPersistentData().putDouble("cnt_bf", 0.0D);
    }
}
