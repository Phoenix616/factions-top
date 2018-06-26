package net.novucs.ftop.gui.element;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.Settings;
import net.novucs.ftop.entity.Worth;
import net.novucs.ftop.gui.GuiContext;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.*;

import static net.novucs.ftop.util.StringUtils.*;

public class GuiWorthList implements GuiElement {

    private final int count;
    private final boolean fillEmpty;
    private final String text;
    private final List<String> lore;

    private GuiWorthList(int count, boolean fillEmpty, String text, List<String> lore) {
        this.count = count;
        this.fillEmpty = fillEmpty;
        this.text = text;
        this.lore = lore;
    }

    @Override
    public void render(GuiContext context) {
        FactionsTopPlugin plugin = context.getPlugin();
        DecimalFormat currencyFormat = plugin.getSettings().getCurrencyFormat();
        DecimalFormat countFormat = plugin.getSettings().getCountFormat();

        int counter = 0;
        while (counter++ < count) {
            if (context.getInventory().getSize() <= context.getSlot()) {
                break;
            }

            context.getSlots().add(this);

            if (!context.getWorthIterator().hasNext()) {
                if (!fillEmpty) {
                    break;
                }
                context.getAndIncrementSlot();
                continue;
            }

            Worth worth = context.getWorthIterator().next();
            Map<String, String> placeholders = new HashMap<>(context.getPlaceholders());
            placeholders.put("{rank}", Integer.toString(context.getAndIncrementRank()));
            placeholders.put("{relcolor}", "" + ChatColor.COLOR_CHAR +
                    getRelationColor(plugin, context.getPlayer(), context.isShowingAlliances(), worth.getId()).getChar());
            placeholders.put("{name}", worth.getName());
            placeholders.put("{faction}", worth.getName()); // backwards compatibility
            placeholders.put("{worth:total}", currencyFormat.format(worth.getTotalWorth()));
            placeholders.put("{count:total:spawner}", countFormat.format(worth.getTotalSpawnerCount()));

            String owner = context.isShowingAlliances()
                    ? plugin.getFactionsHook().getAllianceOwnerName(worth.getId())
                    : plugin.getFactionsHook().getOwnerName(worth.getId());
            ItemStack item = getItem(worth, placeholders, plugin.getSettings(), owner);
            context.getInventory().setItem(context.getAndIncrementSlot(), item);
        }
    }

    @Override
    public void handleClick(GuiContext context) {
    }

    public int getCount() {
        return count;
    }

    private ItemStack getItem(Worth worth, Map<String, String> placeholders, Settings settings, String owner) {
        String text = insertPlaceholders(settings, worth, replace(this.text, placeholders));
        List<String> lore = insertPlaceholders(settings, worth, replace(this.lore, placeholders));

        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(text);
        meta.setLore(lore);
        meta.setOwner(owner);

        item.setItemMeta(meta);

        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiWorthList that = (GuiWorthList) o;
        return count == that.count &&
                fillEmpty == that.fillEmpty &&
                Objects.equals(text, that.text) &&
                Objects.equals(lore, that.lore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, fillEmpty, text, lore);
    }

    @Override
    public String toString() {
        return "GuiWorthList{" +
                "count=" + count +
                ", fillEmpty=" + fillEmpty +
                ", text='" + text + '\'' +
                ", lore=" + lore +
                '}';
    }

    public static class Builder {
        private int count = 0;
        private boolean fillEmpty = true;
        private String text = "";
        private List<String> lore = new ArrayList<>();

        public void factionCount(int factionCount) {
            this.count = factionCount;
        }

        public void fillEmpty(boolean fillEmpty) {
            this.fillEmpty = fillEmpty;
        }

        public void text(String text) {
            this.text = text;
        }

        public void lore(List<String> lore) {
            this.lore = lore;
        }

        public GuiWorthList build() {
            return new GuiWorthList(count, fillEmpty, text, lore);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Builder builder = (Builder) o;
            return count == builder.count &&
                    fillEmpty == builder.fillEmpty &&
                    Objects.equals(text, builder.text) &&
                    Objects.equals(lore, builder.lore);
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, fillEmpty, text, lore);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "count=" + count +
                    ", fillEmpty=" + fillEmpty +
                    ", text='" + text + '\'' +
                    ", lore=" + lore +
                    '}';
        }
    }
}
