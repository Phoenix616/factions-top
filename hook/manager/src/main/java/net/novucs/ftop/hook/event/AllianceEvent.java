package net.novucs.ftop.hook.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AllianceEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String allianceId;

    public AllianceEvent(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
