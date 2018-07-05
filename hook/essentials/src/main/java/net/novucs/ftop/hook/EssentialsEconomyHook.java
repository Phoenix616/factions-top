package net.novucs.ftop.hook;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.Economy;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.novucs.ftop.hook.event.AllianceEconomyEvent;
import net.novucs.ftop.hook.event.FactionEconomyEvent;
import net.novucs.ftop.hook.event.PlayerEconomyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssentialsEconomyHook implements EconomyHook, Listener {

    private final Plugin plugin;
    private final FactionsHook factionsHook;
    private final Pattern economyAccountPattern;
    private final Pattern allianceEconomyAccountPattern;
    private boolean playerEnabled;
    private boolean factionEnabled;
    private boolean alliancesEnabled;
    private IEssentials essentials = null;

    public EssentialsEconomyHook(Plugin plugin, FactionsHook factionsHook) {
        this.plugin = plugin;
        this.factionsHook = factionsHook;
        this.economyAccountPattern = Pattern.compile(Pattern.quote(factionsHook.getEssentialsAccount("thisisatestfaction"))
                .replace("-", "_").replace("thisisatestfaction", "\\E(.*)\\Q"));
        this.allianceEconomyAccountPattern = Pattern.compile(Pattern.quote(factionsHook.getAllianceEssentialsAccount("thisisatestalliance"))
                .replace("-", "_").replace("thisisatestalliance", "\\E(.*)\\Q"));
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        essentials = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void setPlayerEnabled(boolean enabled) {
        playerEnabled = enabled;
    }

    @Override
    public void setFactionEnabled(boolean enabled) {
        factionEnabled = enabled;
    }

    @Override
    public void setAlliancesEnabled(boolean enabled) {
        alliancesEnabled = enabled;
    }

    @Override
    public double getBalance(Player player) {
        try {
            return Economy.getMoneyExact(player.getName()).doubleValue();
        } catch (UserDoesNotExistException | NullPointerException e) {
            return 0;
        }
    }

    @Override
    public double getBalance(UUID playerId) {
        User user;
        try {
            user = essentials.getUser(playerId);
        } catch (NullPointerException e) {
            return 0;
        }

        if (user != null) {
            return user.getMoney().doubleValue();
        }
        return 0;
    }

    @Override
    public double getTotalBalance(List<UUID> playerIds) {
        double balance = 0;
        for (UUID playerId : playerIds) {
            balance += getBalance(playerId);
        }
        return balance;
    }

    @Override
    public double getFactionBalance(String factionId) {
        try {
            return Economy.getMoneyExact(factionsHook.getEssentialsAccount(factionId)).doubleValue();
        } catch (UserDoesNotExistException e) {
            return 0;
        }
    }

    @Override
    public double getAllianceBalance(String allianceId) {
        try {
            return Economy.getMoneyExact(factionsHook.getAllianceEssentialsAccount(allianceId)).doubleValue();
        } catch (UserDoesNotExistException e) {
            return 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEconomyEvent(UserBalanceUpdateEvent event) {
        double oldBalance = event.getOldBalance().doubleValue();
        double newBalance = event.getNewBalance().doubleValue();

        Player player = event.getPlayer();

        if (!player.isOnline()) {
            Matcher matcher = economyAccountPattern.matcher(player.getName());
            if (matcher.matches()) {
                String factionId = matcher.group(1).replace("_", "-");
                if (factionsHook.isFaction(factionId)) {
                    if (factionEnabled) {
                        callEvent(new FactionEconomyEvent(factionId, oldBalance, newBalance));
                    }
                    return;
                }
            }

            Matcher allianceMatcher = allianceEconomyAccountPattern.matcher(player.getName());
            if (allianceMatcher.matches()) {
                String allianceId = allianceMatcher.group(1).replace("_", "-");
                if (factionsHook.getAllianceName(allianceId) != null) {
                    if (alliancesEnabled) {
                        callEvent(new AllianceEconomyEvent(allianceId, oldBalance, newBalance));
                    }
                    return;
                }
            }
        }

        try {
            if (Economy.isNPC(player.getName())) {
                return;
            }
        } catch (UserDoesNotExistException ignore) {
        }

        if (playerEnabled) {
            callEvent(new PlayerEconomyEvent(player, oldBalance, newBalance));
        }
    }

    private void callEvent(Event event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }
}
