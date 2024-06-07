package me.nickpalceski.suggestionspl;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerChatListener implements Listener {
    private final SuggestionsPL plugin;

    public PlayerChatListener(SuggestionsPL plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (plugin.getPlayersAddingSuggestion().containsKey(playerUUID)) {
            event.setCancelled(true);
            Map<String, String> suggestionData = plugin.getPlayersAddingSuggestion().get(playerUUID);

            String message = event.getMessage();
            String stage = suggestionData.get("stage");

            switch (stage) {
                case "name":
                    suggestionData.put("name", message);
                    player.sendMessage(ChatColor.GREEN + "Suggestion name set to: " + message);

                    player.sendMessage(ChatColor.AQUA + "Please type the " + ChatColor.BOLD + "description" + ChatColor.AQUA + " of your suggestion in chat.");
                    player.sendMessage("-----------------------------------------------");
                    suggestionData.put("stage", "description");
                    break;
                case "description":
                    suggestionData.put("description", message);
                    player.sendMessage(ChatColor.GREEN + "Suggestion description set to: " + message);
                    player.sendMessage(ChatColor.AQUA + "Please type " + ChatColor.BOLD + "suggesters name" + ChatColor.AQUA + " in chat.");
                    player.sendMessage("-----------------------------------------------");
                    suggestionData.put("stage", "playerName");
                    break;
                case "playerName":
                    suggestionData.put("playerName", message);
                    player.sendMessage(ChatColor.GREEN + "Suggestion player name set to: " + message);

                    Suggestion suggestion = new Suggestion(
                            suggestionData.get("name"),
                            suggestionData.get("description"),
                            suggestionData.get("playerName")
                    );
                    plugin.getSuggestions().add(suggestion);
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Suggestion added: " + suggestion.getName());

                    plugin.getPlayersAddingSuggestion().remove(playerUUID);

                    // Save after adding a suggestion
                    plugin.saveSuggestions();

                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unexpected error occurred.");
                    plugin.getPlayersAddingSuggestion().remove(playerUUID);
                    break;
            }
        }
    }
}
