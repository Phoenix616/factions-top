package net.novucs.ftop.entity;

import net.novucs.ftop.WorthType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class FactionWorth extends Worth {
    private String allianceId = null;
    
    public FactionWorth(String factionId, String name) {
        super(factionId, name);
    }
    
    /**
     * @deprecated Use {@link #getId()}
     */
    @Deprecated
    public String getFactionId() {
        return getId();
    }
    
    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }
    
    public String getAllianceId() {
        return allianceId;
    }

    public void addAll(ChunkWorth chunkWorth) {
        addMaterials(chunkWorth.getMaterials());
        addSpawners(chunkWorth.getSpawners());
        addSpecials(chunkWorth.getSpecials());
        addWorth(chunkWorth.getWorth());
    }

    @Override
    public String toString() {
        return "FactionWorth{" +
                "factionId='" + getId() + '\'' +
                ", allianceId='" + getAllianceId() + '\'' +
                ", worth=" + getWorth() +
                ", materials=" + materials +
                ", spawners=" + spawners +
                ", specials=" + specials +
                ", name='" + getName() + '\'' +
                ", totalWorth=" + getTotalWorth() +
                '}';
    }
}
