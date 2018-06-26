package net.novucs.ftop.hook.event;

import org.bukkit.event.HandlerList;

public class AllianceJoinEvent extends AllianceEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String factionId;

    public AllianceJoinEvent(String allianceId, String factionId) {
        super(allianceId);
        this.factionId = factionId;
    }

    public String getFactionId() {
        return factionId;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
