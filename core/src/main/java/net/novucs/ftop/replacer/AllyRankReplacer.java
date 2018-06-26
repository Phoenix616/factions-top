package net.novucs.ftop.replacer;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.Worth;
import net.novucs.ftop.util.SplaySet;

import java.util.function.Function;

public class AllyRankReplacer implements Function<Integer, String> {

    private final FactionsTopPlugin plugin;

    public AllyRankReplacer(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String apply(Integer rank) {
        SplaySet<Worth> alliances = plugin.getWorthManager().getOrderedAlliances();

        if (rank > 0 && alliances.size() >= rank) {
            return alliances.byIndex(rank - 1).getName();
        }

        return plugin.getSettings().getPlaceholdersFactionNotFound();
    }
}
