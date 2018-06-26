package net.novucs.ftop.replacer;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.AllianceWorth;
import net.novucs.ftop.entity.FactionWorth;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class AllyPlayerReplacer implements Function<Player, String> {

    private final FactionsTopPlugin plugin;

    public AllyPlayerReplacer(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String apply(Player player) {
        String faction = plugin.getFactionsHook().getFaction(player);
        if (faction == null) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }

        FactionWorth factionWorth = plugin.getWorthManager().getWorth(faction);
        if (factionWorth == null || factionWorth.getAllianceId() == null) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }
    
        AllianceWorth allianceWorth = plugin.getWorthManager().getAllianceWorth(factionWorth.getAllianceId());
        if (allianceWorth == null) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }

        int rank = plugin.getWorthManager().getOrderedAlliances().indexOf(allianceWorth) + 1;
        if (rank <= 0) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }

        return Integer.toString(rank);
    }
}
