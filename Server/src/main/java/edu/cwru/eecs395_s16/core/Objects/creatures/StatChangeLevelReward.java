package edu.cwru.eecs395_s16.core.objects.creatures;

import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.LevelReward;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by james on 3/23/16.
 */
public class StatChangeLevelReward extends LevelReward {

    public enum StatChanged {
        ATTACK("a"),
        DEFENSE("d"),
        VISION("v"),
        MOVEMENT("m"),
        HEALTH("h");
        public final String string;
        StatChanged(String str){
            this.string = str;
        }
        public static StatChanged getStat(String val){
            for(StatChanged stat : StatChanged.values()){
                if(stat.string.equals(val)){
                    return stat;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    private final Map<StatChanged,Integer> statChangeMap;

    public StatChangeLevelReward(int levelAwarded, long expThreshold, HeroType heroType, String reward){
        super(levelAwarded,expThreshold,heroType);
        statChangeMap = new HashMap<>();
        List<Character> chars = reward.chars().mapToObj(e->(char)e).collect(Collectors.toList());
        Iterator<Character> it = chars.iterator();
        while(it.hasNext()) {
            Character c = it.next();
            if(c == '+' || c == '-'){
                int value = 0;
                while (it.hasNext()) {
                    Character t = it.next();
                    if(Character.isDigit(t)) {
                        value *= 10;
                        value += Character.getNumericValue(t);
                    } else {
                        c = t;
                        break;
                    }
                }
                if(c == '-'){
                    value *= -1;
                }
                String statStr = String.valueOf(c);
                StatChanged stat = StatChanged.getStat(statStr);
                statChangeMap.put(stat,value);
            }
        }
        if(statChangeMap.size() == 0){
            throw new IllegalArgumentException("Unable to parse stat changes from that reward.");
        }
    }

    @Override
    public void apply(Hero h) {
        if(h.getHeroType() == heroType && h.getExp() >= expThreshold) {
            super.apply(h);
            System.out.print("Applying level "+levelApplied+". Rewards: ");
            for (Map.Entry<StatChanged, Integer> entry : statChangeMap.entrySet()) {
                StatChanged stat = entry.getKey();
                Integer change = entry.getValue();
                switch (stat) {
                    case ATTACK:
                        System.out.print("Incrementing Attack by "+change+". ");
                        h.setAttack(h.getAttack() + change);
                        break;
                    case DEFENSE:
                        System.out.print("Incrementing Defense by "+change+". ");
                        h.setDefense(h.getDefense() + change);
                        break;
                    case HEALTH:
                        System.out.print("Incrementing Health by "+change+". ");
                        h.setMaxHealth(h.getHealth() + change);
                        break;
                    case VISION:
                        System.out.print("Incrementing Vision by "+change+". ");
                        h.setVision(h.getVision() + change);
                        break;
                    case MOVEMENT:
                        System.out.print("Incrementing Movement by "+change+". ");
                        h.setMovement(h.getMovement() + change);
                        break;
                }
            }
            System.out.println();
        }
    }
}
