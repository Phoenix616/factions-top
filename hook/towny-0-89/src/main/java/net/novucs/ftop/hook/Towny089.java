package net.novucs.ftop.hook;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.palmergames.bukkit.towny.event.RenameNationEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.util.StringMgmt;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.hook.event.AllianceDisbandEvent;
import net.novucs.ftop.hook.event.AllianceJoinEvent;
import net.novucs.ftop.hook.event.AllianceLeaveEvent;
import net.novucs.ftop.hook.event.FactionClaimEvent;
import net.novucs.ftop.hook.event.FactionDisbandEvent;
import net.novucs.ftop.hook.event.FactionJoinEvent;
import net.novucs.ftop.hook.event.FactionLeaveEvent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Towny089 extends FactionsHook {

    private final static int[][] CHUNK_MOD = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};
    private static final String TOWN_ECONOMY_PREFIX = TownySettings.getTownAccountPrefix();
    private static final String NATION_ECONOMY_PREFIX = TownySettings.getNationAccountPrefix();

    private final Map<String, UUID> uuidCache = new HashMap<>();
    private final Set<ChunkPos> recentClaims = new HashSet<>(); // Chunks claimed in this tick

    public Towny089(Plugin plugin) {
        super(plugin);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            recentClaims.clear();
        }, 1, 1);
    }

    private UUID getUuid(String playerName) {
        return uuidCache.compute(playerName.toLowerCase(), (name, uuid) -> {
            if (uuid == null) {
                OfflinePlayer player = getPlugin().getServer().getOfflinePlayer(name);
                if (player != null && player.hasPlayedBefore()) {
                    return player.getUniqueId();
                }
            }
            return uuid;
        });
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        // loop through all 4 corners of the chunk to check for a town
        for (int[] mod : CHUNK_MOD) {
            try {
                return new WorldCoord(worldName, chunkX * 16 - 16 * mod[0], chunkZ * 16 - 16 * mod[1])
                        .getTownBlock().getTown().getName();
            } catch (NotRegisteredException ignored) {} // no town
        }
        return "none";
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public String getFaction(Player player) {
        try {
            return TownyUniverse.getDataSource().getResident(player.getName()).getTown().getName();
        } catch (NotRegisteredException ignored) {} // not found
        return "none";
    }

    @Override
    public String getFactionName(String factionId) {
        try {
            return TownyUniverse.getDataSource().getTown(factionId).getName();
        } catch (NotRegisteredException ignored) {} // not found
        return factionId;
    }

    @Override
    public boolean isFaction(String factionId) {
        return TownyUniverse.getDataSource().hasTown(factionId);
    }

    @Override
    public String getAlliance(String factionId) {
        try {
            return TownyUniverse.getDataSource().getTown(factionId).getNation().getName();
        } catch (NotRegisteredException ignored) {} // not found
        return null;
    }

    @Override
    public String getAllianceName(String allianceId) {
        try {
            return TownyUniverse.getDataSource().getNation(allianceId).getName();
        } catch (NotRegisteredException ignored) {} // not found
        return null;
    }

    @Override
    public ChatColor getRelation(Player player, String factionId) {
        try {
            Town residentTown = TownyUniverse.getDataSource().getResident(player.getName()).getTown();
            Town town = TownyUniverse.getDataSource().getTown(factionId);
            return residentTown.equals(town) ? ChatColor.DARK_GREEN : getRelation(residentTown.getName(), town.getNation().getName());
        } catch (NotRegisteredException ignored) {} // not found
        return ChatColor.WHITE;
    }

    @Override
    public ChatColor getRelation(String factionId, String allianceId) {
        try {
            Town town = TownyUniverse.getDataSource().getTown(factionId);
            Nation nation = TownyUniverse.getDataSource().getNation(allianceId);
            return nation.hasTown(town)
                    ? ChatColor.DARK_GREEN
                    : nation.hasAlly(town.getNation())
                            ? ChatColor.GREEN
                            : nation.hasEnemy(town.getNation())
                                    ? ChatColor.RED
                                    : ChatColor.WHITE;
        } catch (NotRegisteredException ignored) {} // not found
        return ChatColor.WHITE;
    }

    @Override
    public String getOwnerName(String factionId) {
        try {
            return TownyUniverse.getDataSource().getTown(factionId).getMayor().getName();
        } catch (NotRegisteredException ignored) {} // not found
        return null;
    }

    @Override
    public String getAllianceOwnerName(String allianceId) {
        try {
            return TownyUniverse.getDataSource().getNation(allianceId).getCapital().getMayor().getName();
        } catch (NotRegisteredException ignored) {} // not found
        return null;
    }

    @Override
    public List<UUID> getMembers(String factionId) {
        try {
            return TownyUniverse.getDataSource().getTown(factionId).getResidents()
                    .stream().map(r -> getUuid(r.getName())).collect(Collectors.toList());
        } catch (NotRegisteredException ignored) {} // not found
        return new ArrayList<>();
    }

    @Override
    public List<String> getAllianceMembers(String allianceId) {
        try {
            return TownyUniverse.getDataSource().getNation(allianceId).getTowns()
                    .stream().map(Town::getName).collect(Collectors.toList());
        } catch (NotRegisteredException ignored) {} // not found
        return new ArrayList<>();
    }

    @Override
    public List<ChunkPos> getClaims() {
        return TownyUniverse.getDataSource().getAllTownBlocks()
                .stream().map(this::getChunkPos).distinct().collect(Collectors.toList());
    }

    @Override
    public Set<String> getFactionIds() {
        return TownyUniverse.getDataSource().getTownsKeys();
    }

    @Override
    public Set<String> getAllianceIds() {
        return TownyUniverse.getDataSource().getNationsKeys();
    }

    @Override
    public String getEssentialsAccount(String factionId) {
        return getEconomyAccount(factionId);
    }

    @Override
    public String getAllianceEssentialsAccount(String allianceId) {
        return getNationEconomyAccount(allianceId);
    }

    @Override
    public String getVaultAccount(String factionId) {
        return getEconomyAccount(factionId);
    }

    @Override
    public String getAllianceVaultAccount(String allianceId) {
        return getNationEconomyAccount(allianceId);
    }

    private String getEconomyAccount(String factionId) {
        return StringMgmt.trimMaxLength(TOWN_ECONOMY_PREFIX + factionId, 32);
    }

    private String getNationEconomyAccount(String allianceId) {
        return StringMgmt.trimMaxLength(NATION_ECONOMY_PREFIX + allianceId, 32);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownDelete(DeleteTownEvent event) {
        String townName = event.getTownName();
        callEvent(new FactionDisbandEvent(townName, townName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationDelete(DeleteNationEvent event) {
        String nationName = event.getNationName();
        callEvent(new AllianceDisbandEvent(nationName, nationName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationAddTown(NationAddTownEvent event) {
        callEvent(new AllianceJoinEvent(event.getNation().getName(), event.getTown().getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNewNation(NewNationEvent event) {
        callEvent(new AllianceJoinEvent(event.getNation().getName(), event.getNation().getCapital().getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNationRemoveTown(NationRemoveTownEvent event) {
        callEvent(new AllianceLeaveEvent(event.getNation().getName(), event.getTown().getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRenameTown(RenameTownEvent event) {
        // Towny has no internal identifier for their towns...
        // Changing the name completely changes by what it identifies the town.
        // So we have to destroy the old one and create a new one ;_;
        String newName = event.getTown().getName();
        String oldName = event.getOldName();
        callEvent(new FactionDisbandEvent(oldName, oldName));
        for (Resident resident : event.getTown().getResidents()) {
            try {
                callEvent(new FactionJoinEvent(newName, TownyUniverse.getPlayer(resident)));
            } catch (TownyException ignored) {
                // player not found, cannot calculate worth of offline player without redesigning large parts of the plugin
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRenameTown(RenameNationEvent event) {
        // Towny has no internal identifier for their nations...
        // Changing the name completely changes by what it identifies the nations.
        // So we have to destroy the old one and create a new one ;_;
        String newName = event.getNation().getName();
        String oldName = event.getOldName();
        callEvent(new AllianceDisbandEvent(oldName, oldName));
        for (Town town : event.getNation().getTowns()) {
            callEvent(new AllianceJoinEvent(newName, town.getName()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaim(TownClaimEvent event) {
        try {
            String townName = event.getTownBlock().getTown().getName();
            ChunkPos chunkPos = getChunkPos(event.getTownBlock());

            if (recentClaims.contains(chunkPos)) { // don't call event multiple times for the same chunk
                return;
            }
            recentClaims.add(chunkPos);

            Multimap<String, ChunkPos> claims = HashMultimap.create();
            claims.put(townName, chunkPos);
            callEvent(new FactionClaimEvent(townName, claims));
        } catch (NotRegisteredException ignored) {} // why would it ever claim without a town?
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUnclaim(TownUnclaimEvent event) {
        ChunkPos chunkPos = getChunkPos(event.getWorldCoord());
        if (recentClaims.contains(chunkPos)) {  // don't call event multiple times for the same chunk
            return;
        }
        recentClaims.add(chunkPos);
        Multimap<String, ChunkPos> claims = HashMultimap.create();
        claims.put(event.getTown().getName(), chunkPos);
        callEvent(new FactionClaimEvent("none", claims));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(TownAddResidentEvent event) {
        try {
            Player player = TownyUniverse.getPlayer(event.getResident());
            String townName = event.getTown().getName();
            callEvent(new FactionJoinEvent(townName, player));
        } catch (TownyException ignored) {} // why would someone join a town when offline
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(TownRemoveResidentEvent event) {
        try {
            Player player = TownyUniverse.getPlayer(event.getResident());
            String townName = event.getTown().getName();
            callEvent(new FactionLeaveEvent(townName, player));
        } catch (TownyException ignored) {} // why would someone join a town when offline
    }

    private ChunkPos getChunkPos(TownBlock block) {
        return ChunkPos.of(block.getWorld().getName(), block.getX() >> 4, block.getZ() >> 4);
    }

    private ChunkPos getChunkPos(WorldCoord coord) {
        return ChunkPos.of(coord.getWorldName(), coord.getX() >> 4, coord.getZ() >> 4);
    }
}
