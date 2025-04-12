package com.mrcrayfish.configured.client.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.configured.Constants;
import com.mrcrayfish.configured.api.ActionResult;
import com.mrcrayfish.configured.api.IModConfig;
import com.mrcrayfish.configured.client.screen.widget.IconButton;
import com.mrcrayfish.configured.client.util.ScreenUtil;
import com.mrcrayfish.configured.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class WorldSelectionScreen extends ListMenuScreen
{
    private static final LevelResource SERVER_CONFIG_FOLDER = Services.CONFIG.getServerConfigResource();
    private static final ResourceLocation MISSING_ICON = ResourceLocation.withDefaultNamespace("textures/misc/unknown_server.png");

    private final IModConfig config;

    public WorldSelectionScreen(Screen parent, IModConfig config, Component title)
    {
        super(parent, Component.translatable("configured.gui.edit_world_config", title.plainCopy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)), 30);
        this.config = config;
    }

    @Override
    protected void constructEntries(List<Item> entries)
    {
        try
        {
            LevelStorageSource source = Minecraft.getInstance().getLevelSource();
            List<LevelSummary> levels = new ArrayList<>(source.loadLevelSummaries(source.findLevelCandidates()).join());
            if(levels.size() > 6)
            {
                entries.add(new TitleItem(Component.translatable("configured.gui.title.recently_played").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
                List<LevelSummary> recent = levels.stream().sorted(Comparator.comparing(s -> -s.getLastPlayed())).limit(3).toList();
                recent.forEach(summary -> entries.add(new WorldItem(summary)));
                levels.removeAll(recent);
                entries.add(new TitleItem(Component.translatable("configured.gui.title.other_worlds").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            }
            levels.stream().sorted(Comparator.comparing(LevelSummary::getLevelName)).forEach(summary -> {
                entries.add(new WorldItem(summary));
            });
        }
        catch(LevelStorageException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void init()
    {
        super.init();
        this.addRenderableWidget(ScreenUtil.button(this.width / 2 - 75, this.height - 29, 150, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().pushPose();
        graphics.pose().translate(this.width - 30, 15, 0);
        graphics.pose().scale(2.5F, 2.5F, 2.5F);
        graphics.drawString(this.font, Component.literal("?").withStyle(ChatFormatting.BOLD), 0, 0, 0xFFFFFF);
        graphics.pose().popPose();
    }

    @Override
    protected void updateTooltip(int mouseX, int mouseY)
    {
        super.updateTooltip(mouseX, mouseY);
        if(ScreenUtil.isMouseWithin(this.width - 30, 15, 23, 23, mouseX, mouseY))
        {
            this.setActiveTooltip(Component.translatable("configured.gui.server_config_info"));
        }
    }

    @Override
    public void onClose()
    {
        super.onClose();
        this.entries.forEach(item ->
        {
            if(item instanceof WorldItem)
            {
                ((WorldItem) item).disposeIcon();
            }
        });
    }

    public class WorldItem extends Item
    {
        private final Component worldName;
        private final Component folderName;
        private Path iconFile;
        private final Button modifyButton;
        private final FaviconTexture icon;

        public WorldItem(LevelSummary summary)
        {
            super(summary.getLevelName());
            this.worldName = Component.literal(summary.getLevelName());
            this.folderName = Component.literal(summary.getLevelId()).withStyle(ChatFormatting.DARK_GRAY);
            this.icon = FaviconTexture.forWorld(Minecraft.getInstance().getTextureManager(), summary.getLevelId());
            this.iconFile = summary.getIcon();
            this.validateIcon();
            this.loadWorldIcon();
            this.modifyButton = new IconButton(0, 0, 0, this.getIconV(), 60, this.getButtonLabel(), onPress -> {
                this.loadWorldConfig(summary.getLevelId(), summary.getLevelName());
            });
        }

        private void validateIcon()
        {
            if(this.iconFile == null)
                return;

            try
            {
                BasicFileAttributes attributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if(attributes.isSymbolicLink())
                {
                    List<ForbiddenSymlinkInfo> list = Minecraft.getInstance().directoryValidator().validateSymlink(this.iconFile);
                    if(!list.isEmpty())
                    {
                        this.iconFile = null;
                        return;
                    }
                    attributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class);
                }
                if(!attributes.isRegularFile())
                {
                    this.iconFile = null;
                }
            }
            catch(IOException e)
            {
                this.iconFile = null;
            }
        }

        private Component getButtonLabel()
        {
            if(WorldSelectionScreen.this.config.isReadOnly())
            {
                return Component.translatable("configured.gui.view");
            }
            return Component.translatable("configured.gui.select");
        }

        private int getIconV()
        {
            if(WorldSelectionScreen.this.config.isReadOnly())
            {
                return 33;
            }
            return 22;
        }

        @Override
        public List<? extends GuiEventListener> children()
        {
            return ImmutableList.of(this.modifyButton);
        }

        @Override
        public void render(GuiGraphics graphics, int x, int top, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks)
        {
            if(x % 2 != 0) graphics.fill(left, top, left + width, top + 24, 0x55000000);
            if(this.modifyButton.isMouseOver(mouseX, mouseY)) graphics.fill(left - 1, top - 1, left + 25, top + 25, 0xFFFFFFFF);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(RenderType::guiTextured, this.icon.textureLocation(), left, top, 0, 0, 24, 24, 32, 32, 32, 32);
            graphics.drawString(WorldSelectionScreen.this.minecraft.font, this.worldName, left + 30, top + 3, 0xFFFFFF);
            graphics.drawString(WorldSelectionScreen.this.minecraft.font, this.folderName, left + 30, top + 13, 0xFFFFFF);
            this.modifyButton.setX(left + width - 61);
            this.modifyButton.setY(top + 2);
            this.modifyButton.render(graphics, mouseX, mouseY, partialTicks);
        }

        private void loadWorldIcon()
        {
            if(this.iconFile == null || !Files.isRegularFile(this.iconFile))
                return;
            try(InputStream is = Files.newInputStream(this.iconFile); NativeImage image = NativeImage.read(is))
            {
                if(image.getWidth() != 64 || image.getHeight() != 64)
                    return;
                this.icon.upload(image);
            }
            catch(IOException ignored) {}
        }

        public void disposeIcon()
        {
            this.icon.clear();
        }

        private void loadWorldConfig(String worldFileName, String worldName)
        {
            try(LevelStorageSource.LevelStorageAccess storageAccess = Minecraft.getInstance().getLevelSource().createAccess(worldFileName))
            {
                // TODO move to config specific
                Path worldConfigPath = storageAccess.getLevelPath(SERVER_CONFIG_FOLDER);
                PathUtils.createParentDirectories(worldConfigPath);
                if(!Files.isDirectory(worldConfigPath))
                    Files.createDirectory(worldConfigPath);
                ActionResult result = WorldSelectionScreen.this.config.loadWorldConfig(worldConfigPath);
                if(result.asBoolean())
                {
                    Component configName = Component.literal(ModConfigSelectionScreen.createLabelFromModConfig(WorldSelectionScreen.this.config));
                    Component newTitle = Component.literal(worldName).copy().append(Component.literal(" > ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).append(configName);
                    WorldSelectionScreen.this.minecraft.setScreen(new ConfigScreen(WorldSelectionScreen.this.parent, newTitle, WorldSelectionScreen.this.config));
                    return;
                }
                Component message = result.message().orElse(Component.translatable("configured.gui.load_world_config_failed"));
                ConfirmationScreen.showError(WorldSelectionScreen.this.minecraft, WorldSelectionScreen.this, message);
            }
            catch(IOException e)
            {
                Constants.LOG.error("Failed to load world config", e);
                ConfirmationScreen.showError(WorldSelectionScreen.this.minecraft, WorldSelectionScreen.this, Component.translatable("configured.gui.load_world_config_exception"));
            }
        }
    }
}
