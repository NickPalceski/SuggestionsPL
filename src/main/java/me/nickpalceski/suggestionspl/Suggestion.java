package me.nickpalceski.suggestionspl;

import java.util.UUID;

public class Suggestion {
    private final UUID uniqueId;
    private final String name;
    private final String description;
    private final String playerName;
    private int votes;

    public Suggestion(String name, String description, String playerName) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.playerName = playerName;
        this.votes = 0;
    }

    public Suggestion(UUID uniqueId, String name, String description, String playerName, int votes) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.description = description;
        this.playerName = playerName;
        this.votes = votes;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getVotes() {
        return votes;
    }

    public void addVote() {
        votes++;
    }

    public void removeVote() {
        if (votes > 0) {
            votes--;
        }
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
