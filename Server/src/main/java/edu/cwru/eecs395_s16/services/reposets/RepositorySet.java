package edu.cwru.eecs395_s16.services.reposets;

import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/20/16.
 */
public interface RepositorySet {

    void initialize(Map<String,List<String>> baseData);
    ServiceContainerBuilder addServicesToContainer(ServiceContainerBuilder scb);
    void resetToDefaultData();

}
