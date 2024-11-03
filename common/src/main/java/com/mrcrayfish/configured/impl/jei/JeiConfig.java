package com.mrcrayfish.configured.impl.jei;

import com.mrcrayfish.configured.api.ConfigType;
import com.mrcrayfish.configured.api.ExecutionContext;
import com.mrcrayfish.configured.api.IConfigEntry;
import com.mrcrayfish.configured.api.IModConfig;
import com.mrcrayfish.configured.api.ActionResult;
import com.mrcrayfish.configured.util.ConfigHelper;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class JeiConfig implements IModConfig
{
    private final String name;
    private final ConfigType type;
    private final List<? extends IJeiConfigCategory> categories;
    private final IJeiConfigFile configFile;

    public JeiConfig(String name, ConfigType type, IJeiConfigFile configFile)
    {
        this.name = name;
        this.type = type;
        this.categories = configFile.getCategories();
        this.configFile = configFile;
    }

    @Override
    public ConfigType getType()
    {
        return this.type;
    }

    @Override
    public String getFileName()
    {
        return this.configFile.getPath().getFileName().toString();
    }

    @Override
    public String getModId()
    {
        return "jei";
    }

    @Override
    public IConfigEntry createRootEntry()
    {
        return new JeiCategoryListEntry(this.name, this.categories);
    }

    @Override
    public ActionResult update(IConfigEntry entry)
    {
        ConfigHelper.getChangedValues(entry)
            .stream()
            .filter(JeiValue.class::isInstance)
            .map(JeiValue.class::cast)
            .forEach(JeiValue::updateConfigValue);
        return ActionResult.success();
    }

    @Override
    public ActionResult canPlayerEdit(Player player)
    {
        ExecutionContext context = new ExecutionContext(player);
        if(context.isClient())
        {
            if(context.isMainMenu() || context.isLocalPlayer())
            {
                return ActionResult.success();
            }
        }
        return ActionResult.fail();
    }
}
