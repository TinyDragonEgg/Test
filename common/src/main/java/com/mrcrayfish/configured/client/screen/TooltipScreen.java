package com.mrcrayfish.configured.client.screen;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.configured.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jetbrains.annotations.Nullable;
import java.util.List;


public abstract class TooltipScreen extends Screen
{
    private static final List<Component> DUMMY_TOOLTIP = ImmutableList.of(Component.empty());

    @Nullable
    public List<FormattedCharSequence> tooltipText;
    @Nullable
    public TooltipStyle tooltipStyle;

    protected TooltipScreen(Component title)
    {
        super(title);
    }

    protected void resetTooltip()
    {
        this.tooltipText = null;
        this.tooltipStyle = null;
    }

    /**
     * Sets the tool tip to render. Must be actively called in the render method as
     * the tooltip is reset every draw call.
     *
     * @param tooltip a tooltip list to show
     */
    public void setActiveTooltip(@Nullable List<FormattedCharSequence> tooltip)
    {
        this.resetTooltip();
        this.tooltipText = tooltip;
    }

    /**
     * Sets the tool tip from the given component. Must be actively called in the
     * render method as the tooltip is reset every draw call. This method automatically
     * splits the text.
     *
     * @param text the text to show on the tooltip
     */
    public void setActiveTooltip(Component text)
    {
        this.resetTooltip();
        this.tooltipText = this.minecraft.font.split(text, 200);
    }

    /**
     * Set the tool tip from the given component and colours. Must be actively called
     * in the render method as the tooltip is reset every draw call. This method
     * automatically splits the text.
     *
     * @param text the text to show on the tooltip
     */
    public void setActiveTooltip(Component text, @Nullable TooltipStyle style)
    {
        this.resetTooltip();
        this.tooltipText = this.minecraft.font.split(text, 200);
        this.tooltipStyle = style;
    }

    protected void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if(this.tooltipText != null)
        {
            // Use new tooltip system instead
            this.setTooltipForNextRenderPass(this.tooltipText);

            // Yep, this is strange. See the forge events below!
            //this.renderComponentTooltip(poseStack, DUMMY_TOOLTIP, mouseX, mouseY);
        }
    }

    public record ListMenuTooltipComponent(FormattedCharSequence text) implements TooltipComponent
    {
        public ClientTextTooltip asClientTextTooltip()
        {
            return new ClientTextTooltip(this.text);
        }
    }

    public enum TooltipStyle
    {
        SUCCESS(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "success")),
        HINT(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "hint")),
        ERROR(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "error")),
        LINK(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "link"));

        private final ResourceLocation texture;

        TooltipStyle(ResourceLocation texture)
        {
            this.texture = texture;
        }

        public ResourceLocation getTexture()
        {
            return this.texture;
        }
    }
}
