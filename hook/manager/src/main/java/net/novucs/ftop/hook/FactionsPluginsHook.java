package net.novucs.ftop.hook;

import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * This class collects common functionality between all real factions plugins
 * This is mainly related to the pass-through nature of faction alliances
 * but also due to their shared economy account naming scheme
 */
public abstract class FactionsPluginsHook extends FactionsHook {

    public FactionsPluginsHook(Plugin plugin) {
        super(plugin);
    }


    @Override
    public String getAllianceName(String allianceId) {
        return getFactionName(allianceId);
    }

    @Override
    public String getAlliance(String factionId) {
        return factionId;
    }

    @Override
    public String getAllianceOwnerName(String allianceId) {
        return getOwnerName(allianceId);
    }

    @Override
    public Set<String> getAllianceIds() {
        return getFactionIds();
    }

    @Override
    public String getEssentialsAccount(String factionId) {
        return "faction_" + factionId.replace("-", "_");
    }

    @Override
    public String getAllianceEssentialsAccount(String allianceId) {
        return "alliances-dont-have-accounts";
    }

    @Override
    public String getVaultAccount(String factionId) {
        return "faction-" + factionId;
    }

    @Override
    public String getAllianceVaultAccount(String allianceId) {
        return "alliances-dont-have-accounts";
    }
}
