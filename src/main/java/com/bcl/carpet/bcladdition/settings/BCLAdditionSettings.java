package com.bcl.carpet.bcladdition.settings;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.*;

public class BCLAdditionSettings {

    public static final String BCL = "BCL";

    @Rule(
            categories = {BCL, SURVIVAL, FEATURE}
    )
    public static boolean mineableEndPortalFrame = false;
}
