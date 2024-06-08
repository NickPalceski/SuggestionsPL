package me.nickpalceski.suggestionspl;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;


public class SuggestionsListener implements Listener {
    private final SuggestionsPL plugin;

    public SuggestionsListener(SuggestionsPL plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null || (!event.getView().getTitle().startsWith("Delete Suggestions") && !event.getView().getTitle().startsWith("Suggestions"))) {
            return;
        }
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        ItemMeta meta = clickedItem.getItemMeta();
        List<String> lore = meta.getLore();

        String displayName = meta.getDisplayName();

        int currentPage = Integer.parseInt(event.getView().getTitle().split("Page ")[1]) - 1;

        // Handle navigation
        if (clickedItem.getType() == Material.ARROW) {
            if (displayName.equals(ChatColor.GREEN + "Next Page")) {
                if (event.getView().getTitle().startsWith("Suggestions")) {
                    plugin.openSuggestionsGUI(player, currentPage + 1);
                } else if (event.getView().getTitle().startsWith("Delete Suggestions")) {
                    plugin.openSuggestionsGUIForDeletion(player, currentPage + 1);
                }
            } else if (displayName.equals(ChatColor.GREEN + "Previous Page")) {
                if (event.getView().getTitle().startsWith("Suggestions")) {
                    plugin.openSuggestionsGUI(player, currentPage - 1);
                } else if (event.getView().getTitle().startsWith("Delete Suggestions")) {
                    plugin.openSuggestionsGUIForDeletion(player, currentPage - 1);
                }
            }
            return;
        }

        //handle discord (black stained-glass)
        if (clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            if (lore != null && !lore.isEmpty()) {
                player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.8f, 0.8f);
                String discordLink = ChatColor.stripColor(lore.get(0));
                player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "         Winter"+ChatColor.BLUE + ""+ChatColor.BOLD+ "Vale");
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Click the following link to go to our Discord!");
                player.sendMessage(ChatColor.DARK_PURPLE + discordLink);
            }
            return;
        }

        // Voting functionality
        if (event.getView().getTitle().startsWith("Suggestions")) {
            if (clickedItem.getType() == Material.PAPER) {
                String uniqueIdString = ChatColor.stripColor(lore.get(0)).replace("ID: ", "");
                UUID uniqueId;

                try {
                    uniqueId = UUID.fromString(uniqueIdString);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Error: Invalid suggestion format.");
                    return;
                }

                Suggestion suggestion = plugin.getSuggestions().stream()
                        .filter(s -> s.getUniqueId().equals(uniqueId))
                        .findFirst().orElse(null);

                if (suggestion != null) {
                    if (!plugin.getPlayerVotes().containsKey(player.getUniqueId()) ||
                            !plugin.getPlayerVotes().get(player.getUniqueId()).contains(suggestion.getUniqueId().toString())) {
                        suggestion.addVote();

                        // Update the last line with the new vote count
                        for (int i = 0; i < lore.size(); i++) {
                            if (lore.get(i).startsWith(ChatColor.GOLD + "Votes: ")) {
                                lore.set(i, ChatColor.GOLD + "Votes: " + suggestion.getVotes());
                                break;
                            }
                        }

                        meta.setLore(lore);
                        clickedItem.setItemMeta(meta);

                        // Add the player's UUID to the votes map
                        plugin.getPlayerVotes().computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(suggestion.getUniqueId().toString());

                        // Save after voting
                        plugin.savePlayerVotes();

                    } else {
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 0.8f, 0.8f);
                        player.sendMessage(ChatColor.RED + "You have already voted for this suggestion!");
                    }
                }
            }
        }

        // Deletion functionality
        if (event.getView().getTitle().startsWith("Delete Suggestions")) {
            if (clickedItem.getType() == Material.PAPER) {
                String uniqueIdString = ChatColor.stripColor(lore.get(0)).replace("ID: ", "");
                UUID uniqueId;

                try {
                    uniqueId = UUID.fromString(uniqueIdString);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Error: Invalid suggestion format.");
                    return;
                }

                Suggestion suggestionToRemove = plugin.getSuggestions().stream()
                        .filter(suggestion -> suggestion.getUniqueId().equals(uniqueId))
                        .findFirst().orElse(null);

                if (suggestionToRemove != null) {
                    plugin.getSuggestions().remove(suggestionToRemove);
                    plugin.removeVotesForSuggestion(uniqueId);
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 0.8f);
                    player.sendMessage(ChatColor.GREEN + "Suggestion removed: " + suggestionToRemove.getName());
                    player.closeInventory(); // Close the player's inventory

                    // Save after deleting a suggestion
                    plugin.saveSuggestions();
                    plugin.savePlayerVotes();

                } else {
                    player.sendMessage(ChatColor.RED + "Error: Suggestion not found.");
                }
            }
        }
    }
}
