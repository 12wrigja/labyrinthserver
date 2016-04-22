package edu.cwru.eecs395_s16.core.objects.creatures.heroes;

/**
 * Created by james on 3/20/16.
 */
public class LevelReward {

    public final int levelApplied;
    public final long expThreshold;
    public final HeroType heroType;

    public LevelReward(int levelApplied, long expThreshold, HeroType heroType) {
        this.levelApplied = levelApplied;
        this.expThreshold = expThreshold;
        this.heroType = heroType;
    }

    public void apply(Hero h) {
        if (h.getHeroType() == heroType && h.getExp() >= expThreshold) {
            h.setLevel(levelApplied);
        }
    }
}
