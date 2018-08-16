package net.novucs.ftop.entity;

import net.novucs.ftop.WorthType;
import net.novucs.ftop.util.GenericUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ChunkWorth {

    private final Map<WorthType, Double> worth;
    private Map<Material, Integer> materials;
    private Map<EntityType, Integer> spawners;
    private Map<String, Integer> specials;
    private long nextRecalculation;

    public ChunkWorth() {
        this(new EnumMap<>(WorthType.class), new EnumMap<>(Material.class), new EnumMap<>(EntityType.class), new HashMap<>());
    }

    public ChunkWorth(Map<WorthType, Double> worth, Map<Material, Integer> materials, Map<EntityType, Integer> spawners, Map<String, Integer> specials) {
        this.worth = worth;
        this.materials = materials;
        this.spawners = spawners;
        this.specials = specials;
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }

    public void setWorth(WorthType worthType, double worth) {
        if (!WorthType.isPlaced(worthType)) {
            throw new IllegalArgumentException("Liquid worth cannot be associated with chunks!");
        }

        worth = Math.max(0, worth);
        this.worth.put(worthType, worth);
    }

    public Map<WorthType, Double> getWorth() {
        return worth;
    }

    public Map<Material, Integer> getMaterials() {
        return materials;
    }

    public Map<EntityType, Integer> getSpawners() {
        return spawners;
    }

    public Map<String, Integer> getSpecials() {
        return specials;
    }

    public void setMaterials(Map<Material, Integer> materials) {
        this.materials = materials;
    }

    public void setSpawners(Map<EntityType, Integer> spawners) {
        this.spawners = spawners;
    }

    public void setSpecials(Map<String, Integer> specials) {
        this.specials = specials;
    }

    public void addMaterials(Map<Material, Integer> materials) {
        GenericUtils.addCountMap(this.materials, materials, false);
    }

    public void addSpawners(Map<EntityType, Integer> spawners) {
        GenericUtils.addCountMap(this.spawners, spawners, false);
    }

    public void addSpecials(Map<String, Integer> specials) {
        GenericUtils.addCountMap(this.specials, specials, false);
    }

    public void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
    }

    public long getNextRecalculation() {
        return nextRecalculation;
    }

    public void setNextRecalculation(long nextRecalculation) {
        this.nextRecalculation = nextRecalculation;
    }

    @Override
    public String toString() {
        return "ChunkWorth{" +
                "worth=" + worth +
                ", materials=" + materials +
                ", spawners=" + spawners +
                ", specials=" + specials +
                ", nextRecalculation=" + nextRecalculation +
                '}';
    }
}
