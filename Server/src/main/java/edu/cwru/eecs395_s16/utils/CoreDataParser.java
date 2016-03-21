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

    public static Map<String,CoreDataEntry> parse(String fileName){
        Map<String, CoreDataEntry> entries = new HashMap<>();
        Scanner scan = null;
        try{
            scan = new Scanner(new BufferedReader(new FileReader(fileName)));
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                Matcher startMatcher = startDataSetPattern.matcher(line);
                if(startMatcher.matches()){
                    String dataSetName = startMatcher.group(1);
                    CoreDataEntry dataStore;
                    if(!entries.containsKey(dataSetName)){
                        dataStore = new CoreDataEntry(entries.size()+1,dataSetName);
                        entries.put(dataSetName,dataStore);
                    } else {
                        dataStore = entries.get(dataSetName);
                    }
                    boolean seenEndOfDataset = false;
                    while(scan.hasNextLine()){
                        String innerLine = scan.nextLine();
                        if(endDataSetPattern.matcher(innerLine).matches()){
                            seenEndOfDataset = true;
                            break;
                        } else {
                            dataStore.addEntry(innerLine);
                        }
                    }
                    if(!seenEndOfDataset){
                        throw new IllegalArgumentException("Data set "+dataSetName+" does not have a matching end.");
                    }
                }
            }

        } catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException("Something is wrong with the core data file or format.");
        } finally {
            if(scan != null){
                scan.close();
            }
        }
        return entries;
    }

    public static class CoreDataEntry {
        public final int order;
        public final String name;
        public final List<String> entries;

        public CoreDataEntry(int order, String name) {
            this.order = order;
            this.name = name;
            this.entries = new ArrayList<>();
        }

        public void addEntry(String entry){
            entries.add(entry);
        }
    }

    public static void main(String[] args){
        String file = "new_base_data.data";
        Map<String,CoreDataEntry> data = CoreDataParser.parse(file);
        for(CoreDataEntry key : data.values()){
            System.out.println(key.name);
        }
    }

}
