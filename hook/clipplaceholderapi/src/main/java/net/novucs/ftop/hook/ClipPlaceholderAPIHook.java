package net.novucs.ftop.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClipPlaceholderAPIHook implements PlaceholderHook {

    private final Plugin plugin;
    private final Function<Player, String> playerReplacer;
    private final Function<Integer, String> rankReplacer;
    private final Supplier<String> lastReplacer;
    private final Function<Player, String> allyPlayerReplacer;
    private final Function<Integer, String> allyRankReplacer;
    private final Supplier<String> allyLastReplacer;

    public ClipPlaceholderAPIHook(Plugin plugin,
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
        return PlaceholderAPI.registerPlaceholderHook(plugin.getName().toLowerCase(), new me.clip.placeholderapi.PlaceholderHook() {
            @Override
            public String onPlaceholderRequest(Player player, String identifier) {
                if ("name:last".equals(identifier)) {
                    return lastReplacer.get();
                } else  if ("nation_name:last".equals(identifier)) {
                    return allyLastReplacer.get();
                } else if ("rank:player".equals(identifier)) {
                    return playerReplacer.apply(player);
                } else if ("nation_rank:player".equals(identifier)) {
                    return allyPlayerReplacer.apply(player);
                } else if (identifier.startsWith("name:")) {
                    String[] split = identifier.split(":");
                    if (split.length > 1) {
                        try {
                            int rank = Integer.parseInt(split[1]);
                            return rankReplacer.apply(rank);
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (identifier.startsWith("nation_name:")) {
                    String[] split = identifier.split(":");
                    if (split.length > 1) {
                        try {
                            int rank = Integer.parseInt(split[1]);
                            return allyRankReplacer.apply(rank);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                return null;
            }
        });
    }
}
