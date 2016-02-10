package edu.cwru.eecs395_s16.ui;

import com.google.common.collect.ImmutableList;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;

import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by james on 2/1/16.
 */
public class mainUI {

    private static GameEngine activeEngine;

    private static Scanner scan;

    public static void main(String[] args) {

        System.out.println("Welcome to Labyrinth Server UI");
        System.out.println("Please enter a command, or type 'help' for a list of all available commands.");
        scan = new Scanner(System.in);

        List<ConsoleCommand> cmds = new ArrayList<>();
        Map<String, ConsoleCommand> cmdMap = new HashMap<>();

        //Create all commands
        ConsoleCommand startCMD = new ConsoleCommand("start", "Starts the engine if it is stopped. Will report errors if there is something blocking the engine port.", "interface", "port", "persist", "trace") {
            @Override
            public void run() {
                if (activeEngine == null) {
                    PlayerRepository playerRepo;
                    SessionRepository sessionRepo;
                    String persistText = getOption("persist");
                    if (persistText == null || Boolean.parseBoolean(persistText)) {
                        //TODO update this so it uses persistent storage
                        playerRepo = new InMemoryPlayerRepository();
                        sessionRepo = new InMemorySessionRepository();
                    } else {
                        playerRepo = new InMemoryPlayerRepository();
                        sessionRepo = new InMemorySessionRepository();
                    }
                    GameEngine engine = new GameEngine(playerRepo, sessionRepo);
                    String serverInterface = getOption("interface");
                    if (serverInterface != null) {
                        engine.setServerInterface(serverInterface);
                    }
                    try {
                        String portText = getOption("port");
                        if (portText != null) {
                            int port = Integer.parseInt(portText);
                            engine.setServerPort(port);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Specified port is invalid.");
                        return;
                    }
                    try {
                        engine.start();
                        activeEngine = engine;
                    } catch (BindException e) {
                        System.err.println("Unable to start engine - something is running on port " + engine.getServerPort() + ". Try using the linux commands netstat or lsof to determine the offending program and kill it.");
                        if (Boolean.parseBoolean(getOption("trace"))) {
                            e.printStackTrace();
                        }
                    } catch (UnknownHostException e){
                        System.err.println("Unable to start engine - the provided host '"+ serverInterface + "' is not valid.");
                        if (Boolean.parseBoolean(getOption("trace"))) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        System.err.println("An unknown error occurred.");
                        if(Boolean.parseBoolean(getOption("trace"))) {
                            e.printStackTrace();
                        } else {
                            System.err.println(" Run this command again with the trace option set to true to print a stacktrace.");
                        }
                    }
                } else {
                    System.err.println("Engine is already started.");
                }
            }
        };

        ConsoleCommand stopCMD = new ConsoleCommand("stop", "Stops the engine if it is running.") {
            @Override
            public void run() {
                if (activeEngine != null) {
                    terminateActiveEngine();
                    System.out.println("Engine successfully stopped.");
                } else {
                    System.err.println("Engine is not running.");
                }
            }
        };

        ConsoleCommand helpCommand = new ConsoleCommand("help", "Gives information about a command. Run this with no arguments to get a full list of commands.", "cmd") {
            @Override
            public void run() {
                String cmd = getOption("cmd");
                if (cmd != null) {
                    //Requesting specific information on something.
                    ConsoleCommand specificCommand = cmdMap.get(cmd);
                    if (specificCommand != null) {
                        //Print specific command help here.
                        System.out.println("Command: "+specificCommand.phrase);
                        System.out.println("Description: "+specificCommand.description);
                        System.out.println("Required Parameters: ");
                        if(specificCommand.requiredParameters.size() > 0) {
                            for (String param : specificCommand.requiredParameters) {
                                System.out.println(" - " + param);
                            }
                        } else {
                            System.out.println("none");
                        }
                        System.out.println("Optional Parameters: ");
                        if(specificCommand.optionalParameters.size() > 0){
                            for (String param : specificCommand.optionalParameters) {
                                System.out.println(" - " + param);
                            }
                        } else {
                            System.out.println("none");
                        }
                    } else {
                        System.err.println("The specified command '" + cmd + "' could not be found. Run 'help' with no arguments for a full list of commands.");
                    }
                } else {
                    ConsoleTable tbl = new ConsoleTable();
                    tbl.setRowHeaders("Command", "Description", "Required Parameters", "Optional Parameters");
                    for (ConsoleCommand c : cmds) {
                        tbl.addRow(c.phrase, c.description, c.requiredParameters.toString().replaceAll("\\[|\\]",""), c.optionalParameters.toString().replaceAll("\\[|\\]",""));
                    }
                    System.out.println(tbl);
                }
            }
        };

        ConsoleCommand listAllFunctionsCommand = new ConsoleCommand("functions", "Lists all the socket functions that this game engine supports.") {
            @Override
            public void run() {
                if (activeEngine != null) {
                    List<FunctionDescription> fnList = activeEngine.getAllFunctions();
                    Collections.sort(fnList, (o1, o2) -> o1.humanName.compareTo(o2.humanName));
                    ConsoleTable t = new ConsoleTable();
                    t.setRowHeaders("Function Name", "Socket Event Name", "Must Authenticate?");
                    for (FunctionDescription fd : fnList) {
                        t.addRow(fd.humanName, fd.name, fd.mustAuthenticate, Arrays.toString(fd.parameters));
                    }
                    System.out.println(t.toString());
                } else {
                    System.err.println("Engine not booted. You need to boot the machine in order to know what functions are available to you. This can be done with the start command.");
                }
            }
        };

        ConsoleCommand describeSpecificFunction = new ConsoleCommand("function", "Describes the function given by the fn parameter", "*fn") {
            @Override
            public void run() {

            }
        };

        //Technically a stand-in for the actual exit command.
        ConsoleCommand exitCommand = new ConsoleCommand("exit", "Exits the engine. Terminates the engine if it running.") {
            @Override
            public void run() {
                scan.close();
                if (activeEngine != null) {
                    terminateActiveEngine();
                }
            }
        };

        cmds.add(startCMD);
        cmds.add(stopCMD);
        cmds.add(helpCommand);
        cmds.add(exitCommand);
        cmds.add(describeSpecificFunction);
        cmds.add(listAllFunctionsCommand);

        for (ConsoleCommand cmd : cmds) {
            cmdMap.put(cmd.phrase, cmd);
        }
        char escCode = 0x1B;

        Runtime.getRuntime().addShutdownHook(new Thread(exitCommand::run));

        while (true) {
            System.out.print(">");
            System.out.print(String.format("%c[%dC", escCode, 1));
            String command;
            try {
                command = scan.nextLine();
            }catch(Exception e){
                exitCommand.run();
                break;
            }
            String[] parts = command.trim().split("\\s+");
            if (parts.length >= 1) {
                if (parts[0].equals(exitCommand.phrase)) {
                    exitCommand.run();
                    break;
                }
                ConsoleCommand c = cmdMap.get(parts[0]);
                if (c != null) {
                    String[] options = Arrays.copyOfRange(parts, 1, parts.length);
                    try {
                        c.execute(options);
                    } catch (Exception e) {
                        e.printStackTrace();
                        exitCommand.run();
                        break;
                    }
                } else {
                    System.err.println("Unrecognized command: " + parts[0]);
                }
            } else {
                System.out.println();
            }
        }
    }

    private static void terminateActiveEngine() {
        if (activeEngine != null) {
            activeEngine.stop();
            activeEngine = null;
        }
    }
}

abstract class ConsoleCommand implements Runnable {

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
        requiredParameters = ImmutableList.copyOf(reqParams);
        optionalParameters = ImmutableList.copyOf(optParams);
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
