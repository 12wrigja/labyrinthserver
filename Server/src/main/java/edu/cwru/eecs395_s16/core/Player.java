package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.DatabaseObject;
import edu.cwru.eecs395_s16.services.cache.CacheService;
import edu.cwru.eecs395_s16.services.connections.GameClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/19/16.
 */
public class Player implements DatabaseObject {

    //Match local storage and caching
    private static final String PLAYER_CURRENT_MATCH_KEY = ":CurrentMatch";
    private final boolean isDev;
    private String username;
    private String password;
    private Optional<GameClient> client;
    private Optional<UUID> currentMatchID;

    private int databaseIdentifier;

    public Player(int databaseIdentifier, String username, String password, boolean isDev) {
        this.username = username;
        this.password = password;
        this.databaseIdentifier = databaseIdentifier;
        this.client = Optional.empty();
        this.isDev = isDev;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public String getUsername() {
        return username;
    }

    public Optional<GameClient> getClient() {
        return GameEngine.instance().services.sessionRepository.findClient(this);
    }

    public void setClient(Optional<GameClient> client) {
        this.client = client;
    }

    @Override
    public int hashCode() {
        return getUsername().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Player))
            return false;

        Player player = (Player) o;

        return getUsername().equals(player.getUsername());

    }

    public Optional<UUID> getCurrentMatchID() {
        //If the current match is null, it means we haven't looked at it yet.
        //Let's try retrieving it from the cache
        if (currentMatchID == null) {
            Optional<String> matchID = GameEngine.instance().services.cacheService.getString(this.getUsername() +
                    PLAYER_CURRENT_MATCH_KEY);
            if (matchID.isPresent()) {
                //Build up the match object from it's unique id
                currentMatchID = Optional.of(UUID.fromString(matchID.get()));
            } else {
                //According to the cache, we aren't in a match.
                //Set this to empty so we don't need to look again
                this.currentMatchID = Optional.empty();
            }
        } else {
            return currentMatchID;
        }
        //TODO check and make sure this is threadsafe
        return currentMatchID;
    }

    public void setCurrentMatch(Optional<UUID> currentMatch) {
        CacheService cache = GameEngine.instance().services.cacheService;
        if (currentMatch.isPresent()) {
            cache.storeString(this.getUsername() + PLAYER_CURRENT_MATCH_KEY, currentMatch.get().toString());
        } else {
            cache.removeString(this.getUsername() + PLAYER_CURRENT_MATCH_KEY);
        }
        this.currentMatchID = currentMatch;
    }

    public void forceLeaveCurrentMatch() {
        GameEngine.instance().services.cacheService.removeString(this.getUsername() + PLAYER_CURRENT_MATCH_KEY);
        setCurrentMatch(Optional.empty());
    }

    @Override
    public int getDatabaseID() {
        return this.databaseIdentifier;
    }

    public boolean isDev() {
        return isDev;
    }
}
