package net.novucs.ftop.hook.event;

import org.bukkit.event.HandlerList;

public class AllianceDisbandEvent extends AllianceEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String name;

    public AllianceDisbandEvent(String allianceId, String name) {
        super(allianceId);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
