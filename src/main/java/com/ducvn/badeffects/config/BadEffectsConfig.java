package com.ducvn.badeffects.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BadEffectsConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> heatstroke;
    public static final ForgeConfigSpec.ConfigValue<Boolean> broke_leg;
    public static final ForgeConfigSpec.ConfigValue<Boolean> lack_of_oxygen;
    public static final ForgeConfigSpec.ConfigValue<Boolean> nausea_drowning;
    public static final ForgeConfigSpec.ConfigValue<Boolean> frostbite;
    public static final ForgeConfigSpec.ConfigValue<Boolean> food_poisoning;
    public static final ForgeConfigSpec.ConfigValue<Boolean> stuck;
    public static final ForgeConfigSpec.ConfigValue<Boolean> stun;
    public static final ForgeConfigSpec.ConfigValue<Boolean> karma;
    public static final ForgeConfigSpec.ConfigValue<Boolean> vision_disrupt;
    public static final ForgeConfigSpec.ConfigValue<Boolean> broken_hand;
    public static final ForgeConfigSpec.ConfigValue<Boolean> infection;
    public static final ForgeConfigSpec.ConfigValue<Boolean> head_bump;
    public static final ForgeConfigSpec.ConfigValue<Boolean> exhausted;

    static {
        BUILDER.push("Bad Effects Config");

        heatstroke = BUILDER.define("Disable Heatstroke: ", false);
        broke_leg = BUILDER.define("Disable Broken Leg: ", false);
        lack_of_oxygen = BUILDER.define("Disable Lack of Oxygen: ", false);
        nausea_drowning = BUILDER.define("Disable Nausea Drowning: ", false);
        frostbite = BUILDER.define("Disable Frostbite: ", false);
        food_poisoning = BUILDER.define("Disable Food Poisoning: ", false);
        stuck = BUILDER.define("Disable Stuck: ", false);
        stun = BUILDER.define("Disable Stun: ", false);
        karma = BUILDER.define("Disable Karma: ", false);
        vision_disrupt = BUILDER.define("Disable Vision Disrupt: ", false);
        broken_hand = BUILDER.define("Disable Broken Hand: ", false);
        infection = BUILDER.define("Disable Infection: ", false);
        head_bump = BUILDER.define("Disable Head Bump: ", false);
        exhausted = BUILDER.define("Disable Exhausted: ", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
