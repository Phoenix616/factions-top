package net.novucs.ftop.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.util.GenericUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class IdentityCache {

    private final BiMap<Key, Integer> chunkMaterial = HashBiMap.create();
    private final BiMap<Key, Integer> chunkSpawner = HashBiMap.create();
    private final BiMap<Key, Integer> chunkSpecial = HashBiMap.create();
    private final BiMap<Key, Integer> chunkWorth = HashBiMap.create();
    private final BiMap<Key, Integer> chunkPos = HashBiMap.create();

    private final BiMap<Key, Integer> material = HashBiMap.create();
    private final BiMap<Key, Integer> spawner = HashBiMap.create();
    private final BiMap<Key, Integer> special = HashBiMap.create();
    private final BiMap<Key, Integer> world = HashBiMap.create();
    private final BiMap<Key, Integer> worth = HashBiMap.create();

    private final Set<String> faction = new HashSet<>();
    private final Table<String, Integer, Integer> factionMaterial = HashBasedTable.create();
    private final Table<String, Integer, Integer> factionSpawner = HashBasedTable.create();
    private final Table<String, Integer, Integer> factionSpecial = HashBasedTable.create();
    private final Table<String, Integer, Integer> factionWorth = HashBasedTable.create();

    private final BiMap<Key, Integer> block = HashBiMap.create();
    private final BiMap<Key, Integer> sign = HashBiMap.create();

    public Set<String> getFactionIds() {
        return faction;
    }

    public boolean hasChunkMaterial(int chunkId, int materialId) {
        return chunkMaterial.containsKey(new Key<>(chunkId, materialId));
    }

    public boolean hasChunkSpawner(int chunkId, int spawnerId) {
        return chunkSpawner.containsKey(new Key<>(chunkId, spawnerId));
    }

    public boolean hasChunkSpecial(int chunkId, int specialId) {
        return chunkSpecial.containsKey(new Key<>(chunkId, specialId));
    }

    public boolean hasChunkWorth(int chunkId, int worthId) {
        return chunkWorth.containsKey(new Key<>(chunkId, worthId));
    }

    public boolean hasChunkPos(int worldId, int x, int z) {
        return chunkPos.containsKey(new Key<>(worldId, x, z));
    }

    public boolean hasMaterial(String name) {
        return material.containsKey(new Key<>(name));
    }

    public boolean hasSpawner(String name) {
        return spawner.containsKey(new Key<>(name));
    }
    public boolean hasSpecial(String name) {
        return special.containsKey(new Key<>(name));
    }

    public boolean hasWorld(String name) {
        return world.containsKey(new Key<>(name));
    }

    public boolean hasWorth(String name) {
        return worth.containsKey(new Key<>(name));
    }

    public boolean hasFaction(String factionId) {
        return faction.contains(factionId);
    }

    public boolean hasFactionMaterial(String factionId, int materialId) {
        return factionMaterial.contains(factionId, materialId);
    }

    public boolean hasFactionSpawner(String factionId, int spawnerId) {
        return factionSpawner.contains(factionId, spawnerId);
    }

    public boolean hasFactionSpecial(String factionId, int specialId) {
        return factionSpecial.contains(factionId, specialId);
    }

    public boolean hasFactionWorth(String factionId, int worthId) {
        return factionWorth.contains(factionId, worthId);
    }

    public boolean hasBlock(int worldId, int x, int y, int z) {
        return block.containsKey(new Key<>(worldId, x, y, z));
    }

    public boolean hasSign(int blockId) {
        return sign.containsKey(new Key<>(blockId));
    }

    public Integer getChunkMaterialId(int chunkId, int materialId) {
        return chunkMaterial.get(new Key<>(chunkId, materialId));
    }

    public Integer getChunkSpawnerId(int chunkId, int spawnerId) {
        return chunkSpawner.get(new Key<>(chunkId, spawnerId));
    }

    public Integer getChunkSpecialId(int chunkId, int special) {
        return chunkSpecial.get(new Key<>(chunkId, special));
    }

    public Integer getChunkWorthId(int chunkId, int worthId) {
        return chunkWorth.get(new Key<>(chunkId, worthId));
    }

    public Integer getChunkPosId(int worldId, int x, int z) {
        return chunkPos.get(new Key<>(worldId, x, z));
    }

    public Integer getMaterialId(String name) {
        return material.get(new Key<>(name));
    }

    public Integer getSpawnerId(String name) {
        return spawner.get(new Key<>(name));
    }

    public Integer getSpecialId(String name) {
        return special.get(new Key<>(name));
    }

    public Integer getWorldId(String name) {
        return world.get(new Key<>(name));
    }

    public Integer getWorthId(String name) {
        return worth.get(new Key<>(name));
    }

    public Integer getFactionMaterialId(String factionId, int materialId) {
        return factionMaterial.get(factionId, materialId);
    }

    public Integer getFactionSpawnerId(String factionId, int spawnerId) {
        return factionSpawner.get(factionId, spawnerId);
    }

    public Integer getFactionSpecialId(String factionId, int specialId) {
        return factionSpecial.get(factionId, specialId);
    }

    public Integer getFactionWorthId(String factionId, int worthId) {
        return factionWorth.get(factionId, worthId);
    }

    public Integer getBlockId(int worldId, int x, int y, int z) {
        return block.get(new Key<>(worldId, x, y, z));
    }

    public Integer getSignId(int blockId) {
        return sign.get(new Key<>(blockId));
    }

    public Optional<String> getWorldName(int worldId) {
        Key key = world.inverse().get(worldId);
        return key == null ? Optional.empty() : Optional.of((String) key.getObjects()[0]);
    }

    public Optional<Material> getMaterial(int materialId) {
        Key key = material.inverse().get(materialId);
        return key == null ? Optional.empty() : GenericUtils.parseEnum(Material.class, (String) key.getObjects()[0]);
    }

    public Optional<EntityType> getSpawner(int spawnerId) {
        Key key = spawner.inverse().get(spawnerId);
        return key == null ? Optional.empty() : GenericUtils.parseEnum(EntityType.class, (String) key.getObjects()[0]);
    }

    public Optional<String> getSpecial(int specialId) {
        Key key = special.inverse().get(specialId);
        return key == null ? Optional.empty() : Optional.of((String) key.getObjects()[0]);
    }

    public Optional<WorthType> getWorthType(int worthId) {
        Key key = worth.inverse().get(worthId);
        return key == null ? Optional.empty() : GenericUtils.parseEnum(WorthType.class, (String) key.getObjects()[0]);
    }

    public Optional<BlockPos> getBlock(int blockId) {
        Key key = block.inverse().get(blockId);

        int worldId = (int) key.getObjects()[0];
        int x = (int) key.getObjects()[1];
        int y = (int) key.getObjects()[2];
        int z = (int) key.getObjects()[3];

        Optional<String> worldName = getWorldName(worldId);

        return worldName.isPresent() ? Optional.of(BlockPos.of(worldName.get(), x, y, z)) : Optional.empty();
    }

    public void setChunkMaterialId(int chunkId, int materialId, Integer value) {
        chunkMaterial.put(new Key<>(chunkId, materialId), value);
    }

    public void setChunkSpawnerId(int chunkId, int spawnerId, Integer value) {
        chunkSpawner.put(new Key<>(chunkId, spawnerId), value);
    }

    public void setChunkSpecialId(int chunkId, int specialId, Integer value) {
        chunkSpecial.put(new Key<>(chunkId, specialId), value);
    }

    public void setChunkWorthId(int chunkId, int worthId, Integer value) {
        chunkWorth.put(new Key<>(chunkId, worthId), value);
    }

    public void setChunkPosId(int worldId, int x, int z, Integer value) {
        chunkPos.put(new Key<>(worldId, x, z), value);
    }

    public void setMaterialId(String name, Integer value) {
        material.put(new Key<>(name), value);
    }

    public void setSpawnerId(String name, Integer value) {
        spawner.put(new Key<>(name), value);
    }

    public void setSpecialId(String name, Integer value) {
        special.put(new Key<>(name), value);
    }

    public void setWorldId(String name, Integer value) {
        world.put(new Key<>(name), value);
    }

    public void setWorthId(String name, Integer value) {
        worth.put(new Key<>(name), value);
    }

    public void addFaction(String factionId) {
        faction.add(factionId);
    }

    public void removeFaction(String factionId) {
        faction.remove(factionId);
        factionMaterial.row(factionId).clear();
        factionSpawner.row(factionId).clear();
        factionSpecial.row(factionId).clear();
        factionWorth.row(factionId).clear();
    }

    public void setFactionMaterialId(String factionId, int materialId, Integer value) {
        factionMaterial.put(factionId, materialId, value);
    }

    public void setFactionSpawnerId(String factionId, int spawnerId, Integer value) {
        factionSpawner.put(factionId, spawnerId, value);
    }

    public void setFactionSpecialId(String factionId, int specialId, Integer value) {
        factionSpecial.put(factionId, specialId, value);
    }

    public void setFactionWorthId(String factionId, int worthId, Integer value) {
        factionWorth.put(factionId, worthId, value);
    }

    public void setBlockId(int worldId, int x, int y, int z, Integer value) {
        block.put(new Key<>(worldId, x, y, z), value);
    }

    public void setSignId(int blockId, Integer value) {
        sign.put(new Key<>(blockId), value);
    }

    private static class Key<T> {
        private final T[] objects;

        @SafeVarargs
        private Key(T... objects) {
            this.objects = objects;
        }

        public T[] getObjects() {
            return objects;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key<?> key = (Key<?>) o;
            return Arrays.equals(objects, key.objects);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(objects);
        }
    }
}
