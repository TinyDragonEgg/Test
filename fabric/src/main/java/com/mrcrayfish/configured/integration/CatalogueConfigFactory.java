package com.mrcrayfish.configured.integration;

import com.mrcrayfish.configured.Constants;
import com.mrcrayfish.configured.api.ConfigType;
import com.mrcrayfish.configured.api.IModConfig;
import com.mrcrayfish.configured.api.ModContext;
import com.mrcrayfish.configured.api.util.ConfigScreenHelper;
import com.mrcrayfish.configured.client.ClientHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Provides a config screen factory and provider to Catalogue (Fabric)
 *
 * Author: MrCrayfish
 */
public final class CatalogueConfigFactory
{
    // Do not change signature
    public static Map<String, BiFunction<Screen, ModContainer, Screen>> createConfigProvider()
    {
        Map<String, BiFunction<Screen, ModContainer, Screen>> modConfigFactories = new HashMap<>();
        Set<String> mods = new HashSet<>();

        // Find all ids of mods that have a config
        FabricLoader.getInstance().getAllMods().forEach(container -> {
            ModContext context = new ModContext(container.getMetadata().getId());
            ClientHandler.getProviders().stream().flatMap(p -> p.getConfigurationsForMod(context).stream()).forEach(config -> {
                mods.add(config.getModId());
            });
        });

        // Remove Configured if exists
        mods.removeIf(s -> s.equals(Constants.MOD_ID));

        // Register config factory for all found mods
        mods.forEach(id -> {
            FabricLoader.getInstance().getModContainer(id).ifPresent(container -> {
                modConfigFactories.put(id, CatalogueConfigFactory::newConfigScreen);
            });
        });
        return modConfigFactories;
    }

    static Screen newConfigScreen(Screen currentScreen, ModContainer container)
    {
        String modId = container.getMetadata().getId();
        Map<ConfigType, Set<IModConfig>> modConfigMap = ClientHandler.createConfigMap(new ModContext(modId));
        if(modConfigMap.isEmpty())
            return null;
        return ConfigScreenHelper.createSelectionScreen(currentScreen, Component.literal(container.getMetadata().getName()), modConfigMap);
    }
}
