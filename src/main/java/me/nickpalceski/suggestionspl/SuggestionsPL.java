package me.nickpalceski.suggestionspl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class SuggestionsPL extends JavaPlugin {
    private final List<Suggestion> suggestions = new ArrayList<>();
    private final Map<UUID, Set<String>> playerVotes = new HashMap<>();
    private final Map<UUID, Map<String, String>> playersAddingSuggestion = new HashMap<>();
    private FileConfiguration suggestionsConfig;
    private File suggestionsFile;
    private FileConfiguration playerVotesConfig;
    private File playerVotesFile;
    private String discordLink;

    @Override
    public void onEnable() {

        saveDefaultConfig(); // This saves the default config.yml if it does not exist
        reloadConfig(); // This reloads the config to ensure we are using the latest values

        discordLink = getConfig().getString("link", "https://discord.gg/yourinvite");


        createSuggestionsFile();
        createPlayerVotesFile();
        loadSuggestions();
        loadPlayerVotes();
        cleanUpPlayerVotes();

        this.getCommand("suggestions").setExecutor(new SuggestionsCommand(this));
        this.getCommand("addsuggestion").setExecutor(new AddSuggestionCommand(this));
        this.getCommand("deletesuggestion").setExecutor(new DeleteSuggestionCommand(this));
        this.getCommand("reloadsuggestions").setExecutor(new ReloadSuggestionsCommand(this));

        Bukkit.getPluginManager().registerEvents(new SuggestionsListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this), this);
    }

    @Override
    public void onDisable() {
        saveSuggestions();
        savePlayerVotes();
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public Map<UUID, Set<String>> getPlayerVotes() {
        return playerVotes;
    }

    public Map<UUID, Map<String, String>> getPlayersAddingSuggestion() {
        return playersAddingSuggestion;
    }

    public String getDiscordLink() {
        return discordLink;
    }

    public void setDiscordLink(String discordLink) {
        this.discordLink = discordLink;
    }

    private void createSuggestionsFile() {
        suggestionsFile = new File(getDataFolder(), "suggestions.yml");
        if (!suggestionsFile.exists()) {
            try {
                suggestionsFile.getParentFile().mkdirs();
                suggestionsFile.createNewFile();
                // Add default values to the newly created suggestions.yml file
                suggestionsConfig = YamlConfiguration.loadConfiguration(suggestionsFile);
                suggestionsConfig.set("suggestions", new ArrayList<>());
                suggestionsConfig.save(suggestionsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            suggestionsConfig = YamlConfiguration.loadConfiguration(suggestionsFile);
        }
    }

    private void createPlayerVotesFile() {
        playerVotesFile = new File(getDataFolder(), "playervotes.yml");
        if (!playerVotesFile.exists()) {
            try {
                playerVotesFile.getParentFile().mkdirs();
                playerVotesFile.createNewFile();
                // Add default values to the newly created playervotes.yml file
                playerVotesConfig = YamlConfiguration.loadConfiguration(playerVotesFile);
                playerVotesConfig.set("playerVotes", new HashMap<>());
                playerVotesConfig.save(playerVotesFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            playerVotesConfig = YamlConfiguration.loadConfiguration(playerVotesFile);
        }
    }

    private void loadSuggestions() {
        List<Map<?, ?>> suggestionsList = suggestionsConfig.getMapList("suggestions");
        for (Map<?, ?> suggestionMap : suggestionsList) {
            String name = (String) suggestionMap.get("name");
            String description = (String) suggestionMap.get("description");
            String playerName = (String) suggestionMap.get("playerName");
            int votes = (int) suggestionMap.get("votes");
            String uniqueIdString = (String) suggestionMap.get("uniqueId");
            UUID uniqueId = UUID.fromString(uniqueIdString);
            Suggestion suggestion = new Suggestion(uniqueId, name, description, playerName, votes);
            suggestions.add(suggestion);
        }
    }

    private void loadPlayerVotes() {
        ConfigurationSection playerVotesSection = playerVotesConfig.getConfigurationSection("playerVotes");
        if (playerVotesSection != null) {
            for (String uuidString : playerVotesSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                Set<String> votes = new HashSet<>(playerVotesSection.getStringList(uuidString));
                if (!votes.isEmpty()) {
                    playerVotes.put(uuid, votes);
                }
            }
        }
    }

    public void saveSuggestions() {
        List<Map<String, Object>> suggestionsList = new ArrayList<>();
        for (Suggestion suggestion : suggestions) {
            Map<String, Object> suggestionMap = new HashMap<>();
            suggestionMap.put("name", suggestion.getName());
            suggestionMap.put("description", suggestion.getDescription());
            suggestionMap.put("playerName", suggestion.getPlayerName());
            suggestionMap.put("votes", suggestion.getVotes());
            suggestionMap.put("uniqueId", suggestion.getUniqueId().toString());
            suggestionsList.add(suggestionMap);
        }
        suggestionsConfig.set("suggestions", suggestionsList);

        try {
            suggestionsConfig.save(suggestionsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerVotes() {
        Map<String, List<String>> playerVotesMap = new HashMap<>();
        for (UUID uuid : playerVotes.keySet()) {
            if (!playerVotes.get(uuid).isEmpty()) {
                playerVotesMap.put(uuid.toString(), new ArrayList<>(playerVotes.get(uuid)));
            }
        }
        playerVotesConfig.set("playerVotes", playerVotesMap);

        try {
            playerVotesConfig.save(playerVotesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeVotesForSuggestion(UUID suggestionId) {
        for (UUID playerId : playerVotes.keySet()) {
            playerVotes.get(playerId).remove(suggestionId.toString());
        }
    }

    private void cleanUpPlayerVotes() {
        Set<String> validSuggestionIds = suggestions.stream()
                .map(s -> s.getUniqueId().toString())
                .collect(Collectors.toSet());

        for (UUID playerId : playerVotes.keySet()) {
            playerVotes.get(playerId).removeIf(voteId -> !validSuggestionIds.contains(voteId));
        }
    }

    public void openSuggestionsGUI(Player player, int page) {
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, "Suggestions Page " + (page + 1));
        int startIndex = page * (size-9);
        int endIndex = (page+1) * (size - 9);

        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName("Suggest an Idea Here!");
        glassMeta.setLore(Collections.singletonList(discordLink));
        glassPane.setItemMeta(glassMeta);
        gui.setItem(size - 5, glassPane); // Add the glass pane to the middle of the last row

        for (int i = startIndex; i < suggestions.size() && i < endIndex; i++) {
            Suggestion suggestion = suggestions.get(i);
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + suggestion.getName());
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "ID: " + suggestion.getUniqueId().toString());
            lore.add(ChatColor.BLUE + "Description: " + suggestion.getDescription());
            lore.add(ChatColor.GOLD + "Votes: " + suggestion.getVotes());
            lore.add(ChatColor.AQUA + "Suggested by: " + suggestion.getPlayerName());

            meta.setLore(lore);
            paper.setItemMeta(meta);
            gui.setItem(i % 45, paper);
        }

        // Add navigation arrows if needed
        if ((size - 9) < (suggestions.size() - (size-9)*page)) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            nextPage.setItemMeta(nextPageMeta);
            gui.setItem(size - 1, nextPage);
        }

        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            prevPage.setItemMeta(prevPageMeta);
            gui.setItem(size - 9, prevPage);
        }

        player.openInventory(gui);
    }

    public void openSuggestionsGUIForDeletion(Player player, int page) {
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, "Delete Suggestions Page " + (page + 1));
        int startIndex = page * (size-9);
        int endIndex = (page+1) * (size - 9);

        for (int i = startIndex; i < suggestions.size() && i < endIndex; ++i) {
            Suggestion suggestion = suggestions.get(i);
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + suggestion.getName());
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + "ID: " + suggestion.getUniqueId().toString());
            lore.add(ChatColor.BLUE + "Description: " + suggestion.getDescription());
            lore.add(ChatColor.GOLD + "Votes: " + suggestion.getVotes());
            lore.add(ChatColor.AQUA + "Suggested by: " + suggestion.getPlayerName());
            lore.add(ChatColor.RED + "Click to delete this suggestion");

            meta.setLore(lore);
            paper.setItemMeta(meta);
            gui.setItem(i % 45, paper);
        }

        // Add navigation arrows if needed
        if ((size - 9) < (suggestions.size() - (size-9)*page)) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            nextPage.setItemMeta(nextPageMeta);
            gui.setItem(size - 1, nextPage);
        }

        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevPageMeta = prevPage.getItemMeta();
            prevPageMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            prevPage.setItemMeta(prevPageMeta);
            gui.setItem(size - 9, prevPage);
        }

        player.openInventory(gui);
    }
}
