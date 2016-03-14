package edu.cwru.eecs395_s16.services.herorepository;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroType;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by james on 2/18/16.
 */
public class PostgresHeroRepository implements HeroRepository {

    private Connection conn;
    private static final String GET_HEROES_QUERY = "select * from hero_player inner join heroes on hero_player.hero_id = heroes.id where player_id = ?";

    public PostgresHeroRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public InternalResponseObject<List<Hero>> getPlayerHeroes(Player p) {
        try {
            List<Hero> heroes = new ArrayList<>();
            PreparedStatement stmt = conn.prepareStatement(GET_HEROES_QUERY);
            stmt.setInt(1,p.getDatabaseID());
            ResultSet rst = stmt.executeQuery();
            while(rst.next()){
                Hero h = heroFromResultSet(p,rst);
                heroes.add(h);
            }
            return new InternalResponseObject<>(heroes, "heroes");
        } catch (SQLException e) {
            if(GameEngine.instance().IS_DEBUG_MODE){
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.INVALID_USERNAME, "Unable to retrieve heroes from Postgres for the given username.");
        }
    }

    @Override
    public void saveHeroForPlayer(Player p, Hero h) {
        //Need to save: hero level, equipped weapon, and equipped equipment
        //TODO implement hero data storage - useful for whenever weapons, equipment, or level changes

    }

    private Hero heroFromResultSet(Player p, ResultSet r) throws SQLException {
        int heroID = r.getInt("hero_id");
        int attack = r.getInt("attack");
        int defense = r.getInt("defense");
        int health = r.getInt("health");
        int movement = r.getInt("movement");
        int vision = r.getInt("vision");
        int level = r.getInt("level");
        String heroType = r.getString("class");
        HeroBuilder hb = new HeroBuilder();
        hb.setDatabaseIdentifier(heroID)
                .setAttack(attack)
                .setDefense(defense)
                .setHealth(health)
                .setMovement(movement)
                .setVision(vision)
                .setOwnerID(Optional.of(p.getUsername()))
                .setLevel(level)
                .setHeroType(HeroType.valueOf(heroType.toUpperCase()));
        //Here is where we would apply automatic leveling stuff.
        //TODO add in level rewards.
        return hb.createHero();
    }
}