package net.novucs.ftop.hook.event;

import org.bukkit.event.HandlerList;

public class AllianceEconomyEvent extends EconomyEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String allianceId;

    public AllianceEconomyEvent(String allianceId, double oldBalance, double newBalance) {
        super(oldBalance, newBalance);
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
