package edu.cwru.eecs395_s16.ui;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;

import java.io.IOException;
import java.util.*;

/**
 * Created by james on 2/1/16.
 */
public class mainUI {

    private static GameEngine activeEngine;

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(activeEngine != null){
                activeEngine.stop();
                activeEngine = null;
            }
        }));

        System.out.println("Welcome to Labyrinth Server UI");
        System.out.println("Please enter a command, or type 'help' for a list of all available commands.");
        Scanner scan = new Scanner(System.in);

        List<Command> cmds = new ArrayList<>();
        Map<String, Command> cmdMap = new HashMap<>();

        //Create all commands
        Command startCMD = new Command("start", "interface", "port", "persist" ,"trace") {
            @Override
            public void run() {
                if (activeEngine == null) {
                    PlayerRepository playerRepo;
                    SessionRepository sessionRepo;
                    String persistText = optMapping.get("persist");
                    if (persistText == null || Boolean.parseBoolean(persistText)) {
                        //TODO update this so it uses persistent storage
                        playerRepo = new InMemoryPlayerRepository();
                        sessionRepo = new InMemorySessionRepository();
                    } else {
                        playerRepo = new InMemoryPlayerRepository();
                        sessionRepo = new InMemorySessionRepository();
                    }
                    GameEngine engine = new GameEngine(playerRepo, sessionRepo);
                    String serverInterface = optMapping.get("interface");
                    if (serverInterface != null) {
                        engine.setServerInterface(serverInterface);
                    }
                    try {
                        String portText = optMapping.get("port");
                        if (portText != null) {
                            int port = Integer.parseInt(optMapping.get("port"));
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
                        if(Boolean.parseBoolean(optMapping.get("trace"))){
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("Engine is already started.");
                }
            }
        };

        Command stopCMD = new Command("stop") {
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

        Command helpCommand = new Command("help", "cmd") {
            @Override
            public void run() {
                String cmd = optMapping.get("cmd");
                if (cmd != null) {
                    Command specificCommand = cmdMap.get(cmd);
                    if (specificCommand != null) {
                        //Print specific command help here.
                    } else {
                        System.err.println("The specified command '" + cmd + "' could not be found. Run 'help' with no arguments for a full list of commands.");
                    }
                } else {
                    for(Command c : cmds){
                        System.out.println(c.phrase);
                    }
                }
            }
        };

        cmds.add(startCMD);
        cmds.add(stopCMD);
        cmds.add(helpCommand);

        for (Command cmd : cmds) {
            cmdMap.put(cmd.phrase, cmd);
        }
        char escCode = 0x1B;
        while (true) {
            System.out.print(">");
            System.out.print(String.format("%c[%dC",escCode,1));
            String command = scan.nextLine();
            String[] parts = command.trim().split("\\s+");
            if (parts.length >= 1) {
                if (parts[0].equals("exit")) {
                    if(activeEngine != null){
                        terminateActiveEngine();
                    }
                    break;
                }
                Command c = cmdMap.get(parts[0]);
                if (c != null) {
                    String[] options = Arrays.copyOfRange(parts, 1, parts.length);
                    c.execute(options);
                } else {
                    System.err.println("Unrecognized command: " + parts[0]);
                }
            } else {
                System.out.println();
            }
        }
    }

    private static void terminateActiveEngine(){
        if(activeEngine != null){
            activeEngine.stop();
            activeEngine = null;
        }
    }
}

abstract class Command implements Runnable {

    public final String phrase;
    public Map<String, String> optMapping;

    public Command(String phrase, String... opts) {
        this.phrase = phrase;
        this.optMapping = new HashMap<>();
        for (String opt : opts) {
            optMapping.put(opt, null);
        }
    }

    public void execute(String... optVals) {
        for (String optVal : optVals) {
            String[] temp = optVal.split("=", 2);
            String key = "-" + temp[0];
            String val = temp[1];
            if (optMapping.keySet().contains(key)) {
                optMapping.put(key, val);
            }
        }
        this.run();
    }
}
