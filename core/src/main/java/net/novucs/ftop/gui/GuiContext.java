package net.novucs.ftop.gui;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.Worth;
import net.novucs.ftop.gui.element.GuiElement;
import net.novucs.ftop.util.TreeIterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiContext {

    private final FactionsTopPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final int maxPage;
    private final int thisPage;
    private final TreeIterator<Worth> worthIterator;
    private final Map<String, String> placeholders;
    private final List<GuiElement> slots = new ArrayList<>();
    private int currentRank;
    private int slot;
    private boolean showingAlliances;
    
    public GuiContext(FactionsTopPlugin plugin, Player player, Inventory inventory, int maxPage, int thisPage,
                      boolean showingAlliances, TreeIterator<Worth> worthIterator, Map<String, String> placeholders) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = inventory;
        this.maxPage = maxPage;
        this.thisPage = thisPage;
        this.showingAlliances = showingAlliances;
        this.worthIterator = worthIterator;
        this.placeholders = placeholders;
    }

    public FactionsTopPlugin getPlugin() {
        return plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public int getThisPage() {
        return thisPage;
    }

    public TreeIterator<Worth> getWorthIterator() {
        return worthIterator;
    }

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    public List<GuiElement> getSlots() {
        return slots;
    }

    public boolean hasNextPage() {
        return thisPage < maxPage;
    }

    public boolean hasPrevPage() {
        return thisPage > 1;
    }

    public int getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
    }

    public int getAndIncrementRank() {
        return currentRank++;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getAndIncrementSlot() {
        return slot++;
    }
    
    public boolean isShowingAlliances() {
        return showingAlliances;
    }
}
