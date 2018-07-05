package net.novucs.ftop.hook;

import net.novucs.ftop.PluginService;
import net.novucs.ftop.entity.ChunkPos;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class FactionsHook implements Listener, PluginService {

    private final Plugin plugin;

    public FactionsHook(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    public String getFactionAt(ChunkPos pos) {
        return getFactionAt(pos.getWorld(), pos.getX(), pos.getZ());
    }

    public String getFactionAt(Block block) {
        return getFactionAt(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
    }

    public abstract String getFactionAt(String worldName, int chunkX, int chunkZ);

    public abstract String getFaction(Player player);

    public abstract String getFactionName(String factionId);

    public abstract boolean isFaction(String factionId);

    public abstract String getAlliance(String factionId);

    public abstract String getAllianceName(String allianceId);

    public abstract List<String> getAllianceMembers(String allianceId);

    public abstract ChatColor getRelation(Player player, String factionId);

    public abstract ChatColor getRelation(String factionId, String allianceId);

    public abstract String getOwnerName(String factionId);

    public abstract String getAllianceOwnerName(String allianceId);

    public abstract List<UUID> getMembers(String factionId);

    public abstract List<ChunkPos> getClaims();

    public abstract Set<String> getFactionIds();

    public abstract Set<String> getAllianceIds();

    public abstract String getEssentialsAccount(String factionId);

    public abstract String getAllianceEssentialsAccount(String allianceId);

    public abstract String getVaultAccount(String factionId);

    public abstract String getAllianceVaultAccount(String allianceId);

    void callEvent(Event event) {
        if (plugin.getServer().isPrimaryThread()) {
            plugin.getServer().getPluginManager().callEvent(event);
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().getPluginManager().callEvent(event));
    }
}
