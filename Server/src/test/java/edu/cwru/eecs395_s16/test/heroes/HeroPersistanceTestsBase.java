package edu.cwru.eecs395_s16.test.heroes;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.networking.requests.RegisterUserRequest;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.TestBot;
import edu.cwru.eecs395_s16.test.EngineOnlyTest;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 3/24/16.
 */
public abstract class HeroPersistanceTestsBase extends EngineOnlyTest {

    @Test
    public void testBotHeroPersistance(){
        GameBot b = new TestBot();
        InternalResponseObject<List<Hero>> heroes = GameEngine.instance().services.heroRepository.getPlayerHeroes(b);
        if(heroes.isNormal()){
            assert(heroes.get().size() > 0);
            Hero randomHero = heroes.get().get(0);
            HeroType heroType = randomHero.getHeroType();
            randomHero.grantXP(20000);
            InternalResponseObject<List<Hero>> heroesReretrieved = GameEngine.instance().services.heroRepository.getPlayerHeroes(b);
            if(heroesReretrieved.isNormal()){
                Optional<Hero> sameHeroResp = heroesReretrieved.get().stream().filter(hero -> hero.getHeroType() == heroType).findFirst();
                if(!sameHeroResp.isPresent()){
                    fail("Unable to retrieve hero with the same hero type.");
                } else {
                    Hero sameHero = sameHeroResp.get();
                    assertEquals(randomHero.getExp(),sameHero.getExp());
                    assertEquals(randomHero.getAttack(), sameHero.getAttack());
                    assertEquals(randomHero.getDefense(), sameHero.getDefense());
                    assertEquals(randomHero.getHealth(), sameHero.getHealth());
                    assertEquals(randomHero.getVision(), sameHero.getVision());
                    assertEquals(randomHero.getMovement(), sameHero.getMovement());
                    assertEquals(randomHero.getWeapon(), sameHero.getWeapon());
                }
            } else {
                fail("Unable to retrieve heroes from storage.");
            }
        } else {
            fail("Unable to retrieve heroes from storage.");
        }
    }

    @Test
    public void testActualPlayerHeroPersistance(){
        RegisterUserRequest req = new RegisterUserRequest("testusername","testpassword","testpassword");
        InternalResponseObject<Player> playerRegisterResp = engine.networkingInterface.register(req);
        if(!playerRegisterResp.isNormal()){
            fail("Unable to register a real player. ERROR:"+playerRegisterResp.getMessage());
        }
        Player b = playerRegisterResp.get();
        InternalResponseObject<List<Hero>> heroes = GameEngine.instance().services.heroRepository.getPlayerHeroes(b);
        if(heroes.isNormal()){
            assert(heroes.get().size() > 0);
            Hero randomHero = heroes.get().get(0);
            HeroType heroType = randomHero.getHeroType();
            InternalResponseObject<Boolean> grantXPResp = randomHero.grantXP(20000);
            if(!grantXPResp.isNormal()){
                GameEngine.instance().services.playerRepository.deletePlayer(b);
                fail("Unable to update hero. ERROR:"+grantXPResp.getMessage());
            }
            InternalResponseObject<List<Hero>> heroesReretrieved = GameEngine.instance().services.heroRepository.getPlayerHeroes(b);
            if(heroesReretrieved.isNormal()){
                Optional<Hero> sameHeroResp = heroesReretrieved.get().stream().filter(hero -> hero.getHeroType() == heroType).findFirst();
                if(!sameHeroResp.isPresent()){
                    GameEngine.instance().services.playerRepository.deletePlayer(b);
                    fail("Unable to retrieve hero with the same hero type.");
                } else {
                    Hero sameHero = sameHeroResp.get();
                    try {
                        assertEquals(randomHero.getExp(), sameHero.getExp());
                        assertEquals(randomHero.getAttack(), sameHero.getAttack());
                        assertEquals(randomHero.getDefense(), sameHero.getDefense());
                        assertEquals(randomHero.getHealth(), sameHero.getHealth());
                        assertEquals(randomHero.getVision(), sameHero.getVision());
                        assertEquals(randomHero.getMovement(), sameHero.getMovement());
                        assertEquals(randomHero.getWeapon(), sameHero.getWeapon());
                    } catch (AssertionError e){
                        GameEngine.instance().services.playerRepository.deletePlayer(b);
                        throw e;
                    }
                }
            } else {
                GameEngine.instance().services.playerRepository.deletePlayer(b);
                fail("Unable to retrieve heroes from storage.");
            }
        } else {
            GameEngine.instance().services.playerRepository.deletePlayer(b);
            fail("Unable to retrieve heroes from storage.");
        }
        GameEngine.instance().services.playerRepository.deletePlayer(b);
    }

}
