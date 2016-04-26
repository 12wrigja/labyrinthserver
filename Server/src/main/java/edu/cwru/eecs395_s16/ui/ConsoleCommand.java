package edu.cwru.eecs395_s16.ui;

import java.util.*;

/**
 * A ConsoleCommand is used to create a command that does something in the
 * Console UI. Commands have several parts: The phrase (such as help) which
 * is the main command, the description (which is printed out to describe the
 * function), and the parameters.
 * <p>
 * Parameters can be required or optional, and
 * they all have names. To make a parameter required, its name is
 * specified with an asterisk in front of it (such as *port). These names
 * (such as persist or trace) are used as keys in the parameter map available
 * within the class. Using the parameter names in general is not required.
 * <p>
 * When a command is run, parameter parsing occurs on the string and the
 * results of that are placed into the parameter map. If names are used for
 * parameters, those are respected first. Otherwise, the order in which the
 * parameters are defined in the constructor is respected.
 */
public abstract class ConsoleCommand implements Runnable {

    public final String phrase;
    public final String description;
    public final List<String> requiredParameters;
    public final List<String> optionalParameters;
    private final String[] orderedParams;
    private Map<String, String> commandOptionMapping;

    public ConsoleCommand(String phrase, String description, String... opts) {
        this.phrase = phrase;
        this.commandOptionMapping = new HashMap<>();
        this.description = description;
        List<String> reqParams = new ArrayList<>();
        List<String> optParams = new ArrayList<>();
        this.orderedParams = new String[opts.length];
        for (int i = 0; i < opts.length; i++) {
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
        for (String optVal : optVals) {
            if (optVal.contains("=")) {
                String[] temp = optVal.split("=", 2);
                storeOption(temp[0], temp[1]);
                specifiedParams.add(temp[0]);
            } else {
                unspecifiedParamStrings.add(optVal);
            }
        }
        int insertIndex = 0;
        for (String optVal : unspecifiedParamStrings) {
            while (true) {
                if (insertIndex >= orderedParams.length) {
                    System.err.println("Unable to insert parameter '" +
                            optVal + "'");
                    break;
                }
                String key = orderedParams[insertIndex];
                if (specifiedParams.contains(key)) {
                    insertIndex++;
                } else {
                    storeOption(key, optVal);
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

    private void storeOption(String key, String value) {
        if (commandOptionMapping.keySet().contains(key)) {
            commandOptionMapping.put(key, value);
        }
    }

    private void setInitialCommandOpts() {
        commandOptionMapping = new HashMap<>();
        for (String key : orderedParams) {
            commandOptionMapping.put(key, null);
        }
    }

    final String getOption(String key) {
        return commandOptionMapping.get(key);
    }
}
