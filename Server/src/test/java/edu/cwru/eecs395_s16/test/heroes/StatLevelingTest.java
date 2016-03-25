package edu.cwru.eecs395_s16.test.heroes;

import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.TestBot;
import edu.cwru.eecs395_s16.test.EngineOnlyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by james on 3/24/16.
 */
public class StatLevelingTest extends EngineOnlyTest {

    @Test
    public void testBasicLevelUpWarrior() {
        GameBot heroBot = new TestBot();
        long exp = 1000;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.WARRIOR).setExp(exp, true).createHero();
        //Changed
        assertEquals(exp, h.getExp());
        assertEquals(2, h.getLevel());
        assertEquals(15, h.getAttack());
        //Should be the same
        assertEquals(10, h.getDefense());
        assertEquals(70, h.getHealth());
        assertEquals(5, h.getVision());
        assertEquals(3, h.getMovement());
    }

    @Test
    public void testMultipleLevelUpWarrior() {
        GameBot heroBot = new TestBot();
        long exp = 3500;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.WARRIOR).setExp(exp, true).createHero();
        //Changed
        assertEquals(exp, h.getExp());
        assertEquals(4, h.getLevel());
        assertEquals(15, h.getAttack());
        assertEquals(80, h.getHealth());
        //Should be the same
        assertEquals(10, h.getDefense());
        assertEquals(5, h.getVision());
        assertEquals(3, h.getMovement());
    }

    @Test
    public void testIncrementalLevelUpWarrior() {
        GameBot heroBot = new TestBot();
        long exp = 1000;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.WARRIOR).setExp(exp, true).createHero();
        //Chagned
        assertEquals(exp, h.getExp());
        assertEquals(2, h.getLevel());
        assertEquals(15, h.getAttack());
        //Should be the same
        assertEquals(10, h.getDefense());
        assertEquals(70, h.getHealth());
        assertEquals(5, h.getVision());
        assertEquals(3, h.getMovement());

        h.grantXP(2500);
        //Changed
        assertEquals(3500, h.getExp());
        assertEquals(4, h.getLevel());
        assertEquals(15, h.getAttack());
        assertEquals(80, h.getHealth());
        //Should be the same
        assertEquals(10, h.getDefense());
        assertEquals(5, h.getVision());
        assertEquals(3, h.getMovement());
    }

    @Test
    public void testFullLevelUpRogue() {
        GameBot heroBot = new TestBot();
        long exp = 0;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.ROGUE).setExp(exp, true).createHero();
        //Changed
        assertEquals(exp, h.getExp());
        assertEquals(1, h.getLevel());
        //Should be the same
        assertEquals(10, h.getAttack());
        assertEquals(0, h.getDefense());
        assertEquals(50, h.getHealth());
        assertEquals(7, h.getVision());
        assertEquals(4, h.getMovement());

        h.grantXP(20000);
        //Changed
        assertEquals(20000, h.getExp());
        assertEquals(20, h.getLevel());
        assertEquals(25, h.getAttack());
        assertEquals(5, h.getDefense());
        assertEquals(70, h.getHealth());
        assertEquals(8, h.getVision());
        assertEquals(6, h.getMovement());
    }

    @Test
    public void testFullLevelUpMage() {
        GameBot heroBot = new TestBot();
        long exp = 0;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.MAGE).setExp(exp, true).createHero();
        //Changed
        assertEquals(exp, h.getExp());
        assertEquals(1, h.getLevel());
        //Should be the same
        assertEquals(15, h.getAttack());
        assertEquals(0, h.getDefense());
        assertEquals(50, h.getHealth());
        assertEquals(7, h.getVision());
        assertEquals(3, h.getMovement());

        h.grantXP(20000);
        //Changed
        assertEquals(20000, h.getExp());
        assertEquals(20, h.getLevel());
        assertEquals(40, h.getAttack());
        assertEquals(0, h.getDefense());
        assertEquals(70, h.getHealth());
        assertEquals(9, h.getVision());
        assertEquals(4, h.getMovement());
    }

    @Test
    public void testFullLevelUpWarriorRogue() {
        GameBot heroBot = new TestBot();
        long exp = 0;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.WARRIORROGUE).setExp(exp, true).createHero();
        //Changed
        assertEquals(exp, h.getExp());
        assertEquals(1, h.getLevel());
        //Should be the same
        assertEquals(10, h.getAttack());
        assertEquals(10, h.getDefense());
        assertEquals(50, h.getHealth());
        assertEquals(5, h.getVision());
        assertEquals(4, h.getMovement());

        h.grantXP(20000);
        //Changed
        assertEquals(20000, h.getExp());
        assertEquals(20, h.getLevel());
        assertEquals(25, h.getAttack());
        assertEquals(20, h.getDefense());
        assertEquals(90, h.getHealth());
        assertEquals(6, h.getVision());
        assertEquals(5, h.getMovement());
    }

    @Test
    public void testFullLevelUpWarriorMage() {
        GameBot heroBot = new TestBot();
        long exp = 0;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.WARRIORMAGE).setExp(exp, true).createHero();
        //Changed
        assertEquals(exp, h.getExp());
        assertEquals(1, h.getLevel());
        //Should be the same
        assertEquals(15, h.getAttack());
        assertEquals(0, h.getDefense());
        assertEquals(50, h.getHealth());
        assertEquals(5, h.getVision());
        assertEquals(4, h.getMovement());

        h.grantXP(20000);
        //Changed
        assertEquals(20000, h.getExp());
        assertEquals(20, h.getLevel());
        assertEquals(35, h.getAttack());
        assertEquals(5, h.getDefense());
        assertEquals(80, h.getHealth());
        assertEquals(6, h.getVision());
        assertEquals(5, h.getMovement());
    }

    @Test
    public void testFullLevelUpRogueMage() {
        GameBot heroBot = new TestBot();
        long exp = 0;
        Hero h = new HeroBuilder(heroBot.getUsername(), HeroType.ROGUEMAGE).setExp(exp, true).createHero();
        //Changed
        assertEquals(exp, h.getExp());
        assertEquals(1, h.getLevel());
        //Should be the same
        assertEquals(10, h.getAttack());
        assertEquals(5, h.getDefense());
        assertEquals(50, h.getHealth());
        assertEquals(7, h.getVision());
        assertEquals(3, h.getMovement());

        h.grantXP(20000);
        //Changed
        assertEquals(20000, h.getExp());
        assertEquals(20, h.getLevel());
        assertEquals(30, h.getAttack());
        assertEquals(10, h.getDefense());
        assertEquals(70, h.getHealth());
        assertEquals(8, h.getVision());
        assertEquals(4, h.getMovement());
    }
}
