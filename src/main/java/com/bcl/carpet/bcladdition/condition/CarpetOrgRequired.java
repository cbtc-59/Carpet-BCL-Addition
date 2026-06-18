package com.bcl.carpet.bcladdition.condition;

import carpet.api.settings.Rule;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

/**
 * Only registers a rule when Carpet-Org-Addition is installed
 * and its version is at least 1.41.5.
 */
public class CarpetOrgRequired implements Rule.Condition {

    @Override
    public boolean shouldRegister() {
        return FabricLoader.getInstance()
                .getModContainer("carpet-org-addition")
                .map(container -> {
                    try {
                        Version version = container.getMetadata().getVersion();
                        Version required = Version.parse("1.41.5");
                        return version.compareTo(required) >= 0;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }
}
