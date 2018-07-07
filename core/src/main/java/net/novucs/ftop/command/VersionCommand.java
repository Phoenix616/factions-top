package net.novucs.ftop.command;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VersionCommand implements CommandExecutor, PluginService {

    private FactionsTopPlugin plugin;

    public VersionCommand(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getCommand("ttopversion").setExecutor(this);
    }

    @Override
    public void terminate() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + plugin.getName() + " version " + plugin.getDescription().getVersion()
                + " by " + String.join(", ", plugin.getDescription().getAuthors()) + ".");
        return true;
    }
}
