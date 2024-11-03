package com.mrcrayfish.configured.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Speiger
 * <p>
 * Config interface that allows you to implement custom config formats into Configured.
 * This isn't a full automatic system. It is just a interface to make such things actually possible.
 */
public interface IModConfig
{
    /**
     * The storage type of this config. This determines where the configuration is loaded from and saved to.
     *
     * @return the storage type of the config
     */
    ConfigType getType();

    /**
     * @return the filename of the config
     */
    String getFileName();

    /**
     * @return the modId of the config.
     */
    String getModId();

    /**
     * @return If this config is considered read only.
     */
    default boolean isReadOnly()
    {
        return false;
    }

    /**
     * @return the name to display on the file list
     */
    @Nullable
    default String getTranslationKey()
    {
        return null;
    }

    /**
     * This function returns provides the Entry point of the Configuration File.
     * So users can traverse through it.
     *
     * @return the root node.
     */
    IConfigEntry createRootEntry();

    /**
     * This function expects you to do everything necessary to save the config.
     * If you want an example, see implements in the impl package.
     *
     * @param entry the entry that is used or should be checked for updates.
     *              Also make sure to check children if children of said entry have been changed too.
     */
    ActionResult update(IConfigEntry entry);

    /**
     * A simple utility function to determine if this config has been changed, as in differs from its
     * default values. This method will always return false if this config is a world config type. It
     * should be noted that if the config will be temporarily loaded from file and closed immediately
     * after; this should be considered before calling this method!
     *
     * @return true if this config differs from its default values
     */
    default boolean isChanged()
    {
        return false;
    }

    /**
     * An event that is fired when this config is starting to be edited by the player using the
     * in-game menu. This is only fired once during the initial opening of the config.
     */
    default void startEditing() {}

    /**
     * An event that is fired when this config is no longer being edited by the player using the
     * in-game menu. This is only fired once after the player has exited the menu. The changed
     * parameter indicates that updates were performed to the config during editing and were saved.
     * This method should be used for
     *
     * @param updated True if the config was updated during editing
     */
    default void stopEditing(boolean updated) {}

    /**
     * A Helper function that allows to load the config from the server into the config instance.
     * Since this is highly dynamic it has to be done on a per implementation basis.
     *
     * @param path to the expected config folder.
     * @throws IOException since its IO work the function will be expected to maybe throw IOExceptions
     */
    default ActionResult loadWorldConfig(Path path) throws IOException
    {
        return ActionResult.fail();
    }

    /**
     * Provides an optional runnable, that when executed, restores the entire config to its default
     * values. An empty optional indicates that the config cannot be restored and the functionality
     * will be disabled in the user interface. Players that do not have permission to edit the config,
     * according to {@link #canPlayerEdit(Player)}, will not be able to run the restore task.
     *
     * @return An optional runnable that restores the config to its default values.
     */
    default Optional<Runnable> restoreDefaultsTask()
    {
        return Optional.empty();
    }

    /**
     * Creates a task that sends a request to the server to download the data for this config. This
     * is only applicable to configs that are not available on the client. The runnable should
     * simply send a packet to the server, authorise, then send back to the client the data.
     *
     * @return An optional runnable. If the optional is empty, this indicates the config cannot be
     * requested
     */
    default Optional<Runnable> requestFromServerTask()
    {
        return Optional.empty();
    }

    /**
     * Allows you to provide the rules on whether a player is allowed to edit this config. Note that
     * the player parameter may be null and this generally indicates the config is being edited from
     * the main menu.
     *
     * @param player the instance of the player or null if in main menu
     * @return A query result containing the editing permissions
     */
    default ActionResult canPlayerEdit(@Nullable Player player)
    {
        return ActionResult.fail();
    }

    /**
     * Shows a custom confirmation screen just before saving. This can be used to warn the player
     * of an impending action once the config is saved. To show the confirmation screen, the
     * returned {@link ActionResult} must be successful and include a message. Use the helper
     * method {@link ActionResult#success(Component)} to create the correct return result.
     *
     * @return A successful action result with a message or fail to show nothing
     */
    default ActionResult showSaveConfirmation(Player player)
    {
        return ActionResult.fail();
    }
}
