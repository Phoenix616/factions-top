package net.novucs.ftop.manager;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.RecalculateReason;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.AllianceWorth;
import net.novucs.ftop.entity.ChestWorth;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.entity.ChunkWorth;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.entity.Worth;
import net.novucs.ftop.util.GenericUtils;
import net.novucs.ftop.util.ItemMatcher;
import net.novucs.ftop.util.SplaySet;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class WorthManager implements PluginService {

    private final FactionsTopPlugin plugin;
    private final Map<ChunkPos, ChunkWorth> chunks = new HashMap<>();
    private final Map<String, FactionWorth> factions = new HashMap<>();
    private final Map<String, AllianceWorth> alliances = new HashMap<>();
    private final SplaySet<Worth> orderedFactions = SplaySet.create();
    private final SplaySet<Worth> orderedAlliances = SplaySet.create();
    private final Table<ChunkPos, WorthType, Double> recalculateQueue = HashBasedTable.create();
    private final Table<ChunkPos, Material, Integer> materialsQueue = HashBasedTable.create();

    public WorthManager(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns an unmodifiable view of the ordered factions.
     *
     * @return the ordered factions.
     */
    public SplaySet<Worth> getOrderedFactions() {
        return orderedFactions;
    }

    /**
     * Returns all loaded faction IDs.
     *
     * @return all faction IDs.
     */
    public Set<String> getFactionIds() {
        return Collections.unmodifiableSet(factions.keySet());
    }
    
    /**
     * Returns an unmodifiable view of the ordered alliances.
     *
     * @return the ordered alliances.
     */
    public SplaySet<Worth> getOrderedAlliances() {
        return orderedAlliances;
    }

    /**
     * Gets the worth for a particular faction ID.
     *
     * @param factionId the faction ID.
     * @return the {@link FactionWorth} or null if no worth found.
     */
    public FactionWorth getWorth(String factionId) {
        return factions.get(factionId.toLowerCase());
    }

    /**
     * Gets the worth for a particular alliance ID.
     *
     * @param allianceId the alliance ID.
     * @return the {@link AllianceWorth} or null if no worth found.
     */
    public AllianceWorth getAllianceWorth(String allianceId) {
        return alliances.get(allianceId.toLowerCase());
    }

    @Override
    public void initialize() {
    }

    @Override
    public void terminate() {
    }

    public Map<ChunkPos, ChunkWorth> getChunks() {
        return chunks;
    }

    public void loadChunks(Map<ChunkPos, ChunkWorth> chunks) {
        this.chunks.clear();
        this.chunks.putAll(chunks);
    }

    public void updateAllFactions() {
        factions.clear();

        for (Map.Entry<ChunkPos, ChunkWorth> chunk : chunks.entrySet()) {
            FactionWorth worth = getOrCreateFactionWorth(chunk.getKey());
            if (worth != null) {
                worth.addAll(chunk.getValue());
            }
        }

        orderedFactions.clear();

        for (FactionWorth worth : factions.values()) {
            orderedFactions.add(worth);
            plugin.getPersistenceTask().queue(worth);
        }

        alliances.clear();

        for (FactionWorth faction : factions.values()) {
            getOrCreateAllianceWorth(faction.getAllianceId());
        }

        orderedAlliances.clear();
        orderedAlliances.addAll(alliances.values());
    }

    /**
     * Gets the chunk worth profile for a specific chunk.
     *
     * @param pos the position of the chunk.
     * @return the chunk profile.
     */
    private ChunkWorth getChunkWorth(ChunkPos pos) {
        return chunks.computeIfAbsent(pos, k -> new ChunkWorth());
    }

    /**
     * Gets a faction worth profile by a chunk.
     *
     * @param pos the chunk.
     * @return the worth of a faction who has claimed this chunk, or null if no
     * valid faction owns this land.
     */
    private FactionWorth getOrCreateFactionWorth(ChunkPos pos) {
        return getOrCreateFactionWorth(plugin.getFactionsHook().getFactionAt(pos));
    }

    /**
     * Gets the faction worth profile by a faction ID.
     *
     * @param factionId the faction ID.
     * @return the faction worth profile or null of not a valid faction.
     */
    private FactionWorth getOrCreateFactionWorth(String factionId) {
        // No faction worth is associated with ignored faction IDs.
        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId)) {
            return null;
        }

        return factions.computeIfAbsent(factionId.toLowerCase(), k -> {
            FactionWorth v = new FactionWorth(k, plugin.getFactionsHook().getFactionName(k));
            String allianceId = plugin.getFactionsHook().getAlliance(k);
            if (allianceId != null) {
                v.setAllianceId(allianceId);
            }

            if (plugin.getSettings().isEnabled(WorthType.PLAYER_BALANCE)) {
                List<UUID> members = plugin.getFactionsHook().getMembers(k);
                double balance = plugin.getEconomyHook().getTotalBalance(members);
                v.addWorth(WorthType.PLAYER_BALANCE, balance);
            }

            if (plugin.getSettings().isEnabled(WorthType.FACTION_BALANCE)) {
                double balance = plugin.getEconomyHook().getFactionBalance(k);
                v.addWorth(WorthType.FACTION_BALANCE, balance);
            }
            orderedFactions.add(v);
            return v;
        });
    }

    /**
     * Gets the alliance worth profile by a alliance ID.
     *
     * @param allianceId the alliance ID.
     * @return the alliance worth profile or null of not a valid faction.
     */
    private AllianceWorth getOrCreateAllianceWorth(String allianceId) {
        if (allianceId == null) {
            return null;
        }

        // No faction worth is associated with ignored faction IDs.
        if (plugin.getSettings().getIgnoredAllianceIds().contains(allianceId)) {
            return null;
        }

        return alliances.computeIfAbsent(allianceId.toLowerCase(), k -> {
            String allianceName = plugin.getFactionsHook().getAllianceName(k);
            if (allianceName != null) {
                AllianceWorth v = new AllianceWorth(k, allianceName);
                for (String factionsId : plugin.getFactionsHook().getAllianceMembers(k)) {
                    FactionWorth f = getOrCreateFactionWorth(factionsId);
                    if (f != null) {
                        v.addFaction(f);
                    }
                }
                if (plugin.getSettings().isEnabled(WorthType.ALLIANCE_BALANCE)) {
                    v.addWorth(WorthType.ALLIANCE_BALANCE, plugin.getEconomyHook().getAllianceBalance(k));
                }
                return v;
            }
            return null;
        });
    }

    /**
     * Sets a chunks worth.
     *
     * @param pos       the chunk position.
     * @param worthType the worth type.
     * @param worth     the worth value.
     */
    public void set(ChunkPos pos, WorthType worthType, double worth) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getOrCreateFactionWorth(pos);
        if (factionWorth == null) return;

        orderedFactions.remove(factionWorth);

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        double oldWorth = chunkWorth.getWorth(worthType);
        chunkWorth.setWorth(worthType, worth);
        factionWorth.addWorth(worthType, worth - oldWorth);

        // If this position was added to the recalculate queue, add all queued
        // updates while the chunk was recalculated and set the next time to
        // recalculate the chunk.
        if (recalculateQueue.contains(pos, worthType)) {
            double queuedWorth = recalculateQueue.remove(pos, worthType);
            chunkWorth.addWorth(worthType, queuedWorth);
            factionWorth.addWorth(worthType, queuedWorth);
            chunkWorth.setNextRecalculation(plugin.getSettings().getChunkRecalculateMillis() + System.currentTimeMillis());
        }

        orderedFactions.add(factionWorth);

        recalculateAlliancesWorth();
    }

    public void setMaterials(ChunkPos pos, Map<Material, Integer> materials) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getOrCreateFactionWorth(pos);
        if (factionWorth == null) return;

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        factionWorth.removeMaterials(chunkWorth.getMaterials());
        chunkWorth.setMaterials(materials);
        factionWorth.addMaterials(materials);

        // Add all back all modifications made since the update was scheduled.
        if (materialsQueue.containsRow(pos)) {
            Map<Material, Integer> queued = materialsQueue.row(pos);
            chunkWorth.addMaterials(queued);
            factionWorth.addMaterials(queued);
            queued.clear();
        }

        plugin.getPersistenceTask().queue(pos, chunkWorth);
        plugin.getPersistenceTask().queue(factionWorth);
    }

    private void setSpawners(ChunkPos pos, Map<EntityType, Integer> spawners) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getOrCreateFactionWorth(pos);
        if (factionWorth == null) return;

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        factionWorth.removeSpawners(chunkWorth.getSpawners());
        chunkWorth.setSpawners(spawners);
        factionWorth.addSpawners(spawners);
    }

    private void setSpecials(ChunkPos pos, Map<String, Integer> specials) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getOrCreateFactionWorth(pos);
        if (factionWorth == null) return;

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        factionWorth.removeSpecials(chunkWorth.getSpecials());
        chunkWorth.setSpecials(specials);
        factionWorth.addSpecials(specials);
    }

    /**
     * Adds worth to a chunk.
     *  @param chunk     the chunk.
     * @param reason    the reason.
     * @param worthType the worth type.
     * @param worth     the worth value.
     * @param specials
     */
    public void add(Chunk chunk, RecalculateReason reason, WorthType worthType, double worth,
                    Map<Material, Integer> materials, Map<EntityType, Integer> spawners, Map<String, Integer> specials) {
        // Do nothing if worth type is disabled or worth is nothing.
        if (!plugin.getSettings().isEnabled(worthType) || worth == 0) {
            return;
        }

        // Do nothing if faction worth is null.
        ChunkPos pos = ChunkPos.of(chunk);
        FactionWorth factionWorth = getOrCreateFactionWorth(pos);
        if (factionWorth == null) return;

        orderedFactions.remove(factionWorth);

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        chunkWorth.addWorth(worthType, worth);
        chunkWorth.addMaterials(materials);
        chunkWorth.addSpawners(spawners);
        chunkWorth.addSpecials(specials);

        factionWorth.addWorth(worthType, worth);
        factionWorth.addMaterials(materials);
        factionWorth.addSpawners(spawners);
        factionWorth.addSpecials(specials);

        orderedFactions.add(factionWorth);

        recalculateAlliancesWorth();

        // Add this worth to the recalculation queue if the chunk is being
        // recalculated.
        if (materialsQueue.containsRow(pos)) {
            materialsQueue.row(pos).putAll(materials);
        }

        if (recalculateQueue.contains(pos, worthType)) {
            double prev = recalculateQueue.get(pos, worthType);
            recalculateQueue.put(pos, worthType, worth + prev);
            return;
        }

        // Schedule chunk for recalculation.
        recalculate(chunkWorth, pos, chunk, reason);
    }

    /**
     * Attempts to schedule a chunk for recalculation if the chunk is allowed
     * to be recalculated at this time.
     *
     * @param chunk  the chunk.
     * @param reason the reason for recalculating.
     */
    public void recalculate(Chunk chunk, RecalculateReason reason) {
        ChunkPos pos = ChunkPos.of(chunk);
        if (getOrCreateFactionWorth(pos) == null) return;

        ChunkWorth chunkWorth = getChunkWorth(pos);
        recalculate(chunkWorth, pos, chunk, reason);
    }

    /**
     * Attempts to schedule a chunk for recalculation if the chunk is allowed
     * to be recalculated at this time.
     *
     * @param chunkWorth the worth associated with this chunk.
     * @param pos        the chunk position.
     * @param chunk      the chunk.
     * @param reason     the reason for recalculating.
     */
    private void recalculate(ChunkWorth chunkWorth, ChunkPos pos, Chunk chunk, RecalculateReason reason) {
        // Do not recalculate the chunk value if not within the recalculation period.
        if (chunkWorth.getNextRecalculation() >= System.currentTimeMillis() &&
                !plugin.getSettings().isBypassRecalculateDelay(reason) ||
                !plugin.getSettings().isPerformRecalculate(reason) ||
                plugin.getSettings().getChunkQueueSize() <= plugin.getChunkWorthTask().getQueueSize()) {
            FactionWorth factionWorth = getOrCreateFactionWorth(pos);
            if (factionWorth != null) {
                plugin.getPersistenceTask().queue(pos, chunkWorth);
                plugin.getPersistenceTask().queue(factionWorth);
            }
            return;
        }

        // Next recalculation is scheduled once the chunk worth is re-set.
        chunkWorth.setNextRecalculation(Long.MAX_VALUE);

        // Schedule this chunk to be recalculated on a separate thread.
        if (reason == RecalculateReason.UNLOAD) {
            forceRecalculate(pos, chunk);
        }

        // Occasionally block updates are not updated in the chunk on the
        // same tick, getting the chunk snapshot in the next tick fixes
        // this issue. This does not apply to chunk unloads.
        else {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    forceRecalculate(pos, chunk));
        }
    }

    private void forceRecalculate(ChunkPos pos, Chunk chunk) {
        // Clear the recalculate queue in the event of multiple block
        // changes in the same tick.
        recalculateQueue.row(pos).clear();

        // Update the chunk spawner worth on the main thread, unfortunately
        // there is no better method of doing this. Same with chests.
        Map<EntityType, Integer> spawners = new EnumMap<>(EntityType.class);
        Map<Material, Integer> materials = new EnumMap<>(Material.class);
        Map<String, Integer> specials = new HashMap<>();

        if (plugin.getSettings().isEnabled(WorthType.SPAWNER)) {
            set(pos, WorthType.SPAWNER, getSpawnerWorth(chunk, spawners));
        }

        if (plugin.getSettings().isEnabled(WorthType.CHEST)) {
            set(pos, WorthType.CHEST, getChestWorth(chunk, materials, spawners, specials));
        }

        setSpawners(pos, spawners);
        setSpecials(pos, specials);
        materialsQueue.row(pos).putAll(materials);

        plugin.getChunkWorthTask().queue(chunk.getChunkSnapshot());
    }

    /**
     * Recalculate the worth of all alliances
     */
    private void recalculateAlliancesWorth() {
        orderedAlliances.clear();
        for (AllianceWorth allianceWorth : alliances.values()) {
            allianceWorth.calculateWorth();
            orderedAlliances.add(allianceWorth);
        }
    }

    /**
     * Calculates the spawner worth of a chunk.
     *
     * @param chunk    the chunk.
     * @param spawners the spawner totals to add to.
     * @return the chunk worth in spawners.
     */
    private double getSpawnerWorth(Chunk chunk, Map<EntityType, Integer> spawners) {
        int count;
        double worth = 0;

        for (BlockState blockState : chunk.getTileEntities()) {
            if (!(blockState instanceof CreatureSpawner)) {
                continue;
            }

            CreatureSpawner spawner = (CreatureSpawner) blockState;
            EntityType spawnType = spawner.getSpawnedType();
            int stackSize = plugin.getSpawnerStackerHook().getStackSize(spawner);
            double blockPrice = plugin.getSettings().getSpawnerPrice(spawnType) * stackSize;
            worth += blockPrice;

            if (blockPrice != 0) {
                count = spawners.getOrDefault(spawnType, 0);
                spawners.put(spawnType, count + stackSize);
            }
        }

        return worth;
    }

    /**
     * Calculates the chest worth of a chunk.
     *
     * @param chunk     the chunk.
     * @param materials the material totals to add to.
     * @param spawners  the spawner totals to add to.
     * @param specials  the special item totals to add to.
     * @return the chunk worth in materials.
     */
    private double getChestWorth(Chunk chunk, Map<Material, Integer> materials, Map<EntityType, Integer> spawners, Map<String, Integer> specials) {
        double worth = 0;

        for (BlockState blockState : chunk.getTileEntities()) {
            if (!(blockState instanceof InventoryHolder)) {
                continue;
            }

            ChestWorth chestWorth = getWorth(((InventoryHolder) blockState).getInventory());
            worth += chestWorth.getTotalWorth();
            GenericUtils.addCountMap(materials, chestWorth.getMaterials());
            GenericUtils.addCountMap(spawners, chestWorth.getSpawners());
            GenericUtils.addCountMap(specials, chestWorth.getSpecials());
        }

        return worth;
    }

    public ChestWorth getWorth(Inventory inventory) {
        return getWorth(inventory, false);
    }

    public ChestWorth getWorth(Inventory inventory, boolean negate) {
        int mult = negate ? -1 : 1;
        double worth = 0;
        Map<Material, Integer> materials = new HashMap<>();
        Map<EntityType, Integer> spawners = new HashMap<>();
        Map<String, Integer> specials = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            ItemMatcher specialMatcher = plugin.getSettings().getMatchingSpecial(item);
            if (specialMatcher != null) {
                worth += specialMatcher.getWorth() * item.getAmount() * mult;

                int count = specials.getOrDefault(specialMatcher.getName(), 0);
                specials.put(specialMatcher.getName(), count + item.getAmount() * mult);
                continue;
            }

            if (item.getType() == Material.MOB_SPAWNER) {
                int stackSize = plugin.getSpawnerStackerHook().getStackSize(item);
                EntityType spawnerType = plugin.getSpawnerStackerHook().getSpawnedType(item);
                worth += plugin.getSettings().getSpawnerPrice(spawnerType) * item.getAmount() * stackSize * mult;

                int count = spawners.getOrDefault(spawnerType, 0);
                spawners.put(spawnerType, count + (item.getAmount() * stackSize * mult));
                continue;
            }

            worth += plugin.getSettings().getBlockPrice(item.getType()) * item.getAmount() * mult;
            int count = materials.getOrDefault(item.getType(), 0);
            materials.put(item.getType(), count + item.getAmount() * mult);
        }

        return new ChestWorth(worth, materials, spawners, specials);
    }

    /**
     * Updates the factions worth once the faction has claimed or unclaimed territory.
     *
     * @param factionId the faction ID.
     * @param claims    the claims affected in this transaction.
     * @param unclaimed true if the claims were unclaimed.
     */
    public void update(String factionId, Collection<ChunkPos> claims, boolean unclaimed) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getOrCreateFactionWorth(factionId);
        if (factionWorth == null) return;

        orderedFactions.remove(factionWorth);

        // Add all placed and chest worth of each claim to the faction.
        for (ChunkPos pos : claims) {
            Chunk chunk = pos.getChunk(plugin.getServer());
            if (chunk == null) continue;

            ChunkWorth chunkWorth = getChunkWorth(pos);
            for (WorthType worthType : WorthType.getPlaced()) {
                double worth = chunkWorth.getWorth(worthType);
                factionWorth.addWorth(worthType, unclaimed ? -worth : worth);
            }

            if (unclaimed) {
                factionWorth.removeMaterials(chunkWorth.getMaterials());
                factionWorth.removeSpawners(chunkWorth.getSpawners());
                factionWorth.removeSpecials(chunkWorth.getSpecials());
            } else {
                factionWorth.addMaterials(chunkWorth.getMaterials());
                factionWorth.addSpawners(chunkWorth.getSpawners());
                factionWorth.addSpecials(chunkWorth.getSpecials());
            }

            // Schedule chunk for recalculation.
            recalculate(chunkWorth, pos, chunk, RecalculateReason.CLAIM);
        }

        orderedFactions.add(factionWorth);

        recalculateAlliancesWorth();
    }

    /**
     * Adds to a faction worth.
     *
     * @param factionId the faction ID.
     * @param worthType the worth type.
     * @param worth     the worth to add.
     */
    public void add(String factionId, WorthType worthType, double worth) {
        // Do nothing if the worth type is placed or disabled or worth is equal to nothing.
        if (WorthType.isPlaced(worthType) || !plugin.getSettings().isEnabled(worthType) || worth == 0) {
            return;
        }

        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getOrCreateFactionWorth(factionId);
        if (factionWorth == null) return;

        // Update faction with the new worth and adjust the worth position.
        orderedFactions.remove(factionWorth);
        factionWorth.addWorth(worthType, worth);
        orderedFactions.add(factionWorth);
        plugin.getPersistenceTask().queue(factionWorth);

        recalculateAlliancesWorth();
    }

    /**
     * Adds to a n alliance worth.
     *
     * @param allianceId    the alliance ID.
     * @param worthType     the worth type.
     * @param worth         the worth to add.
     */
    public void addToAlliance(String allianceId, WorthType worthType, double worth) {
        // Do nothing if the worth type is placed or disabled or worth is equal to nothing.
        if (WorthType.isPlaced(worthType) || !plugin.getSettings().isEnabled(worthType) || worth == 0) {
            return;
        }

        // Do nothing if faction worth is null.
        AllianceWorth allianceWorth = getOrCreateAllianceWorth(allianceId);
        if (allianceWorth == null) return;

        // Update alliance with the new worth and adjust the worth position.
        orderedAlliances.remove(allianceWorth);
        allianceWorth.addWorth(worthType, worth);
        orderedAlliances.add(allianceWorth);
    }

    /**
     * Renames a listed faction.
     *
     * @param factionId the faction ID.
     * @param newName   the new faction name.
     */
    public void rename(String factionId, String newName) {
        FactionWorth factionWorth = getWorth(factionId);

        if (factionWorth != null) {
            factionWorth.setName(newName);
            plugin.getPersistenceTask().queue(factionWorth);
        }
    }

    /**
     * Renames a listed faction.
     *
     * @param allianceId the faction ID.
     * @param newName    the new faction name.
     */
    public void renameAlliance(String allianceId, String newName) {
        AllianceWorth allianceWorth = getAllianceWorth(allianceId);

        if (allianceWorth != null) {
            allianceWorth.setName(newName);
        }
    }

    /**
     * Removes a faction from the list.
     *
     * @param factionId the ID of the faction to remove.
     */
    public void remove(String factionId) {
        FactionWorth factionWorth = factions.remove(factionId.toLowerCase());
        orderedFactions.remove(factionWorth);
        removeFromAlliances(factionWorth);
        plugin.getPersistenceTask().queueDeletedFaction(factionId);
    }

    /**
     * Removes an alliance from the list.
     *
     * @param allianceId the ID of the alliance to remove.
     */
    public void removeAlliance(String allianceId) {
        AllianceWorth allianceWorth = alliances.remove(allianceId.toLowerCase());
        orderedAlliances.remove(allianceWorth);
    }

    /**
     * Add a faction into an alliance
     * @param allianceId the ID of the alliance to add to
     * @param factionId  the ID of the faction to add to the alliance
     */
    public void addToAlliance(String allianceId, String factionId) {
        AllianceWorth allianceWorth = getOrCreateAllianceWorth(allianceId);
        FactionWorth factionWorth = getWorth(factionId);
        if (factionWorth != null) {
            orderedAlliances.remove(allianceWorth);
            allianceWorth.addFaction(factionWorth);
            orderedAlliances.add(allianceWorth);
        }
    }

    /**
     * Remove a faction from an alliance
     * @param allianceId the ID of the alliance to remove from
     * @param factionId  the ID of the faction to remove from the alliance
     */
    public void removeFromAlliance(String allianceId, String factionId) {
        AllianceWorth allianceWorth = getOrCreateAllianceWorth(allianceId);
        FactionWorth factionWorth = getWorth(factionId);
        if (factionWorth != null) {
            orderedAlliances.remove(allianceWorth);
            allianceWorth.removeFaction(factionWorth);
            orderedAlliances.add(allianceWorth);
        }
    }

    private void removeFromAlliances(FactionWorth factionWorth) {
        for (AllianceWorth allianceWorth : alliances.values()) {
            orderedAlliances.remove(allianceWorth);
            allianceWorth.removeFaction(factionWorth);
            orderedAlliances.add(allianceWorth);
        }
    }
}
