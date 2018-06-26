package net.novucs.ftop.entity;

import net.novucs.ftop.util.SplaySet;

import java.util.HashMap;
import java.util.Map;

public class AllianceWorth extends Worth {
    
    private Map<String, FactionWorth> factions = new HashMap<>();
    private SplaySet<Worth> orderedFactions = SplaySet.create();
    
    public AllianceWorth(String allianceId, String name) {
        super(allianceId, name);
    }
    
    public void calculateWorth() {
        orderedFactions.clear();
        for (FactionWorth factionWorth : factions.values()) {
            addMaterials(factionWorth.getMaterials());
            addSpawners(factionWorth.getSpawners());
            addWorth(factionWorth.getWorth());
            orderedFactions.add(factionWorth);
        }
    }
    
    public void addFaction(FactionWorth factionWorth) {
        factions.put(factionWorth.getId(), factionWorth);
        orderedFactions.remove(factionWorth);
        orderedFactions.add(factionWorth);
    }
    
    public void removeFaction(FactionWorth factionWorth) {
        factions.remove(factionWorth.getId());
        orderedFactions.remove(factionWorth);
    }
    
    public Map<String, FactionWorth> getFactions() {
        return factions;
    }
    
    public SplaySet<Worth> getOrderedFactions() {
        return orderedFactions;
    }
    
    @Override
    public String toString() {
        return "AllianceWorth{" +
                "allianceId='" + getId() + '\'' +
                ", factions=" + getFactions().keySet() +
                ", worth=" + getWorth() +
                ", materials=" + getMaterials() +
                ", spawners=" + getSpawners() +
                ", name='" + getName() + '\'' +
                ", totalWorth=" + getTotalWorth() +
                '}';
    }
}
