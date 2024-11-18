package com.mrcrayfish.configured.client;

import com.mrcrayfish.configured.Constants;
import com.mrcrayfish.configured.api.ActionResult;
import com.mrcrayfish.configured.api.IModConfig;
import com.mrcrayfish.configured.client.screen.ActiveConfirmationScreen;
import com.mrcrayfish.configured.client.screen.ConfirmationScreen;
import com.mrcrayfish.configured.client.screen.IEditing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Author: MrCrayfish
 */
public class EditingTracker
{
    private IModConfig editingConfig;
    private boolean changed;

    private static EditingTracker instance;

    public static EditingTracker instance()
    {
        if(instance == null)
        {
            instance = new EditingTracker();
        }
        return instance;
    }

    private EditingTracker() {}

    public void onScreenOpen(Screen screen)
    {
        // Keeps track of the config currently being editing and runs events accordingly
        if(screen instanceof IEditing editing)
        {
            if(this.editingConfig == null)
            {
                this.changed = false;
                this.editingConfig = editing.getActiveConfig();
                this.editingConfig.startEditing();
                Constants.LOG.debug("Started editing '" + this.editingConfig.getFileName() + "'");
            }
            else if(editing.getActiveConfig() == null)
            {
                throw new NullPointerException("A null config was returned when getting active config");
            }
            else if(this.editingConfig != editing.getActiveConfig())
            {
                throw new IllegalStateException("Trying to edit a config while one is already loaded. This should not happen!");
            }
        }
        else if(this.editingConfig != null)
        {
            Constants.LOG.debug("Stopped editing '" + this.editingConfig.getFileName() + "'");
            this.editingConfig.stopEditing(this.changed);
            this.editingConfig = null;
            this.changed = false;
        }
    }

    public void markChanged()
    {
        this.changed = true;
    }
}
