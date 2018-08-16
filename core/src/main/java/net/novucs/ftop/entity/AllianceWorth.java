package net.novucs.ftop.entity;

import net.novucs.ftop.WorthType;
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
            addFactionWorth(factionWorth);
        }
    }

    public void addFaction(FactionWorth factionWorth) {
        FactionWorth oldFaction = factions.get(factionWorth.getId().toLowerCase());
        if (oldFaction != null) {
            removeFaction(oldFaction);
        }
        orderedFactions.remove(factionWorth);
        factions.put(factionWorth.getId().toLowerCase(), factionWorth);
        addFactionWorth(factionWorth);
    }
    
    public void removeFaction(FactionWorth factionWorth) {
        FactionWorth oldFaction = factions.get(factionWorth.getId().toLowerCase());
        if (oldFaction != null) {
            orderedFactions.remove(oldFaction);
            removeMaterials(oldFaction.getMaterials());
            removeSpawners(oldFaction.getSpawners());
            removeSpecials(oldFaction.getSpecials());
            removeWorth(oldFaction.getWorth());
        }
        factions.remove(factionWorth.getId());
        orderedFactions.remove(factionWorth);
    }

    private void addFactionWorth(FactionWorth factionWorth) {
        addMaterials(factionWorth.getMaterials());
        addSpawners(factionWorth.getSpawners());
        addSpecials(factionWorth.getSpecials());
        addWorth(factionWorth.getWorth());
        orderedFactions.add(factionWorth);
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
                ", materials=" + materials +
                ", spawners=" + spawners +
                ", specials=" + specials +
                ", name='" + getName() + '\'' +
                ", totalWorth=" + getTotalWorth() +
                '}';
    }
}
