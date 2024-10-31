package com.mrcrayfish.configured.impl.framework;

import com.mrcrayfish.framework.api.config.ConfigType;
import com.mrcrayfish.framework.config.FrameworkConfigManager;

/**
 * Author: MrCrayfish
 */
public class FrameworkConfigHelper
{
    public static boolean isWorldType(FrameworkConfigManager.FrameworkConfigImpl config)
    {
        return config.getType() == ConfigType.WORLD || config.getType() == ConfigType.WORLD_SYNC;
    }
}
