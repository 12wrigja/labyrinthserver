package edu.cwru.eecs395_s16.services.reposets;

import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.services.cache.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.herorepository.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.maprepository.InMemoryMapRepository;
import edu.cwru.eecs395_s16.services.playerrepository.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.InMemorySessionRepository;
import edu.cwru.eecs395_s16.services.weaponrepository.InMemoryWeaponRepository;
import edu.cwru.eecs395_s16.utils.CoreDataParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/20/16.
 */
public class InMemoryRepositorySet implements RepositorySet {

    public static final InMemoryPlayerRepository PLAYER_REPOSITORY = new InMemoryPlayerRepository();
    public static final InMemoryHeroRepository HERO_REPOSITORY = new InMemoryHeroRepository();
    public static final InMemoryMapRepository MAP_REPOSITORY = new InMemoryMapRepository();
    public static final InMemoryCacheService CACHE_SERVICE = new InMemoryCacheService();
    public static final InMemoryWeaponRepository WEAPON_REPOSITORY = new InMemoryWeaponRepository();
    public static final InMemorySessionRepository SESSION_REPOSITORY = new InMemorySessionRepository();

    @Override
    public void initialize(Map<String,CoreDataParser.CoreDataEntry> baseData) {
        List<CoreDataParser.CoreDataEntry> data = new ArrayList<>(baseData.values());
        Map<String,List<List<String>>> sqlSplitifiedData = new HashMap<>();
        for(CoreDataParser.CoreDataEntry entry : data){
            List<List<String>> sqlVersion = new ArrayList<>();
            for (String line : entry.entries){
                List<String> separated = RepositorySet.sqlCSVSplit(line);
                sqlVersion.add(separated);
            }
            sqlSplitifiedData.put(entry.name,sqlVersion);
        }

        WEAPON_REPOSITORY.initialize(sqlSplitifiedData.get("use_patterns"),sqlSplitifiedData.get("use_pattern_tiles"),sqlSplitifiedData.get("hero_items"));
        HERO_REPOSITORY.initialize(sqlSplitifiedData.get("heroes"));
        PLAYER_REPOSITORY.initialize(sqlSplitifiedData.get("players"));
        MAP_REPOSITORY.initialize(sqlSplitifiedData.get("maps"),sqlSplitifiedData.get("tiles"),sqlSplitifiedData.get("tile_map"),sqlSplitifiedData.get("players"));

    }

    @Override
    public ServiceContainerBuilder addServicesToContainer(ServiceContainerBuilder scb) {
        scb.setPlayerRepository(PLAYER_REPOSITORY);
        scb.setHeroRepository(HERO_REPOSITORY);
        scb.setMapRepository(MAP_REPOSITORY);
        scb.setCacheService(CACHE_SERVICE);
        scb.setWeaponRepository(WEAPON_REPOSITORY);
        scb.setSessionRepository(SESSION_REPOSITORY);
        return scb;
    }

    @Override
    public void resetToDefaultData(Map<String,CoreDataParser.CoreDataEntry> baseData) {

    }
}
