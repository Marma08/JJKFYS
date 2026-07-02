package net.marma.jjkfys.mixin;

import net.minecraft.world.effect.MobEffectInstance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {

    @Accessor("duration")
    void jjkfys$setDuration(int duration);
}
