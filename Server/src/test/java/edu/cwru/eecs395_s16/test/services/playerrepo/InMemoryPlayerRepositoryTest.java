package edu.cwru.eecs395_s16.test.services.playerrepo;

import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;
import edu.cwru.eecs395_s16.services.playerrepository.InMemoryPlayerRepository;

/**
 * Created by james on 2/29/16.
 */
public class InMemoryPlayerRepositoryTest extends PlayerRepositoryBaseTest {

    InMemoryPlayerRepository repo = new InMemoryPlayerRepository();

    @Override
    public PlayerRepository getRepositoryImplementation() {
        return repo;
    }
}
