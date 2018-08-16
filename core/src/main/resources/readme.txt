TownyTop by Novucs.

Configuration walkthrough:
- config-version: Should not be touched, determines config version.
- hook-priority: In which order other plugins should be tried to hook into
- command-aliases: List of command to rebind to "/ttop".
- gui-settings: All settings related to GUIs.
- - line-count: Number of inventory lines.
- - inventory-name: Name used in inventory header.
- - layout: Fully configurable GUI layout.
- ignored-faction-ids: Town IDs to not calculate for factions top.
- ignored-alliance-ids: Nation IDs to not calculate for nation top.
- disable-chest-events: Disables chest events, improves performance.
- factions-per-page: Number of towns displayed per page in "/ttop".
- sign-update-ticks: Duration in ticks between sign updates.
- sign-pattern: The regex with which the plugin should check for sign creation.
- liquid-update-ticks: Duration in ticks between liquid economy updates.
- chunk-queue-size: Hard-limit maximum chunks to be queued for recalculation.
- chunk-recalculate-millis: Duration in millis between chunk recalculations.
- chat: Chat placeholder settings.
- - enabled: Are TownyTop placeholders going to be used?
- - rank-placeholder: The text to replace in the original chat format.
- - rank-found: How the placeholder should look when a rank is found.
- - rank-not-found: How the placeholder should look when a rank is NOT found.
- placeholders: MVdWPlaceholderAPI settings.
- - faction-not-found: What to replace with when no town is found.
- - enabled-ranks: The ranks to be loaded into the MVdWPlaceholderAPI.
- database: Various database settings, MySQL and H2 are supported.
- - persist-interval: Millis between database updates.
- - persist-factions: Saves towns in database for websites to parse.
- enabled: Toggles whether specific worth types should be recalculated.
- perform-recalculate: Toggles chunk recalculation for the listed reasons.
- bypass-recalculate-delay: Toggles which reason bypasses the delay.
- spawner-prices: Value for specific spawners.
- block-prices: Value for specific blocks.
- special-prices: Value for special items.
- - item-group: A group of similar items
- - - worth: The worth of this group
- - - item-matcher: The definition of a matcher (all values can be left out and count as a match-all that way
- - - - inverted: Whether or not this match is inverted (match only items that don't match the values)
- - - - material: List of materials to match
- - - - name: List of strings in name to match (case-insensitive), use the prefix r= to use regex
- - - - lore: List of strings in lore to match (case-insensitive), use the prefix r= to use regex
- - - - durability: Item durability value, can use comparators, <x, >x, =x, !=x or just equal a single number. An empty string matches all durabilities. Also supports chaining of comparators with a comma. E.g. >5,<20 for between 5 and 20
- - - - unbreakable: Match items with the unbreakable tag
- - - - enchantments: List of enchantments to match, can match both all or only certain levels. Can take the same comparators as the durability for the level.
- - - - serialized: Serialize the item to YAML and filter it with regex. This is only for advanced users and is less efficient, leave empty to disable.
- - - another-item-matcher: You can define as many matchers as you want
- - another-item-group: Same with the groups, define multiple for different worths

Layout types:
- button_back/button_next: When clicked, moves to relevant page.
- - enabled/disabled: Button looks, enabled when the page is available.
- - - text: Item name.
- - - lore: Item lore.
- - - material: Item material.
- - - data: Item data.
- worth_list: Adds a list of factions to the GUI.
- - count: Number of towns/nations to add to the GUI.
- - fill-empty: Leaves the remainder slots blank when true.
- - text: Item name.
- - lore: Item lore.

Valid spawners (Case insensitive):
https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html

Valid materials (Case insensitive):
https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html

Valid worth types (Case insensitive):
- CHEST
- PLAYER_BALANCE
- FACTION_BALANCE
- SPAWNER
- BLOCK

Valid recalculation reasons (Case insensitive):
- COMMAND
- UNLOAD
- CLAIM
- BREAK
- PLACE
- EXPLODE
- CHEST

Placeholders:
Header/Footer only:
- {button:back} - Goes to previous page.
- {button:next} - Goes to next page.

Header, footer and body:
- {page:back} - Previous page number.
- {page:this} - Current page number.
- {page:next} - Next page number.
- {page:last} - Last page number.

Body only:
- {rank} - Town/nation rank.
- {relcolor} - Relation color of the town listed to the viewer.
- {name} - Town/nation name.
- {worth:total} - Total worth of town/nation listed.
- {count:total:spawner} - Total spawner count.
- {worth:<worth type>} - Value of a specific worth type.
- {count:spawner:<spawner>} - Count of a specific spawner type.
- {count:special:<special-name>} - Count of a specific special item group.
- {count:material:<material>} - Count of a specific material.

MVdW Placeholders/Clip's PlaceholderAPI:
- {townytop_name:*}
- - The town name of a rank by replacing * with a number.
- - The town name in last place by replacing * with "last".
- {townytop_rank:player}
- - The rank of the player's town if valid, otherwise faction-not-found text.
- {townytop_allyname:*}
- - The nation name of a rank by replacing * with a number.
- - The nation name in last place by replacing * with "last".
- {townytop_ally_rank:player}
- - The rank of the player's nation if valid, otherwise faction-not-found text.
