package net.novucs.ftop.hook.event;

import org.bukkit.event.HandlerList;

public class AllianceRenameEvent extends AllianceEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String oldName;
    private final String newName;

    public AllianceRenameEvent(String allianceId, String oldName, String newName) {
        super(allianceId);
        this.oldName = oldName;
        this.newName = newName;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
