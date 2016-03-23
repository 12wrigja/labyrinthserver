package edu.cwru.eecs395_s16.services.containers;

import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.util.Map;

/**
 * Created by james on 3/20/16.
 */
public interface Repository {
    void initialize(Map<String,CoreDataUtils.CoreDataEntry> baseData);
    void resetToDefaultData(Map<String,CoreDataUtils.CoreDataEntry> baseData);

}
