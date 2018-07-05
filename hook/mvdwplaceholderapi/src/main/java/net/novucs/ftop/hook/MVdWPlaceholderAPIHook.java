package net.novucs.ftop.hook;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import net.novucs.ftop.hook.replacer.LastReplacer;
import net.novucs.ftop.hook.replacer.PlayerReplacer;
import net.novucs.ftop.hook.replacer.RankReplacer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MVdWPlaceholderAPIHook implements PlaceholderHook {

    private final Plugin plugin;
    private final Function<Player, String> playerReplacer;
    private final Function<Integer, String> rankReplacer;
    private final Supplier<String> lastReplacer;
    private final Function<Player, String> allyPlayerReplacer;
    private final Function<Integer, String> allyRankReplacer;
    private final Supplier<String> allyLastReplacer;
    
    public MVdWPlaceholderAPIHook(Plugin plugin,
                                  Function<Player, String> playerReplacer,
                                  Function<Integer, String> rankReplacer,
                                  Supplier<String> lastReplacer,
                                  Function<Player, String> allyPlayerReplacer,
                                  Function<Integer, String> allyRankReplacer,
                                  Supplier<String> allyLastReplacer) {
        this.plugin = plugin;
        this.playerReplacer = playerReplacer;
        this.rankReplacer = rankReplacer;
        this.lastReplacer = lastReplacer;
        this.allyPlayerReplacer = allyPlayerReplacer;
        this.allyRankReplacer = allyRankReplacer;
        this.allyLastReplacer = allyLastReplacer;
    }

    @Override
    public boolean initialize(List<Integer> enabledRanks) {
        String prefix = plugin.getName().toLowerCase() + "_";

        LastReplacer lastReplacer = new LastReplacer(this.lastReplacer);
        boolean updated = PlaceholderAPI.registerPlaceholder(plugin, prefix + "name:last", lastReplacer);

        for (int rank : enabledRanks) {
            RankReplacer replacer = new RankReplacer(rankReplacer, rank);
            if (PlaceholderAPI.registerPlaceholder(plugin, prefix + "name:" + rank, replacer)) {
                updated = true;
            }
        }

        PlayerReplacer playerReplacer = new PlayerReplacer(this.playerReplacer);
        if (PlaceholderAPI.registerPlaceholder(plugin, prefix + "rank:player", playerReplacer)) {
            updated = true;
        }

        LastReplacer allyLastReplacer = new LastReplacer(this.allyLastReplacer);
        if (PlaceholderAPI.registerPlaceholder(plugin, prefix + "ally_name:last", allyLastReplacer)) {
            updated = true;
        }
    
        for (int rank : enabledRanks) {
            RankReplacer replacer = new RankReplacer(allyRankReplacer, rank);
            if (PlaceholderAPI.registerPlaceholder(plugin, prefix + "ally_name:" + rank, replacer)) {
                updated = true;
            }
        }

        PlayerReplacer allyPlayerReplacer = new PlayerReplacer(this.allyPlayerReplacer);
        if (PlaceholderAPI.registerPlaceholder(plugin, prefix + "ally_rank:player", allyPlayerReplacer)) {
            updated = true;
        }

        return updated;
    }
}
