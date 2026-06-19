package com.bcl.carpet.bcladdition.settings;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.*;

public class BCLAdditionSettings {

    public static final String BCL = "BCL";

    // === 核心规则 ===

    @Rule(
            categories = {BCL, SURVIVAL, FEATURE}
    )
    public static boolean mineableEndPortalFrame = false;

    // === 特性类 ===

    @Rule(
            categories = {BCL, SURVIVAL}
    )
    public static boolean disableWindChargeEffect = false;

    @Rule(
            categories = {BCL, FEATURE},
            options = {"false", "ignore_weather", "ignore_weather_and_sky"},
            strict = true
    )
    public static String channelingIgnoreConditions = "false";

    @Rule(
            categories = {BCL, FEATURE}
    )
    public static boolean riptideIgnoreConditions = false;

    @Rule(
            categories = {BCL, SURVIVAL},
            options = {"false", "shulker_box", "any"},
            strict = true
    )
    public static String forceOpenContainer = "false";

    // === 和平类 ===

    @Rule(
            categories = {BCL, SURVIVAL}
    )
    public static boolean truePeacefulMode = false;

    // === 假玩家类 ===

    @Rule(
            categories = {BCL, SURVIVAL}
    )
    public static boolean fakePlayerAutoRestock = false;

    // === 命令类 ===

    @Rule(
            categories = {BCL, COMMAND}
    )
    public static boolean playerCommandCloseScreen = false;
}
