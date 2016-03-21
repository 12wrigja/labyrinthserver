package edu.cwru.eecs395_s16.services.weaponrepository;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.UsePattern;
import edu.cwru.eecs395_s16.interfaces.objects.Weapon;
import edu.cwru.eecs395_s16.interfaces.repositories.WeaponRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by james on 3/17/16.
 */
public class InMemoryWeaponRepository implements WeaponRepository {

    Map<Integer,Weapon> weaponMap;

    public InMemoryWeaponRepository() {
        weaponMap = new HashMap<>();
    }

    public void initialize(List<List<String>> use_patterns,List<List<String>> use_pattern_tiles,List<List<String>> hero_items){
        weaponMap.clear();
        //Build Attack Patterns from the list
        Map<Integer, UsePattern> patternMap = new HashMap<>();
        for(List<String> usePattern : use_patterns){
            Map<Location, Float> pattern = new HashMap<>();
            int id = patternMap.size()+1;
            use_pattern_tiles.stream().filter(lst -> Integer.parseInt(lst.get(0)) == id).forEach(lst -> {
                pattern.put(new Location(Integer.parseInt(lst.get(1)),Integer.parseInt(lst.get(2))),100f/Integer.parseInt(lst.get(3)));
            });
            int numInputs = Integer.parseInt(usePattern.get(1));
            boolean isRotatable = Boolean.parseBoolean(usePattern.get(2));
            UsePattern p = new UsePattern(numInputs,isRotatable,pattern);
            patternMap.put(id,p);
        }
        hero_items.stream().filter(lst -> lst.get(3).equals("weapon")).forEach(lst -> {
            String name = lst.get(1);
            String image = lst.get(2);
            String description = lst.get(3);
            int attackModifier = Integer.parseInt(lst.get(6));
            int range = Integer.parseInt(lst.get(12));
            int patternID = Integer.parseInt(lst.get(11));
            Weapon w = new Weapon(weaponMap.size()+1,image,name,description,range,attackModifier,patternMap.get(patternID));
            weaponMap.put(weaponMap.size()+1,w);
        });
    }

    @Override
    public Optional<Weapon> getWeaponForId(int id) {
        if(weaponMap.containsKey(id)){
            return Optional.of(weaponMap.get(id));
        } else {
            return Optional.empty();
        }

    }
}
