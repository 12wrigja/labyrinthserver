package edu.cwru.eecs395_s16.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by james on 3/20/16.
 */
public class CoreDataParser {


    public static final Pattern startDataSetPattern = Pattern.compile("start (.*)?\\r?\\n?",Pattern.CASE_INSENSITIVE);
    public static final Pattern endDataSetPattern = Pattern.compile("end (.*)\\r?\\n?",Pattern.CASE_INSENSITIVE);

    public static Map<String,List<String>> parse(String fileName){
        Map<String,List<String>> data = new HashMap<>();
        Scanner scan = null;
        try{
            scan = new Scanner(new BufferedReader(new FileReader(fileName)));
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                Matcher startMatcher = startDataSetPattern.matcher(line);
                if(startMatcher.matches()){
                    String dataSetName = startMatcher.group(1);
                    List<String> dataStore;
                    if(!data.containsKey(dataSetName)){
                        dataStore = new ArrayList<>();
                        data.put(dataSetName,dataStore);
                    } else {
                        dataStore = data.get(dataSetName);
                    }
                    boolean seenEndOfDataset = false;
                    while(scan.hasNextLine()){
                        String innerLine = scan.nextLine();
                        if(endDataSetPattern.matcher(innerLine).matches()){
                            seenEndOfDataset = true;
                            break;
                        } else {
                            dataStore.add(innerLine);
                        }
                    }
                    if(!seenEndOfDataset){
                        throw new IllegalArgumentException("Data set "+dataSetName+" does not have a matching end.");
                    }
                }
            }

        } catch (Exception e){
            throw new IllegalArgumentException("Something is wrong with the core data file or format.");
        } finally {
            if(scan != null){
                scan.close();
            }
        }
        return data;
    }

    public static void main(String[] args){
        String file = "new_base_data.data";
        Map<String,List<String>> data = CoreDataParser.parse(file);
        for(String key : data.keySet()){
            System.out.println(key);
        }
    }

}
