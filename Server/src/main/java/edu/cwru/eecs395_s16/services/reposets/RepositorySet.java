package edu.cwru.eecs395_s16.services.reposets;

import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.utils.CoreDataParser;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/20/16.
 */
public interface RepositorySet {

    void initialize(List<CoreDataParser.CoreDataEntry> baseData);
    ServiceContainerBuilder addServicesToContainer(ServiceContainerBuilder scb);
    void resetToDefaultData(List<CoreDataParser.CoreDataEntry> baseData);

}
