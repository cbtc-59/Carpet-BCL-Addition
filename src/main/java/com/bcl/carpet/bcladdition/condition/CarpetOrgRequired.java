package com.bcl.carpet.bcladdition.condition;

import carpet.api.settings.Rule;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

/**
 * 仅在 Carpet-Org-Addition 已安装且版本 >= 1.41.5 时才注册规则。
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
