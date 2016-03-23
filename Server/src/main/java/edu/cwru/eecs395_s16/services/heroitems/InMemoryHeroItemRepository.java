package edu.cwru.eecs395_s16.services.heroitems;

import edu.cwru.eecs395_s16.core.objects.creatures.Equipment;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.UsePattern;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by james on 3/17/16.
 */
public class InMemoryHeroItemRepository implements HeroItemRepository {

    Map<Integer,Weapon> weaponMap;

    public InMemoryHeroItemRepository() {
        weaponMap = new HashMap<>();
    }

    public void initialize(List<List<String>> use_patterns,List<List<String>> use_pattern_tiles,List<List<String>> hero_items){
        weaponMap.clear();

    }

    @Override
    public Optional<Weapon> getWeaponForId(int id) {
        if(weaponMap.containsKey(id)){
            return Optional.of(weaponMap.get(id));
        } else {
            return Optional.empty();
        }

    }

    @Override
    public Optional<Equipment> getEquipmentForId(int id) {
        //TODO fill this in eventually
        return null;
    }

    @Override
    public void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        List<List<String>> use_patterns = CoreDataUtils.splitEntries(baseData.get("use_patterns"));
        List<List<String>> use_pattern_tiles = CoreDataUtils.splitEntries(baseData.get("use_pattern_tiles"));
        List<List<String>> hero_items = CoreDataUtils.splitEntries(baseData.get("hero_items"));
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
        hero_items.stream().filter(lst -> lst.get(5).equals("weapon")).forEach(lst -> {
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
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        weaponMap.clear();
        initialize(baseData);
    }
}
