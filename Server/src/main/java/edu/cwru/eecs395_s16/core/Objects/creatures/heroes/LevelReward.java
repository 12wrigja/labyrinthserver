package edu.cwru.eecs395_s16.core.objects.creatures.heroes;

import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;

/**
 * Created by james on 3/20/16.
 */
public class LevelReward {

    public final int levelApplied;
    public final long expThreshold;
    public final HeroType heroType;

    protected LevelReward(int levelApplied, long expThreshold, HeroType heroType) {
        this.levelApplied = levelApplied;
        this.expThreshold = expThreshold;
        this.heroType = heroType;
    }

    public void apply(Hero h){}
}
