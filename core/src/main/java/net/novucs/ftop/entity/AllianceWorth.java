package net.novucs.ftop.entity;

import net.novucs.ftop.WorthType;
import net.novucs.ftop.util.SplaySet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AllianceWorth extends Worth {
    
    private Map<String, FactionWorth> factions = new HashMap<>();
    private SplaySet<Worth> orderedFactions = SplaySet.create();
    
    public AllianceWorth(String allianceId, String name) {
        super(allianceId, name);
    }
    
    public void calculateWorth() {
        orderedFactions.clear();
        materials.clear();
        spawners.clear();
        totalSpawners = 0;
        worth.replaceAll((t, a) -> {
            if (t != WorthType.ALLIANCE_BALANCE) {
                totalWorth -= a;
                return 0D;
            }
            return a;
        });
        for (FactionWorth factionWorth : factions.values()) {
            addMaterials(factionWorth.getMaterials());
            addSpawners(factionWorth.getSpawners());
            addWorth(factionWorth.getWorth());
            orderedFactions.add(factionWorth);
        }
    }
    
    public void addFaction(FactionWorth factionWorth) {
        FactionWorth oldFaction = factions.get(factionWorth.getId().toLowerCase());
        if (oldFaction != null) {
            removeFaction(oldFaction);
        }
        orderedFactions.remove(factionWorth);
        factions.put(factionWorth.getId().toLowerCase(), factionWorth);
        addMaterials(factionWorth.getMaterials());
        addSpawners(factionWorth.getSpawners());
        addWorth(factionWorth.getWorth());
        orderedFactions.add(factionWorth);
    }
    
    public void removeFaction(FactionWorth factionWorth) {
        FactionWorth oldFaction = factions.get(factionWorth.getId().toLowerCase());
        if (oldFaction != null) {
            orderedFactions.remove(oldFaction);
            removeMaterials(oldFaction.getMaterials());
            removeSpawners(oldFaction.getSpawners());
            removeWorth(oldFaction.getWorth());
        }
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
