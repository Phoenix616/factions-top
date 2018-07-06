FactionsTop by Novucs.

Configuration walkthrough:
- config-version: Should not be touched, determines config version.
- hook-priority: In which order other plugins should be tried to hook into
- command-aliases: List of command to rebind to "/ftop".
- gui-settings: All settings related to GUIs.
- - line-count: Number of inventory lines.
- - inventory-name: Name used in inventory header.
- - layout: Fully configurable GUI layout.
- ignored-faction-ids: Faction IDs to not calculate for factions top.
- disable-chest-events: Disables chest events, improves performance.
- factions-per-page: Number of factions displayed per page in "/ftop".
- sign-update-ticks: Duration in ticks between sign updates.
- sign-pattern: The regex with which the plugin should check for sign creation.
- liquid-update-ticks: Duration in ticks between liquid economy updates.
- chunk-queue-size: Hard-limit maximum chunks to be queued for recalculation.
- chunk-recalculate-millis: Duration in millis between chunk recalculations.
- chat: Chat placeholder settings.
- - enabled: Are FactionsTop placeholders going to be used?
- - rank-placeholder: The text to replace in the original chat format.
- - rank-found: How the placeholder should look when a rank is found.
- - rank-not-found: How the placeholder should look when a rank is NOT found.
- placeholders: MVdWPlaceholderAPI settings.
- - faction-not-found: What to replace with when no faction is found.
- - enabled-ranks: The ranks to be loaded into the MVdWPlaceholderAPI.
- database: Various database settings, MySQL and H2 are supported.
- - persist-interval: Millis between database updates.
- - persist-factions: Saves factions in database for websites to parse.
- enabled: Toggles whether specific worth types should be recalculated.
- perform-recalculate: Toggles chunk recalculation for the listed reasons.
- bypass-recalculate-delay: Toggles which reason bypasses the delay.
- spawner-prices: Value for specific spawners.
- block-prices: Value for specific blocks.

Layout types:
- button_back/button_next: When clicked, moves to relevant page.
- - enabled/disabled: Button looks, enabled when the page is available.
- - - text: Item name.
- - - lore: Item lore.
- - - material: Item material.
- - - data: Item data.
- worth_list: Adds a list of factions to the GUI.
- - count: Number of factions to add to the GUI.
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
- {rank} - Faction/alliance rank.
- {relcolor} - Relation color of the faction listed to the viewer.
- {name} - Faction/alliance name.
- {worth:total} - Total worth of faction listed.
- {count:total:spawner} - Total spawner count.
- {worth:<worth type>} - Value of a specific worth type.
- {count:spawner:<spawner>} - Count of a specific spawner type.
- {count:material:<material>} - Count of a specific material.

MVdW Placeholders/Clip's PlaceholderAPI:
- {factionstop_name:*}
- - The faction name of a rank by replacing * with a number.
- - The faction name in last place by replacing * with "last".
- {factionstop_rank:player}
- - The rank of the player's faction if valid, otherwise faction-not-found text.
- {factionstop_allyname:*}
- - The alliance name of a rank by replacing * with a number.
- - The alliance name in last place by replacing * with "last".
- {factionstop_ally_rank:player}
- - The rank of the player's alliance if valid, otherwise faction-not-found text.
