package edu.cwru.eecs395_s16.ui;

import java.util.*;

/**
 * Created by james on 2/16/16.
 */
public abstract class ConsoleCommand implements Runnable {

    public final String phrase;
    private Map<String, String> commandOptionMapping;
    public final String description;
    public final List<String> requiredParameters;
    public final List<String> optionalParameters;
    private final String[] orderedParams;

    public ConsoleCommand(String phrase, String description, String... opts) {
        this.phrase = phrase;
        this.commandOptionMapping = new HashMap<>();
        this.description = description;
        List<String> reqParams = new ArrayList<>();
        List<String> optParams = new ArrayList<>();
        this.orderedParams = new String[opts.length];
        for (int i=0; i<opts.length; i++) {
            String opt = opts[i];
            if (opt.charAt(0) == '*') {
                opt = opt.substring(1);
                reqParams.add(opt);
            } else {
                optParams.add(opt);
            }
            orderedParams[i] = opt;
        }
        setInitialCommandOpts();
        requiredParameters = Collections.unmodifiableList(reqParams);
        optionalParameters = Collections.unmodifiableList(optParams);
    }

    public final void execute(String... optVals) {
        setInitialCommandOpts();
        Set<String> specifiedParams = new HashSet<>();
        List<String> unspecifiedParamStrings = new ArrayList<>();
        for(String optVal : optVals){
            if(optVal.contains("=")){
                String[] temp = optVal.split("=", 2);
                storeOption(temp[0],temp[1]);
                specifiedParams.add(temp[0]);
            } else {
                unspecifiedParamStrings.add(optVal);
            }
        }
        int insertIndex = 0;
        for (String optVal : unspecifiedParamStrings) {
            while(true){
                if(insertIndex >= orderedParams.length){
                    System.err.println("Unable to insert parameter '"+optVal+"'");
                    break;
                }
                String key = orderedParams[insertIndex];
                if(specifiedParams.contains(key)) {
                    insertIndex++;
                } else {
                    storeOption(key,optVal);
                    insertIndex++;
                    break;
                }
            }
        }
        for (String requiredOpt : requiredParameters) {
            if (getOption(requiredOpt) == null) {
                System.err.println("Missing required parameter: " + requiredOpt);
                return;
            }
        }
        this.run();
    }

    private void storeOption(String key, String value){
        if (commandOptionMapping.keySet().contains(key)) {
            commandOptionMapping.put(key, value);
        }
    }

    private void setInitialCommandOpts(){
        commandOptionMapping = new HashMap<>();
        for(String key: orderedParams){
            commandOptionMapping.put(key,null);
        }
    }

    final String getOption(String key) {
        return commandOptionMapping.get(key);
    }
}
