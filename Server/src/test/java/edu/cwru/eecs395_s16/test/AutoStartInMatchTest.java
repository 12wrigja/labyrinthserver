package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 3/23/16.
 */
public abstract class AutoStartInMatchTest extends InMatchTest {

    @Override
    public void setup() throws Exception {
        super.setup();
        initialHeroes = new ArrayList<>(heroBot.getBotsHeroes());
        initialArchitectObjects = new ArrayList<>(architectBot.getArchitectObjects());
        changeHeroesList(heroBot, initialHeroes);
        changeArchitectList(architectBot, initialArchitectObjects);
        super.setupMatch();
    }

    protected void changeHeroesList(Player hero, List<Hero> heroes){

    }

    protected void changeArchitectList(Player architect, List<GameObject> architectObjects){

    }

}
