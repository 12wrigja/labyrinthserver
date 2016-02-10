package edu.cwru.eecs395_s16.ui;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;

import java.io.Console;
import java.io.IOException;
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

        List<Command> cmds = new ArrayList<>();
        Map<String, Command> cmdMap = new HashMap<>();

        //Create all commands
        Command startCMD = new Command("start", "Starts the engine if it is stopped. Will report errors if there is something blocking the engine port.", "interface", "port", "persist", "trace") {
            @Override
            public void run() {
                if (activeEngine == null) {
                    PlayerRepository playerRepo;
                    SessionRepository sessionRepo;
                    String persistText = commandOptionMapping.get("persist");
                    if (persistText == null || Boolean.parseBoolean(persistText)) {
                        //TODO update this so it uses persistent storage
                        playerRepo = new InMemoryPlayerRepository();
                        sessionRepo = new InMemorySessionRepository();
                    } else {
                        playerRepo = new InMemoryPlayerRepository();
                        sessionRepo = new InMemorySessionRepository();
                    }
                    GameEngine engine = new GameEngine(playerRepo, sessionRepo);
                    String serverInterface = commandOptionMapping.get("interface");
                    if (serverInterface != null) {
                        engine.setServerInterface(serverInterface);
                    }
                    try {
                        String portText = commandOptionMapping.get("port");
                        if (portText != null) {
                            int port = Integer.parseInt(commandOptionMapping.get("port"));
                            engine.setServerPort(port);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Specified port is invalid.");
                        return;
                    }
                    try {
                        engine.start();
                        activeEngine = engine;
                    } catch (IOException e) {
                        System.err.println("Unable to start engine - something is running on port " + engine.getServerPort() + ". Try using the linux commands netstat or lsof to determine the offending program and kill it.");
                        if (Boolean.parseBoolean(commandOptionMapping.get("trace"))) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("Engine is already started.");
                }
            }
        };

        Command stopCMD = new Command("stop", "Stops the engine if it is running.") {
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

        Command helpCommand = new Command("help", "Gives information about a command. Run this with no arguments to get a full list of commands", "cmd") {
            @Override
            public void run() {
                String cmd = commandOptionMapping.get("cmd");
                if (cmd != null) {
                    //Requesting specific information on something.
                    Command specificCommand = cmdMap.get(cmd);
                    if (specificCommand != null) {
                        //Print specific command help here.
                    } else {
                        System.err.println("The specified command '" + cmd + "' could not be found. Run 'help' with no arguments for a full list of commands.");
                    }
                } else {
                    for (Command c : cmds) {
                        System.out.println(c.phrase + ":\t" + c.description);
                    }
                }
            }
        };

        Command listAllFunctionsCommand = new Command("functions", "Lists all the socket functions that this game engine supports.") {
            @Override
            public void run() {
                if (activeEngine != null) {
                    List<FunctionDescription> fnList = activeEngine.getAllFunctions();
                    Collections.sort(fnList, (o1, o2) -> o1.humanName.compareTo(o2.humanName));
                    ConsoleTable t = new ConsoleTable();
                    t.setRowHeaders("Function Name","Socket Event Name","Must Authenticate?");
                    for (FunctionDescription fd : fnList) {
                        t.addRow(fd.humanName,fd.name,fd.mustAuthenticate, Arrays.toString(fd.parameters));
                    }
                    System.out.println(t.toString());
                } else {
                    System.err.println("Engine not booted. You need to boot the machine in order to know what functions are available to you. This can be done with the start command.");
                }
            }
        };

        //Technically a stand-in for the actual exit command.
        Command exitCommand = new Command("exit","Exits the engine. Terminates the engine if it running.") {
            @Override
            public void run() {
                if (activeEngine != null) {
                    terminateActiveEngine();
                }
            }
        };

        cmds.add(startCMD);
        cmds.add(stopCMD);
        cmds.add(helpCommand);
        cmds.add(exitCommand);
        cmds.add(listAllFunctionsCommand);

        for (Command cmd : cmds) {
            cmdMap.put(cmd.phrase, cmd);
        }
        char escCode = 0x1B;

        Runtime.getRuntime().addShutdownHook(new Thread(exitCommand::run));

        while (true) {
            System.out.print(">");
            System.out.print(String.format("%c[%dC", escCode, 1));
            String command = scan.nextLine();
            String[] parts = command.trim().split("\\s+");
            if (parts.length >= 1) {
                if(parts[0].equals("exit")){
                    exitCommand.run();
                    break;
                }
                Command c = cmdMap.get(parts[0]);
                if (c != null) {
                    String[] options = Arrays.copyOfRange(parts, 1, parts.length);
                    try {
                        c.execute(options);
                    }catch(Exception e){
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

abstract class Command implements Runnable {

    public final String phrase;
    public Map<String, String> commandOptionMapping;
    public final String description;

    public Command(String phrase, String description, String... opts) {
        this.phrase = phrase;
        this.commandOptionMapping = new HashMap<>();
        this.description = description;
        for (String opt : opts) {
            commandOptionMapping.put(opt, null);
        }
    }

    public void execute(String... optVals) {
        for (String optVal : optVals) {
            String[] temp = optVal.split("=", 2);
            String key = "-" + temp[0];
            String val = temp[1];
            if (commandOptionMapping.keySet().contains(key)) {
                commandOptionMapping.put(key, val);
            }
        }
        this.run();
    }
}
