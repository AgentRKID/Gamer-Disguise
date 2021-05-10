package io.github.agentrkid.gamerdisguise.event;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerDisguiseEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled;

    private final Player player;
    private final String disguiseName;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
