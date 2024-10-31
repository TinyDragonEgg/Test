package com.mrcrayfish.configured.api;

import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public record ActionResult(boolean asBoolean, Optional<Component> message)
{
    private static final ActionResult ALLOW = new ActionResult(true, Optional.empty());
    private static final ActionResult DISALLOW = new ActionResult(false, Optional.empty());

    public static ActionResult success()
    {
        return ALLOW;
    }

    public static ActionResult success(Component message)
    {
        return new ActionResult(true, Optional.of(message));
    }

    public static ActionResult fail()
    {
        return DISALLOW;
    }

    public static ActionResult fail(Component message)
    {
        return new ActionResult(false, Optional.of(message));
    }
}
