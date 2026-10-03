package dev.yuni.ashenprotocol.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class APConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue PISTOL_DAMAGE;
    public static final ForgeConfigSpec.IntValue PISTOL_ENTROPY;
    public static final ForgeConfigSpec.IntValue RELAY_INTEGRITY_GAIN;
    public static final ForgeConfigSpec.IntValue RELAY_ENTROPY_REDUCTION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("ashen_protocol");

        PISTOL_DAMAGE = builder
                .comment("Base damage dealt by the Phase Pistol.")
                .defineInRange("phasePistolDamage", 8, 1, 100);

        PISTOL_ENTROPY = builder
                .comment("Global entropy debt added per Phase Pistol shot.")
                .defineInRange("phasePistolEntropy", 2, 0, 100);

        RELAY_INTEGRITY_GAIN = builder
                .comment("Integrity granted by an active relay pulse.")
                .defineInRange("relayIntegrityGain", 1, 0, 100);

        RELAY_ENTROPY_REDUCTION = builder
                .comment("Entropy removed by an active relay pulse.")
                .defineInRange("relayEntropyReduction", 1, 0, 100);

        builder.pop();

        SPEC = builder.build();
    }

    private APConfig() {}
}
