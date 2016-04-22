package edu.cwru.eecs395_s16.services.heroitems;

import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Equipment;
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

    Map<Integer, Weapon> weaponMap;

    public InMemoryHeroItemRepository() {
        weaponMap = new HashMap<>();
    }

    @Override
    public Optional<Weapon> getWeaponForId(int id) {
        if (weaponMap.containsKey(id)) {
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
        for (List<String> usePattern : use_patterns) {
            Map<Location, Float> pattern = new HashMap<>();
            int id = patternMap.size() + 1;
            use_pattern_tiles.stream().filter(lst -> Integer.parseInt(lst.get(0)) == id).forEach(lst -> {
                pattern.put(new Location(Integer.parseInt(lst.get(1)), Integer.parseInt(lst.get(2))), ((float) Integer.parseInt(lst.get(3))) / 100f);
            });
            int numInputs = Integer.parseInt(usePattern.get(1));
            boolean isRotatable = Boolean.parseBoolean(usePattern.get(2));
            UsePattern p = new UsePattern(numInputs, isRotatable, pattern);
            patternMap.put(id, p);
        }
        int id = 0;
        for (List<String> lst : hero_items) {
            id += 1;
            if (lst.get(4).equals("weapon")) {
                String name = lst.get(1);
                String image = lst.get(2);
                String description = lst.get(3);
                int attackModifier = Integer.parseInt(lst.get(5));
                int range = Integer.parseInt(lst.get(11));
                int patternID = Integer.parseInt(lst.get(10));
                UsePattern p = patternMap.get(patternID);
                if (p == null) {
                    throw new IllegalArgumentException("Use Pattern incorrectly configured for weapon with id: " + id);
                }
                Weapon w = new Weapon(id, image, name, description, range, attackModifier, p);
                weaponMap.put(id, w);
            } else if (lst.get(4).equals("equipment")) {
                //This is where equipment would go.
            }
        }
    }

    @Override
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        weaponMap.clear();
        initialize(baseData);
    }
}
