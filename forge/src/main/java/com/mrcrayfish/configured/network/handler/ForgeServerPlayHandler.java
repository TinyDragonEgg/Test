package com.mrcrayfish.configured.network.handler;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Joiner;
import com.mrcrayfish.configured.Constants;
import com.mrcrayfish.configured.api.ActionResult;
import com.mrcrayfish.configured.impl.forge.ForgeConfig;
import com.mrcrayfish.configured.network.ForgeNetwork;
import com.mrcrayfish.configured.network.message.play.MessageSyncForgeConfig;
import com.mrcrayfish.configured.network.ServerPlayHelper;
import com.mrcrayfish.configured.util.ForgeConfigHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.PacketDistributor;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: MrCrayfish
 */
public class ForgeServerPlayHandler
{
    public static void handleSyncServerConfigMessage(ServerPlayer player, MessageSyncForgeConfig message)
    {
        if(!ServerPlayHelper.canEditServerConfigs(player))
            return;

        Constants.LOG.debug("Received server config sync from player: {}", player.getName().getString());

        ModConfig modConfig = ForgeConfigHelper.getForgeConfig(message.fileName());
        if(modConfig == null)
        {
            Constants.LOG.warn("{} tried to update a Forge config that doesn't exist!", player.getName().getString());
            player.connection.disconnect(Component.translatable("configured.multiplayer.disconnect.bad_config_packet"));
            return;
        }

        if(modConfig.getType() != ModConfig.Type.SERVER)
        {
            Constants.LOG.warn("{} tried to update a Forge config that isn't a server type", player.getName().getString());
            player.connection.disconnect(Component.translatable("configured.multiplayer.disconnect.bad_config_packet"));
            return;
        }

        ForgeConfigSpec spec = ForgeConfigHelper.findConfigSpec(modConfig.getSpec());
        if(spec == null)
        {
            Constants.LOG.warn("Unable to process Forge server config update due to unknown spec for config: {}", message.fileName());
            player.connection.disconnect(Component.translatable("configured.multiplayer.disconnect.bad_config_packet"));
            return;
        }

        ForgeConfig config = new ForgeConfig(modConfig, spec);
        ActionResult permission = config.canPlayerEdit(player);
        if(!permission.asBoolean())
        {
            Constants.LOG.warn("{} tried to update the Forge config '{}' but didn't have permission", player.getName().getString(), modConfig.getFileName());
            player.connection.disconnect(Component.translatable("configured.multiplayer.disconnect.unauthorized_request"));
            return;
        }

        try
        {
            CommentedConfig data = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(message.data()));
            AtomicBoolean malformed = new AtomicBoolean();
            int result = spec.correct(data, (action, path, incorrectValue, correctedValue) -> {
                if(action == ConfigSpec.CorrectionAction.ADD || action == ConfigSpec.CorrectionAction.REMOVE) {
                    malformed.set(true);
                } else if (action == ConfigSpec.CorrectionAction.REPLACE) {
                    Constants.LOG.warn("The value for path \"{}\" was originally \"{}\" but was corrected to \"{}\"", path, incorrectValue, correctedValue);
                }
            });

            // If config data is missing or added new entries, reject update
            if(malformed.get())
            {
                Constants.LOG.warn("{} sent malformed config data when updating a NeoForge config: {}", player.getName().getString(), modConfig.getFileName());
                player.connection.disconnect(Component.translatable("configured.multiplayer.disconnect.bad_config_packet"));
                return;
            }

            if(result != 0)
            {
                Constants.LOG.debug("Config data sent from {} needed to be corrected", player.getName().getString());
            }

            modConfig.getConfigData().putAll(data);
        }
        catch(ParsingException e)
        {
            Constants.LOG.warn("{} sent malformed config data to the server", player.getName().getString());
            player.connection.disconnect(Component.translatable("configured.multiplayer.disconnect.bad_config_packet"));
            ServerPlayHelper.sendMessageToOperators(Component.translatable("configured.chat.malformed_config_data", player.getName(), Component.literal(modConfig.getFileName()).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.RED), player);
            return;
        }
        catch(Exception e)
        {
            Constants.LOG.warn("An error occurred when updating config: '" + message.fileName() + "'", e);
            return;
        }

        Constants.LOG.debug("Successfully processed config update for '" + message.fileName() + "'");
        ServerPlayHelper.sendMessageToOperators(Component.translatable("configured.chat.config_updated", player.getName(), modConfig.getFileName()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), player);

        // Kick all other players and ask them to rejoin
        player.server.getPlayerList().getPlayers().forEach(player1 -> {
            if(!player1.equals(player)) {
                player1.connection.disconnect(Component.translatable("configured.gui.neoforge.server_configs_updated"));
            }
        });
    }
}
