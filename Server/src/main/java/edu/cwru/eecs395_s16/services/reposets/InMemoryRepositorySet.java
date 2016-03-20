package edu.cwru.eecs395_s16.services.reposets;

import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.services.cache.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.herorepository.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.maprepository.InMemoryMapRepository;
import edu.cwru.eecs395_s16.services.playerrepository.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.InMemorySessionRepository;
import edu.cwru.eecs395_s16.services.weaponrepository.InMemoryWeaponRepository;
import edu.cwru.eecs395_s16.utils.CoreDataParser;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/20/16.
 */
public class InMemoryRepositorySet implements RepositorySet {

    @Override
    public void initialize(List<CoreDataParser.CoreDataEntry> baseData) {

    }

    @Override
    public ServiceContainerBuilder addServicesToContainer(ServiceContainerBuilder scb) {
        scb.setPlayerRepository(new InMemoryPlayerRepository());
        scb.setHeroRepository(new InMemoryHeroRepository());
        scb.setMapRepository(new InMemoryMapRepository());
        scb.setCacheService(new InMemoryCacheService());
        scb.setWeaponRepository(new InMemoryWeaponRepository());
        scb.setSessionRepository(new InMemorySessionRepository());
        return scb;
    }

    @Override
    public void resetToDefaultData(List<CoreDataParser.CoreDataEntry> baseData) {

    }
}
