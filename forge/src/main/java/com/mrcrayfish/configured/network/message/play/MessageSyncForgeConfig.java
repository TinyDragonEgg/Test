package com.mrcrayfish.configured.network.message.play;

import com.mrcrayfish.configured.network.handler.ForgeServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Author: MrCrayfish
 */
public record MessageSyncForgeConfig(String fileName, byte[] data)
{
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSyncForgeConfig> STREAM_CODEC = StreamCodec.ofMember(MessageSyncForgeConfig::encode, MessageSyncForgeConfig::decode);

    public static void encode(MessageSyncForgeConfig message, FriendlyByteBuf buffer)
    {
        buffer.writeUtf(message.fileName);
        buffer.writeByteArray(message.data);
    }

    public static MessageSyncForgeConfig decode(FriendlyByteBuf buffer)
    {
        return new MessageSyncForgeConfig(buffer.readUtf(), buffer.readByteArray());
    }

    public static void handle(MessageSyncForgeConfig message, CustomPayloadEvent.Context ctx)
    {
        ServerPlayer player = ctx.getSender();
        if(player != null)
        {
            ctx.enqueueWork(() -> ForgeServerPlayHandler.handleSyncServerConfigMessage(player, message));
        }
        ctx.setPacketHandled(true);
    }
}
