package net.novucs.ftop.command;

import mkremins.fanciful.FancyMessage;
import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.entity.ButtonMessage;
import net.novucs.ftop.entity.Worth;
import net.novucs.ftop.util.SplaySet;
import net.novucs.ftop.util.TreeIterator;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.novucs.ftop.util.StringUtils.*;

public class TextCommand implements CommandExecutor, PluginService {

    private final FactionsTopPlugin plugin;

    public TextCommand(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginCommand("ftop").setExecutor(this);
    }

    @Override
    public void terminate() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(plugin.getName().toLowerCase() + ".use")) {
            sender.sendMessage(plugin.getSettings().getPermissionMessage());
            return true;
        }

        if (args.length == 0) {
            sendTop(sender, command.getName(), false, 0);
        } else {
            boolean showAlliances = args[0].toLowerCase().startsWith("ally")
                    || args[0].toLowerCase().startsWith(plugin.getSettings().getAllianceTypeName().toLowerCase());
            sendTop(sender, command.getName(), showAlliances, NumberUtils.toInt(args[args.length - 1]));
        }
        return true;
    }

    private void sendTop(CommandSender sender, String command, boolean showAlliances, int page) {
        // Do not attempt to send hook worth if page requested is beyond the limit.
        int entries = plugin.getSettings().getFactionsPerPage();
        SplaySet<Worth> worthSet = showAlliances
                ? plugin.getWorthManager().getOrderedAlliances()
                : plugin.getWorthManager().getOrderedFactions();
        int maxPage = Math.max((int) Math.ceil((double) worthSet.size() / entries), 1);
        page = Math.max(1, Math.min(maxPage, page));
        String type = showAlliances
                ? plugin.getSettings().getAllianceTypeName()
                : plugin.getSettings().getFactionTypeName();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{page:back}", String.valueOf(page - 1));
        placeholders.put("{page:this}", String.valueOf(page));
        placeholders.put("{page:next}", String.valueOf(page + 1));
        placeholders.put("{page:last}", String.valueOf(maxPage));
        placeholders.put("{type}", type);
        placeholders.put("{type:faction}", plugin.getSettings().getFactionTypeName());
        placeholders.put("{type:alliance}", plugin.getSettings().getAllianceTypeName());

        ButtonMessage back = plugin.getSettings().getBackButtonMessage();
        ButtonMessage next = plugin.getSettings().getNextButtonMessage();

        String backMsg = page == 1 ? back.getDisabled() : back.getEnabled();
        String backCmd = page == 1 ? null : "/" + command + " " + type + " " + (page - 1);
        List<String> backTooltip = replace(back.getTooltip(), placeholders);

        String nextMsg = page == maxPage ? next.getDisabled() : next.getEnabled();
        String nextCmd = page == maxPage ? null : "/" + command + " " + type + " " + (page + 1);
        List<String> nextTooltip = replace(next.getTooltip(), placeholders);

        if (!plugin.getSettings().getHeaderMessage().isEmpty()) {
            String headerString = replace(plugin.getSettings().getHeaderMessage(), placeholders);
            FancyMessage header = build(headerString, backMsg, backCmd, backTooltip, nextMsg, nextCmd, nextTooltip);
            header.send(sender);
        }

        if (worthSet.size() == 0) {
            sender.sendMessage(plugin.getSettings().getNoEntriesMessage());
            return;
        }

        int spacer = entries * --page;
        TreeIterator<Worth> it = worthSet.iterator(spacer);
        for (int i = 0; i < entries; i++) {
            if (!it.hasNext()) break;
    
            Worth worth = it.next();

            Map<String, String> worthPlaceholders = new HashMap<>(placeholders);
            worthPlaceholders.put("{rank}", Integer.toString(spacer + i + 1));
            worthPlaceholders.put("{relcolor}", "" + ChatColor.COLOR_CHAR + getRelationColor(plugin, sender, showAlliances, worth.getId()).getChar());
            worthPlaceholders.put("{name}", worth.getName());
            worthPlaceholders.put("{faction}", worth.getName()); // backwards compatibility
            worthPlaceholders.put("{worth:total}", plugin.getSettings().getCurrencyFormat().format(worth.getTotalWorth()));
            worthPlaceholders.put("{count:total:spawner}", plugin.getSettings().getCountFormat().format(worth.getTotalSpawnerCount()));

            String bodyMessage = insertPlaceholders(plugin.getSettings(), worth, replace(plugin.getSettings().getBodyMessage(), worthPlaceholders));
            List<String> tooltip = insertPlaceholders(plugin.getSettings(), worth, replace(showAlliances
                            ? plugin.getSettings().getBodyAllianceTooltip()
                            : plugin.getSettings().getBodyTooltip()
                    , worthPlaceholders));

            FancyMessage message = new FancyMessage(bodyMessage).tooltip(tooltip);
            message.send(sender);
        }

        if (!plugin.getSettings().getFooterMessage().isEmpty()) {
            String footerString = replace(plugin.getSettings().getFooterMessage(), placeholders);
            FancyMessage footer = build(footerString, backMsg, backCmd, backTooltip, nextMsg, nextCmd, nextTooltip);
            footer.send(sender);
        }
    }

    private FancyMessage build(String message, String backText, String backCmd, List<String> backTooltip,
                               String nextText, String nextCmd, List<String> nextTooltip) {
        FancyMessage fancyMessage = new FancyMessage("");

        while (!message.isEmpty()) {
            int backIndex = message.indexOf("{button:back}");
            int nextIndex = message.indexOf("{button:next}");

            backIndex = backIndex == -1 ? Integer.MAX_VALUE : backIndex;
            nextIndex = nextIndex == -1 ? Integer.MAX_VALUE : nextIndex;

            if (backIndex < nextIndex) {
                fancyMessage.then(message.substring(0, backIndex)).then(backText);

                if (backCmd != null) {
                    fancyMessage.command(backCmd).tooltip(backTooltip);
                }

                message = message.substring(backIndex + 13);
            } else if (nextIndex < backIndex) {
                fancyMessage.then(message.substring(0, nextIndex)).then(nextText);

                if (nextCmd != null) {
                    fancyMessage.command(nextCmd).tooltip(nextTooltip);
                }

                message = message.substring(nextIndex + 13);
            } else {
                fancyMessage.then(message);
                break;
            }
        }

        return fancyMessage;
    }
}
