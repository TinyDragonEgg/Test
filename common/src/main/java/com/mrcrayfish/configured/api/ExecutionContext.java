package com.mrcrayfish.configured.api;

import com.mrcrayfish.configured.platform.Services;
import com.mrcrayfish.configured.util.ConfigHelper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for common checks
 *
 * Author: MrCrayfish
 */
public record ExecutionContext(@Nullable Player player)
{
    public boolean isClient()
    {
        return Services.PLATFORM.getEnvironment() == Environment.CLIENT;
    }

    public boolean isDedicatedServer()
    {
        return Services.PLATFORM.getEnvironment() == Environment.DEDICATED_SERVER;
    }

    public boolean isIntegratedServer()
    {
        return ConfigHelper.isIntegratedServer();
    }

    public boolean isIntegratedServerOwnedByPlayer()
    {
        return ConfigHelper.isServerOwnedByPlayer(this.player);
    }

    public boolean isSingleplayer()
    {
        return Services.PLATFORM.getEnvironment() == Environment.CLIENT && ConfigHelper.isPlayingGame() && !ConfigHelper.isPlayingLan();
    }

    public boolean isPlayingOnLan()
    {
        return Services.PLATFORM.getEnvironment() == Environment.CLIENT && ConfigHelper.isPlayingGame() && ConfigHelper.isPlayingLan();
    }

    public boolean isMainMenu()
    {
        return this.isClient() && !ConfigHelper.isPlayingGame();
    }

    public boolean isPlayingGame()
    {
        return !this.isClient() || ConfigHelper.isPlayingGame();
    }

    public boolean isPlayerAnOperator()
    {
        return ConfigHelper.isOperator(this.player);
    }

    public boolean isDeveloperPlayer()
    {
        return ConfigHelper.isDeveloper(this.player);
    }

    public boolean isPlayingOnRemoteServer()
    {
        return ConfigHelper.isPlayingOnRemoteServer();
    }

    public boolean isConfiguredInstalledRemotely()
    {
        return ConfigHelper.isConfiguredInstalledOnServer();
    }

    public boolean isLocalPlayer()
    {
        return this.player != null && this.player.isLocalPlayer();
    }
}
