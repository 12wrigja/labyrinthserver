package edu.cwru.eecs395_s16.services.reposets;

import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.utils.CoreDataParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/20/16.
 */
public interface RepositorySet {

    void initialize(Map<String,CoreDataParser.CoreDataEntry> baseData);
    ServiceContainerBuilder addServicesToContainer(ServiceContainerBuilder scb);
    void resetToDefaultData(Map<String,CoreDataParser.CoreDataEntry> baseData);
    static List<String> sqlCSVSplit(String line){
        char[] characters = line.toCharArray();
        List<String> parts = new ArrayList<>();
        StringBuilder pb = new StringBuilder();
        boolean inside = false;
        for(int i=0; i<characters.length; i++){
            char c = characters[i];
            if(c == '\''){
                if(i-1>=0 && characters[i-1] != '\\'){
                    inside = !inside;
                }
            }
            if(!inside && Character.isWhitespace(c)){
                continue;
            }
            if(c!=',' || inside){
                pb.append(c);
            } else {
                parts.add(pb.toString());
                pb.setLength(0);
            }
        }
        String finalStr = pb.toString();
        if(finalStr.length() > 0){
            parts.add(finalStr);
        }
        return parts;
    }

}
