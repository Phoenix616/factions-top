package net.novucs.ftop.hook;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.hook.event.AllianceDisbandEvent;
import net.novucs.ftop.hook.event.AllianceJoinEvent;
import net.novucs.ftop.hook.event.AllianceLeaveEvent;
import net.novucs.ftop.hook.event.AllianceRenameEvent;
import net.novucs.ftop.hook.event.FactionClaimEvent;
import net.novucs.ftop.hook.event.FactionDisbandEvent;
import net.novucs.ftop.hook.event.FactionJoinEvent;
import net.novucs.ftop.hook.event.FactionLeaveEvent;
import net.novucs.ftop.hook.event.FactionRenameEvent;
import net.redstoneore.legacyfactions.FLocation;
import net.redstoneore.legacyfactions.Relation;
import net.redstoneore.legacyfactions.entity.Board;
import net.redstoneore.legacyfactions.entity.FPlayer;
import net.redstoneore.legacyfactions.entity.FPlayerColl;
import net.redstoneore.legacyfactions.entity.Faction;
import net.redstoneore.legacyfactions.entity.FactionColl;
import net.redstoneore.legacyfactions.event.EventFactionsChange;
import net.redstoneore.legacyfactions.event.EventFactionsDisband;
import net.redstoneore.legacyfactions.event.EventFactionsLandChange;
import net.redstoneore.legacyfactions.event.EventFactionsNameChange;
import net.redstoneore.legacyfactions.event.EventFactionsRelationChange;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LegacyFactions0103 extends FactionsHook {

    private final Set<ChunkPos> recentlyClaimedChunks = new HashSet<>();

    public LegacyFactions0103(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        Faction faction = Board.get().getFactionAt(new FLocation(worldName, chunkX, chunkZ));
        return faction.getId();
    }

    @Override
    public void initialize() {
        getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), recentlyClaimedChunks::clear, 1, 1);
        super.initialize();
    }

    @Override
    public String getFaction(Player player) {
        return FPlayerColl.get(player).getFaction().getId();
    }

    @Override
    public String getFactionName(String factionId) {
        return FactionColl.get().getFactionById(factionId).getTag();
    }

    @Override
    public boolean isFaction(String factionId) {
        return FactionColl.get().getFactionById(factionId) != null;
    }
    
    @Override
    public String getAlliance(String factionId) {
        return factionId;
    }
    
    @Override
    public String getAllianceName(String allianceId) {
        return getFactionName(allianceId);
    }
    
    @Override
    public List<String> getAllianceMembers(String allianceId) {
        Faction faction = FactionColl.get().getFactionById(allianceId);
        List<String> allianceMembers = new ArrayList<>();
        if (faction != null) {
            allianceMembers.add(faction.getId());
            for (Faction f : FactionColl.get().getAllFactions()) {
                if (f != faction && faction.getRelationTo(f) == Relation.ALLY) {
                    allianceMembers.add(f.getId());
                }
            }
        }
        return allianceMembers;
    }
    
    @Override
    public ChatColor getRelation(Player player, String factionId) {
        FPlayer fplayer = FPlayerColl.get(player);
        Faction faction = FactionColl.get().getFactionById(factionId);
        return fplayer.getFaction().getRelationTo(faction).getColor();
    }
    
    @Override
    public ChatColor getRelation(String factionId, String allianceId) {
        Faction faction = FactionColl.get().getFactionById(factionId);
        Faction otherFaction = FactionColl.get().getFactionById(allianceId);
        return faction.getRelationTo(otherFaction).getColor();
    }
    
    @Override
    public String getOwnerName(String factionId) {
        Faction faction = FactionColl.get().getFactionById(factionId);

        if (faction == null) {
            return null;
        }

        FPlayer owner = faction.getOwner();
        return owner == null ? null : owner.getName();
    }
    
    @Override
    public String getAllianceOwnerName(String allianceId) {
        return getOwnerName(allianceId);
    }

    @Override
    public List<UUID> getMembers(String factionId) {
        return FactionColl.get().getFactionById(factionId).getFPlayers().stream()
                .map(fplayer -> UUID.fromString(fplayer.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChunkPos> getClaims() {
        List<ChunkPos> target = new LinkedList<>();
        target.addAll(getChunkPos(Board.get().getAllClaims()));
        return target;
    }

    @Override
    public Set<String> getFactionIds() {
        return FactionColl.all().stream()
                .map(Faction::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public String getEssentialsEconomyAccount(String factionId) {
        return "faction_" + factionId.replace("-", "_");
    }

    @Override
    public String getVaultEconomyAccount(String factionId) {
        return "faction-" + factionId;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisband(EventFactionsDisband event) {
        String factionId = event.getFaction().getId();
        String factionName = event.getFaction().getTag();
        callEvent(new FactionDisbandEvent(factionId, factionName));
        callEvent(new AllianceDisbandEvent(factionId, factionName));
        for (Faction f : FactionColl.get().getAllFactions()) {
            if (f != event.getFaction() && event.getFaction().getRelationTo(f) == Relation.ALLY) {
                callEvent(new AllianceLeaveEvent(f.getId(), factionId));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRelationChange(EventFactionsRelationChange event) {
        if (event.getCurrentRelation() == event.getTargetRelation()) {
            return;
        }
        
        if (event.getTargetRelation() == Relation.ALLY) {
            callEvent(new AllianceJoinEvent(event.getFaction().getId(), event.getTargetFaction().getId()));
            callEvent(new AllianceJoinEvent(event.getTargetFaction().getId(), event.getFaction().getId()));
        } else if (event.getCurrentRelation() == Relation.ALLY) {
            callEvent(new AllianceLeaveEvent(event.getFaction().getId(), event.getTargetFaction().getId()));
            callEvent(new AllianceLeaveEvent(event.getTargetFaction().getId(), event.getFaction().getId()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRename(EventFactionsNameChange event) {
        String factionId = event.getFaction().getId();
        String oldName = event.getFaction().getTag();
        String newName = event.getFactionTag();
        callEvent(new FactionRenameEvent(factionId, oldName, newName));
        callEvent(new AllianceRenameEvent(factionId, oldName, newName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaim(EventFactionsLandChange event) {
        String factionId = event.getFPlayer().getFactionId();

        if (event.getCause() == EventFactionsLandChange.LandChangeCause.Claim) {
            Multimap<String, ChunkPos> claims = HashMultimap.create();

            for (Map.Entry<FLocation, Faction> transaction : event.getTransactions().entrySet()) {
                String oldFactionId = Board.get().getFactionAt(transaction.getKey()).getId();
                ChunkPos pos = getChunkPos(transaction.getKey());

                if (recentlyClaimedChunks.contains(pos)) {
                    continue;
                }

                recentlyClaimedChunks.add(pos);
                claims.put(oldFactionId, pos);
            }

            callEvent(new FactionClaimEvent(factionId, claims));
        } else {
            Multimap<String, ChunkPos> claims = HashMultimap.create();

            for (Map.Entry<FLocation, Faction> transaction : event.getTransactions().entrySet()) {
                claims.put(factionId, getChunkPos(transaction.getKey()));
            }

            String newFactionId = FactionColl.get().getWilderness().getId();
            callEvent(new FactionClaimEvent(newFactionId, claims));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangeFaction(EventFactionsChange event) {
        Player player = event.getFPlayer().getPlayer();
        callEvent(new FactionJoinEvent(event.getFactionNew().getId(), player));
        callEvent(new FactionLeaveEvent(event.getFactionOld().getId(), player));
    }

    private Set<ChunkPos> getChunkPos(Set<FLocation> locations) {
        return locations.stream().map(this::getChunkPos).collect(Collectors.toSet());
    }

    private ChunkPos getChunkPos(FLocation location) {
        return ChunkPos.of(location.getWorldName(), (int) location.getX(), (int) location.getZ());
    }
}
