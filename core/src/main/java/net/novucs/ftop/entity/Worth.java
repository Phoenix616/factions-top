package net.novucs.ftop.entity;

import net.novucs.ftop.WorthType;
import net.novucs.ftop.util.GenericUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class Worth implements Comparable<Worth> {
    
    private final String id;
    
    private String name;
    
    protected final Map<WorthType, Double> worth = new EnumMap<>(WorthType.class);
    protected final Map<Material, Integer> materials = new EnumMap<>(Material.class);
    protected final Map<EntityType, Integer> spawners = new EnumMap<>(EntityType.class);
    protected final Map<String, Integer> specials = new HashMap<>();
    protected double totalWorth = 0;
    protected int totalSpawners = 0;
    
    public Worth(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }
    
    public Map<WorthType, Double> getWorth() {
        return worth;
    }
    
    public Map<Material, Integer> getMaterials() {
        return Collections.unmodifiableMap(materials);
    }
    
    public Map<EntityType, Integer> getSpawners() {
        return Collections.unmodifiableMap(spawners);
    }

    public Map<String, Integer> getSpecials() {
        return Collections.unmodifiableMap(specials);
    }
    
    public int getTotalSpawnerCount() {
        return totalSpawners;
    }
    
    public double getTotalWorth() {
        return totalWorth;
    }
    
    private void setWorth(WorthType worthType, double worth) {
        worth = Math.max(0, worth);
        Double prev = this.worth.put(worthType, worth);
        totalWorth += worth - (prev == null ? 0 : prev);
    }
    
    public void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
    }

    protected void removeWorth(Map<WorthType, Double> worth) {
        worth.values().forEach(amount -> totalWorth -= amount);
        for (Map.Entry<WorthType, Double> entry : worth.entrySet()) {
            double amount = Math.max(0D, this.worth.getOrDefault(entry.getKey(), 0D) - entry.getValue());
            this.worth.put(entry.getKey(), amount);
        }
    }
    
    public void addMaterials(Map<Material, Integer> materials) {
        GenericUtils.addCountMap(this.materials, materials, false);
    }
    
    public void removeMaterials(Map<Material, Integer> materials) {
        GenericUtils.removeCountMap(this.materials, materials, false);
    }
    
    public void addSpawners(Map<EntityType, Integer> spawners) {
        spawners.values().forEach(count -> totalSpawners += count);
        GenericUtils.addCountMap(this.spawners, spawners, false);
    }
    
    public void removeSpawners(Map<EntityType, Integer> spawners) {
        spawners.values().forEach(count -> totalSpawners -= count);
        GenericUtils.removeCountMap(this.spawners, spawners, false);
    }

    public void addSpecials(Map<String, Integer> specials) {
        GenericUtils.addCountMap(this.specials, specials, false);
    }

    public void removeSpecials(Map<String, Integer> specials) {
        GenericUtils.removeCountMap(this.specials, specials, false);
    }
    
    protected void addWorth(Map<WorthType, Double> worth) {
        for (Map.Entry<WorthType, Double> entry : worth.entrySet()) {
            double amount = this.worth.getOrDefault(entry.getKey(), 0d);
            totalWorth += entry.getValue();
            this.worth.put(entry.getKey(), amount + entry.getValue());
        }
    }
    
    @Override
    public int compareTo(Worth o) {
        return Double.compare(o.getTotalWorth(), getTotalWorth());
    }
    
    @Override
    public String toString() {
        return "Worth{" +
                "id='" + getId() + '\'' +
                ", worth=" + getWorth() +
                ", materials=" + getMaterials() +
                ", spawners=" + getSpawners() +
                ", specials=" + getSpecials() +
                ", name='" + getName() + '\'' +
                ", totalWorth=" + getTotalWorth() +
                '}';
    }
    
}
