package com.mrcrayfish.configured.network;

import com.mrcrayfish.configured.client.ClientSessionData;
import com.mrcrayfish.configured.network.message.MessageSessionData;

/**
 * Author: MrCrayfish
 */
public class ClientPlayHandler
{
    public static void handleSessionData(MessageSessionData message)
    {
        ClientSessionData.setDeveloper(message.developer());
        ClientSessionData.setLan(message.lan());
    }
}
