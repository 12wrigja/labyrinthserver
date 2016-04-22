package edu.cwru.eecs395_s16.core.objects.creatures.heroes;

/**
 * Created by james on 2/18/16.
 */
public enum HeroType {
    WARRIOR(1),
    ROGUE(2),
    MAGE(3),
    WARRIORROGUE(4),
    WARRIORMAGE(5),
    ROGUEMAGE(6);

    public final int databaseIdentifier;

    HeroType(int id) {
        this.databaseIdentifier = id;
    }
}
