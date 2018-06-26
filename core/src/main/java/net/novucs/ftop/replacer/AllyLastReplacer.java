package net.novucs.ftop.replacer;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.Worth;
import net.novucs.ftop.util.SplaySet;

import java.util.function.Supplier;

public class AllyLastReplacer implements Supplier<String> {

    private final FactionsTopPlugin plugin;

    public AllyLastReplacer(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String get() {
        SplaySet<Worth> alliances = plugin.getWorthManager().getOrderedAlliances();

        if (alliances.isEmpty()) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }

        return alliances.last().getName();
    }
}
