package net.novucs.ftop.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ItemMatcher {
    private final String name;
    private final double worth;
    private final List<MatchDefinition> specificDefinitions = new ArrayList<>();

    public ItemMatcher(JavaPlugin plugin, String name, ConfigurationSection config) throws IllegalArgumentException {
        this.name = name;
        this.worth = config.getDouble("worth");
        for (String specificKey : config.getKeys(false)) {
            if (config.isConfigurationSection(specificKey)) {
                specificDefinitions.add(new MatchDefinition(plugin, specificKey, config.getConfigurationSection(specificKey)));
            }
        }
    }

    /**
     * Get the name of this matcher
     * @return  The name of the matcher as passed in the constructor
     */
    public String getName() {
        return name;
    }

    /**
     * How much this item group is worth
     * @return  The worth of the item group
     */
    public double getWorth() {
        return worth;
    }

    /**
     * Get the matching definition
     * @param item  The item to match against
     * @return      The matching definition or null if none match. The default one will have a "null" name!
     */
    public MatchDefinition getMatching(ItemStack item) {
        for (MatchDefinition definition : specificDefinitions) {
            if (definition.matches(item)) {
                return definition;
            }
        }
        return null;
    }

    /**
     * Check if this matcher matches an item,
     * calls {@link #getMatching(ItemStack)} internally so use that if you need the actual matched definition later on
     * @param item  The item to match against
     * @return      Whether or not this matcher matches it
     */
    public boolean matches(ItemStack item) {
        return getMatching(item) != null;
    }

    public static class MatchDefinition {
        private final String name;

        private final boolean inverted;
        private final Set<Material> materials = EnumSet.noneOf(Material.class);
        private final Set<Pattern> names = new LinkedHashSet<>();
        private final Set<Pattern> lores = new LinkedHashSet<>();
        private final List<NumberComparator> durability = new ArrayList<>();
        private final Boolean unbreakable;
        private final Map<Enchantment, List<NumberComparator>> enchantments = new HashMap<>();
        private final Pattern serializedRegex;

        public MatchDefinition(JavaPlugin plugin, String name, ConfigurationSection config) {
            this.name = name;
            inverted = config.getBoolean("inverted");
            for (String matStr : config.getStringList("material")) {
                Material mat = Material.getMaterial(matStr.toUpperCase());
                if (mat != null) {
                    materials.add(mat);
                } else {
                    plugin.getLogger().log(Level.WARNING, matStr + " is not a valid material name!");
                }
            }

            for (String nameStr : config.getStringList("name")) {
                try {
                    if (nameStr.startsWith("r=")) {
                        names.add(Pattern.compile("(?i)" + nameStr.substring(2)));
                    } else {
                        names.add(Pattern.compile("(?i)" + Pattern.quote(nameStr)));
                    }
                } catch (PatternSyntaxException e) {
                    plugin.getLogger().log(Level.WARNING, nameStr + " is not a valid name pattern!");
                }
            }

            for (String lore : config.getStringList("lore")) {
                try {
                    if (lore.startsWith("r=")) {
                        lores.add(Pattern.compile("(?i)" + lore.substring(2)));
                    } else {
                        lores.add(Pattern.compile("(?i)" + Pattern.quote(lore)));
                    }
                } catch (PatternSyntaxException e) {
                    plugin.getLogger().log(Level.WARNING, lore + " is not a valid lore pattern!");
                }
            }

            if (config.contains("durability")) {
                for (String def : config.getString("durability").split(",")) {
                    try {
                        durability.add(new NumberComparator(def));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().log(Level.WARNING, def + " is not a valid durability comparator string!");
                    }
                }
            }

            unbreakable = config.isBoolean("unbreakable") ? config.getBoolean("unbreakable") : null;

            for (String enchStr : config.getStringList("enchantments")) {
                Enchantment ench;
                String levelStr = "";
                if (enchStr.contains(":")) {
                    String[] enchStrParts = enchStr.split(":");
                    if (enchStrParts.length > 1) {
                        enchStr = enchStrParts[0];
                        levelStr = enchStrParts[1];
                    }
                }
                ench = Enchantment.getByName(enchStr.toUpperCase());
                if (ench != null) {
                    List<NumberComparator> comparators = new ArrayList<>();
                    for (String def : levelStr.split(",")) {
                        try {
                            comparators.add(new NumberComparator(def));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().log(Level.WARNING, def + " is not a valid level comparator string!");
                        }
                    }
                    enchantments.put(ench, comparators);
                } else {
                    plugin.getLogger().log(Level.WARNING, enchStr + " is not a valid Enchantment name!");
                }
            }

            String serializedStr = config.getString("serialized");
            Pattern serializedPattern = null;
            if (serializedStr != null && !serializedStr.isEmpty()) {
                try {
                    serializedPattern = Pattern.compile(serializedStr);
                } catch (PatternSyntaxException e) {
                    plugin.getLogger().log(Level.WARNING, serializedStr + " is not a valid serialized regex string! " + e.getMessage());
                }
            }
            serializedRegex = serializedPattern;
        }

        public boolean matches(ItemStack item) {
            if (item == null) {
                return false;
            }

            boolean matches = !inverted;
            boolean doesNotMatch = inverted;

            if (!materials.contains(item.getType())) {
                return doesNotMatch;
            }

            if (!comparatorsMatch(durability, item.getDurability())) {
                return doesNotMatch;
            }

            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (unbreakable != null && unbreakable != meta.isUnbreakable()) {
                    return doesNotMatch;
                }

                if (!enchantmentsMatch(item.getEnchantments())) {
                    return doesNotMatch;
                }

                if (meta instanceof EnchantmentStorageMeta) {
                    if (!enchantmentsMatch(((EnchantmentStorageMeta) meta).getStoredEnchants())) {
                        return doesNotMatch;
                    }
                }

                if (meta.hasDisplayName()) {
                    if (!patternsMatch(names, meta.getDisplayName())) {
                        return doesNotMatch;
                    }
                }

                if (meta.hasLore()) {
                    if (!patternsMatch(names, String.join("\n", meta.getLore()))) {
                        return doesNotMatch;
                    }
                }
            }

            if (serializedRegex != null) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set("item", item);
                String yamlStr = yaml.saveToString();
                if (!serializedRegex.matcher(yamlStr).find()) {
                    return doesNotMatch;
                }
            }

            return matches;
        }

        private boolean comparatorsMatch(List<NumberComparator> comparators, int i) {
            for (NumberComparator comparator : comparators) {
                if (!comparator.matches(i)) {
                    return false;
                }
            }
            return true;
        }

        private boolean patternsMatch(Collection<Pattern> patterns, String toMatch) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(toMatch).find()) {
                    return true;
                }
            }
            return false;
        }

        private boolean enchantmentsMatch(Map<Enchantment, Integer> enchantments) {
            if (this.enchantments.isEmpty()) {
                return true;
            }

            for (Map.Entry<Enchantment, Integer> ench : enchantments.entrySet()) {
                if (this.enchantments.containsKey(ench.getKey())
                        && comparatorsMatch(this.enchantments.get(ench.getKey()), ench.getValue())) {
                    return true;
                }
            }

            return false;
        }

        /**
         * The name of the definition
         * @return The name of the definition
         */
        public String getName() {
            return name;
        }

        public static class NumberComparator {
            private final Type type;
            private final int number;

            public NumberComparator(String def) {
                if (def == null || def.isEmpty() || "*".equals(def)) {
                    type = Type.ANY;
                    number = -1;
                    return;
                }

                for (Type t : Type.values()) {
                    if (def.startsWith(t.symbol())) {
                        type = t;
                        try {
                            number = Integer.parseInt(def.substring(t.symbol().length()));
                        } catch (NumberFormatException ignored) {
                            throw new IllegalArgumentException(def + " is not a valid NumberComparator definition!");
                        }
                        return;
                    }
                }

                type = Type.EQUALS;
                try {
                    number = Integer.parseInt(def);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(def + " is not a valid NumberComparator definition!");
                }
            }

            public boolean matches(int i) {
                switch (type) {
                    case ANY:
                        return true;
                    case EQUALS:
                        return i == number;
                    case NOT_EQUALS:
                        return i != number;
                    case LESS_THAN:
                        return i < number;
                    case GREATHER_THAN:
                        return i > number;
                }
                return false;
            }

            public enum Type {
                ANY("*"),
                GREATHER_THAN(">"),
                LESS_THAN("<"),
                EQUALS("="),
                NOT_EQUALS("!=");

                private final String symbol;

                Type(String symbol) {
                    this.symbol = symbol;
                }

                public String symbol() {
                    return symbol;
                }
            }
        }
    }
}
