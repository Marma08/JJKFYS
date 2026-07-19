package net.marma.jjkfys.init;

import net.minecraft.world.level.GameRules;

public class JJKFysGameRules {

    public static GameRules.Key<GameRules.IntegerValue> MAX_REBIRTHS;
    public static GameRules.Key<GameRules.IntegerValue> GOJO_UV_DURATION_TICKS;
    public static GameRules.Key<GameRules.IntegerValue> GOJO_BRAIN_DAMAGE_DURATION_TICKS;
    public static GameRules.Key<GameRules.BooleanValue> DISABLE_DOMAIN_MASTERY;
    public static GameRules.Key<GameRules.IntegerValue> YOROZU_TRUE_SPHERE_ATTACK_CHARGE;
    public static GameRules.Key<GameRules.BooleanValue> PREVENT_WORLD_CUT_RESISTANCE_REMOVAL;

    public static void register() {

        MAX_REBIRTHS = GameRules.register(
                "jjkfysMaxRebirths",
                GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(3)
        );

        GOJO_UV_DURATION_TICKS = GameRules.register(
                "jjkfysGojoUvDurationTicks",
                GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(-1)
        );

        GOJO_BRAIN_DAMAGE_DURATION_TICKS = GameRules.register(
                "jjkfysGojoBrainDamageDurationTicks",
                GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(-1)
        );

        DISABLE_DOMAIN_MASTERY = GameRules.register(
                "jjkfysDisableDomainMastery",
                GameRules.Category.PLAYER,
                GameRules.BooleanValue.create(false)
        );

        YOROZU_TRUE_SPHERE_ATTACK_CHARGE = GameRules.register(
                "jjkfysYorozuTrueSphereAttackCharge",
                GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(188)
        );

        PREVENT_WORLD_CUT_RESISTANCE_REMOVAL = GameRules.register(
                "jjkfysPreventWorldCutResistanceRemoval",
                GameRules.Category.PLAYER,
                GameRules.BooleanValue.create(true)
        );
    }
}
