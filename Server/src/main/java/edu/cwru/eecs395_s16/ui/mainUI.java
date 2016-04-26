package edu.cwru.eecs395_s16.ui;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import edu.cwru.eecs395_s16.services.containers.ServiceContainer;
import edu.cwru.eecs395_s16.services.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * ---------------------------------------------------------------------------
 * START HERE
 * ---------------------------------------------------------------------------
 * <p>
 * Alright. Here goes - the semi-guided walkthrough of the Labyrinth server
 * code.
 * This is the main UI for the Labyrinth server. This is what is called when
 * Gradle assembles or runs the project. The basic terminal UI allows the
 * server administrator to start and stop the engine with various parameters,
 * seed the connected database, and query the server while it is running for
 * various information. Each of the commands has a description which can also
 * be queried at runtime using the help command.
 * <p>
 * For information on how the commands work, check the ConsoleCommand class in
 * this package.
 * <p>
 * Most of the time, the server is started using the command 'start
 * trace=true persist=true', which tells the server that it should print all
 * tracing information to the console, and that we should use persistent
 * storage (e.g. the database). For the next part of this walkthrough, jump
 * down to the start command written below.
 */

/**
 * Created by james on 2/1/16.
 */
public class mainUI {

    //Connection strings for the database
    public static final String JDBC_CONN_STRING = "jdbc:postgresql:vagrant";
    public static final String DB_USERNAME = "vagrant";
    public static final String DB_PASSWORD = "vagrant";

    //Default data file - feel free to open and look at it. This file is what
    // gets parsed and used when the server runs with in memory storage, or
    // converted to SQL statements if the database is being used.
    private static final String DEFAULT_DATA_FILE_NAME = "new_base_data.data";
    private static GameEngine activeEngine;
    private static Scanner scan;

    public static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_CONN_STRING, DB_USERNAME, DB_PASSWORD);
    }

    public static void main(String[] args) {

        System.out.println("Welcome to Labyrinth Server UI");
        System.out.println("Please enter a command, or type 'help' for a " +
                "list" + " of all available commands.");
        scan = new Scanner(System.in);

        List<ConsoleCommand> cmds = new ArrayList<>();
        Map<String, ConsoleCommand> cmdMap = new HashMap<>();

        //Create all commands
        ConsoleCommand startCMD = new ConsoleCommand("start", "Starts the " +
                "engine if it is stopped. Will report errors if there is " +
                "something blocking the engine port.", "interface", "port", "persist", "trace") {
            @Override
            public void run() {
                if (activeEngine == null) {
                    /**
                     * Here we are! This is what starts up a server if there
                     * isnt one already running. Basically, it creates a
                     * ServiceContainer and populates it with service
                     * instances, and then passes it to the constructor for
                     * the GameEngine. We then start the GameEngine. Jump to
                     * the ServiceContainer.java file for the next step.
                     */

                    //Setup a persistent container if we need one, otherwise
                    // create an in-memory service container.
                    String file = "new_base_data.data";
                    Map<String, CoreDataUtils.CoreDataEntry> data = CoreDataUtils.parse(file);
                    String persistText = getOption("persist");
                    ServiceContainer container;
                    boolean enableTrace = Boolean.parseBoolean(getOption("trace"));
                    if (persistText != null && Boolean.parseBoolean(persistText)) {
                        Connection dbConnection;
                        try {
                            dbConnection = getDBConnection();
                        } catch (SQLException e) {
                            System.err.println("Unable to create connection " + "to Postgres Database.");
                            if (enableTrace) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        JedisPool jedisPool = new JedisPool("localhost");
                        container = ServiceContainer.buildPersistantContainer(data, dbConnection, jedisPool, new
                                BasicMatchmakingService());
                    } else {
                        container = ServiceContainer.buildInMemoryContainer(data, new BasicMatchmakingService());
                    }

                    //Build a new GameEngine instance and adds a
                    // ClientConnectionService for Socket.IO.
                    GameEngine engine = new GameEngine(enableTrace, container);
                    SocketIOConnectionService socketIO = new SocketIOConnectionService();
                    String serverInterface = getOption("interface");
                    if (serverInterface != null) {
                        socketIO.setServerInterface(serverInterface);
                    }
                    try {
                        String portText = getOption("port");
                        if (portText != null) {
                            int port = Integer.parseInt(portText);
                            socketIO.setServerPort(port);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Specified port is invalid.");
                        return;
                    }
                    engine.addClientService(socketIO);
                    try {
                        engine.start();
                        activeEngine = engine;
                    } catch (BindException e) {
                        engine.stop();
                        System.err.println("Unable to start engine - something is running on a port used for one of " +
                                "the client services. Try using the linux commands netstat or lsof to determine the " +
                                "offending program and kill it.");
                        if (enableTrace) {
                            e.printStackTrace();
                        }
                    } catch (UnknownHostException e) {
                        System.err.println("Unable to start engine - a provided interface for one of the client " +
                                "services is not valid.");
                        if (enableTrace) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        System.err.println("An unknown error occurred.");
                        if (enableTrace) {
                            e.printStackTrace();
                        } else {
                            System.err.println(" Run this command again with " +
                                    "the trace option set to true to print a " +
                                    "stacktrace.");
                        }
                    }
                } else {
                    System.err.println("Engine is already started.");
                }
            }
        };

        ConsoleCommand stopCMD = new ConsoleCommand("stop", "Stops the " +
                "engine" + " if it is running.") {
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

        ConsoleCommand queuesCommand = new ConsoleCommand("queueStats", "Retrieves stats on the queueing system.") {
            @Override
            public void run() {
                if (activeEngine != null) {
                    System.out.println("Queue stats:");
                    System.out.println(activeEngine.services.matchService.getQueueInformation());
                    System.out.println("End Queue Stats.");
                } else {
                    System.err.println("Engine is not running.");
                }
            }
        };

        ConsoleCommand helpCommand = new ConsoleCommand("help", "Gives " +
                "information about a command. Run this with no arguments to " +
                "get a full list of commands.", "cmd") {
            @Override
            public void run() {
                String cmd = getOption("cmd");
                if (cmd != null) {
                    //Requesting specific information on something.
                    ConsoleCommand specificCommand = cmdMap.get(cmd);
                    if (specificCommand != null) {
                        //Print specific command help here.
                        System.out.println("Command: " + specificCommand.phrase);
                        System.out.println("Description: " + specificCommand.description);
                        System.out.println("Required Parameters: ");
                        if (specificCommand.requiredParameters.size() > 0) {
                            for (String param : specificCommand.requiredParameters) {
                                System.out.println(" - " + param);
                            }
                        } else {
                            System.out.println("none");
                        }
                        System.out.println("Optional Parameters: ");
                        if (specificCommand.optionalParameters.size() > 0) {
                            for (String param : specificCommand.optionalParameters) {
                                System.out.println(" - " + param);
                            }
                        } else {
                            System.out.println("none");
                        }
                    } else {
                        System.err.println("The specified command '" + cmd +
                                "' could not be found. Run 'help' with no " +
                                "arguments for a full list of commands.");
                    }
                } else {
                    ConsoleTable tbl = new ConsoleTable();
                    tbl.setRowHeaders("Command", "Description", "Required " + "Parameters", "Optional Parameters");
                    for (ConsoleCommand c : cmds) {
                        tbl.addRow(c.phrase, c.description, c.requiredParameters.toString().replaceAll("\\[|\\]", "")
                                , c.optionalParameters.toString().replaceAll("\\[|\\]", ""));
                    }
                    System.out.println(tbl);
                }
            }
        };

        ConsoleCommand listAllFunctionsCommand = new ConsoleCommand("functions", "Lists all the socket functions that" +
                " this game " +
                "" + "engine supports.") {
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
                    System.err.println("Engine not booted. You need to boot " +
                            "the machine in order to know what functions are " +
                            "available to you. This can be done with the " +
                            "start command.");
                }
            }
        };

        ConsoleCommand describeSpecificFunction = new ConsoleCommand("function", "Describes the function given by the" +
                " fn " + "parameter", "*fn") {
            @Override
            public void run() {
                String fn = getOption("fn");
                FunctionDescription fd = activeEngine.getFunctionDescription(fn);
                System.out.println("Function: " + fd.humanName);
                System.out.println("Socket Event: " + fd.name);
                System.out.println("Description: " + fd.description);
                System.out.println("Parameters: " + Arrays.toString(fd.parameters));
            }
        };

        //Technically a stand-in for the actual exit command.
        ConsoleCommand exitCommand = new ConsoleCommand("exit", "Exits the " + "engine. Terminates the engine if it " +
                "running.") {
            @Override
            public void run() {
                scan.close();
                if (activeEngine != null) {
                    terminateActiveEngine();
                }
            }
        };

        ConsoleCommand seedDBCommand = new ConsoleCommand("seed", "Seeds the " +
                "" + "database with initial data. WILL DROP ALL EXISTING DATA" +
                ".", "dataFile") {
            @Override
            public void run() {
                System.out.println(new File(".").getAbsolutePath());
                String dataFile;
                if (getOption("dataFile") != null) {
                    dataFile = getOption("dataFile");
                } else {
                    dataFile = DEFAULT_DATA_FILE_NAME;
                }
                Map<String, CoreDataUtils.CoreDataEntry> coreData = CoreDataUtils.parse(dataFile);
                Connection dbConnection;
                try {
                    dbConnection = getDBConnection();
                } catch (SQLException e) {
                    System.err.println("Unable to create connection to " + "Postgres Database.");
                    e.printStackTrace();
                    return;
                }
                JedisPool jedisPool = new JedisPool("localhost");
                ServiceContainer c = ServiceContainer.buildPersistantContainer(coreData, dbConnection, jedisPool, new
                        BasicMatchmakingService());
                c.cleanAndInit(coreData);
            }
        };

        cmds.add(startCMD);
        cmds.add(stopCMD);
        cmds.add(helpCommand);
        cmds.add(exitCommand);
        cmds.add(describeSpecificFunction);
        cmds.add(listAllFunctionsCommand);
        cmds.add(seedDBCommand);
        cmds.add(queuesCommand);

        for (ConsoleCommand cmd : cmds) {
            cmdMap.put(cmd.phrase, cmd);
        }
        char escCode = 0x1B;

        Runtime.getRuntime().addShutdownHook(new Thread(exitCommand));

        while (true) {
            System.out.print(">");
            System.out.print(String.format("%c[%dC", escCode, 1));
            String command;
            try {
                command = scan.nextLine();
            } catch (Exception e) {
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


