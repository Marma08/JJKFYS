package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.network.SkillTreeSPButtonMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkillTreeSPButtonMessage.class)
public interface SkillTreeMessageAccessor {

    @Accessor(value = "buttonID", remap = false)
    int getButtonID();

    @Accessor(value = "x", remap = false)
    int getX();

    @Accessor(value = "y", remap = false)
    int getY();

    @Accessor(value = "z", remap = false)
    int getZ();
}