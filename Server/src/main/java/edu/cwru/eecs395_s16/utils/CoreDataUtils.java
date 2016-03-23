package edu.cwru.eecs395_s16.utils;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by james on 3/20/16.
 */
public class CoreDataUtils {

    private static final String DEFAULT_CORE_DATA_FILE = "new_base_data.data";
    private static final String TABLE_EXISTS_QUERY = "select exists (select 1 from information_schema.tables where table_schema = 'public' and table_name = ?);";
    private static Map<String, CoreDataEntry> coreData;

    public static Map<String, CoreDataEntry> defaultCoreData() {
        if (coreData == null) {
            coreData = parse(DEFAULT_CORE_DATA_FILE);
        }
        return coreData;
    }

    private static Map<String, String> createSchemaMap;
    private static final Pattern startCreateSchemaPattern = Pattern.compile("create\\s+table\\s+(?:if not exists)?\\s+(.*?)\\s+\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern startDataSetPattern = Pattern.compile("start\\s+(.*)?\\r?\\n?", Pattern.CASE_INSENSITIVE);
    private static final Pattern endDataSetPattern = Pattern.compile("end\\s+(.*)\\r?\\n?", Pattern.CASE_INSENSITIVE);

    public static Map<String, CoreDataEntry> parse(String fileName) {
        Map<String, CoreDataEntry> entries = new HashMap<>();
        Scanner scan = null;
        try {
            scan = new Scanner(new BufferedReader(new FileReader(fileName)));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                Matcher startMatcher = startDataSetPattern.matcher(line);
                if (startMatcher.matches()) {
                    String dataSetName = startMatcher.group(1);
                    CoreDataEntry dataStore;
                    if (!entries.containsKey(dataSetName)) {
                        dataStore = new CoreDataEntry(entries.size() + 1, dataSetName);
                        entries.put(dataSetName, dataStore);
                    } else {
                        dataStore = entries.get(dataSetName);
                    }
                    boolean seenEndOfDataset = false;
                    while (scan.hasNextLine()) {
                        String innerLine = scan.nextLine();
                        if (endDataSetPattern.matcher(innerLine).matches()) {
                            seenEndOfDataset = true;
                            break;
                        } else {
                            dataStore.addEntry(innerLine);
                        }
                    }
                    if (!seenEndOfDataset) {
                        throw new IllegalArgumentException("Data set " + dataSetName + " does not have a matching end.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(new File(".").getAbsolutePath());
            e.printStackTrace();
            throw new IllegalArgumentException("Something is wrong with the core data file or format.");
        } finally {
            if (scan != null) {
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

        public void addEntry(String entry) {
            entries.add(entry);
        }
    }

    public static boolean setCreateSchemaMap(String schemaFileName) {
        if (createSchemaMap == null) {
            List<String> lines = getSqlLines(schemaFileName);
            createSchemaMap = new HashMap<>();
            for(String line : lines) {
                Matcher m = startCreateSchemaPattern.matcher(line);
                if (m.find()) {
                    String tableName = m.group(1);
                    if (!createSchemaMap.containsKey(tableName)) {
                        createSchemaMap.put(tableName, line);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static List<String> getSqlLines(String filename) {
        Scanner s;
        List<String> lines = new ArrayList<>();
        try {
            s = new Scanner(new BufferedReader(new FileReader(filename)));
        } catch (IOException e) {
            return lines;
        }
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        while (s.hasNext()) {
            String line = s.next();
            if (line.startsWith("/*") && line.endsWith("*/")) {
                int i = line.indexOf(' ');
                line = line.substring(i + 1, line.length() - " */".length());
            }

            if (line.trim().length() > 0) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static boolean createTableForData(Connection conn, String tableName) {
        if (createSchemaMap == null) {
            return false;
        }
        if (!createSchemaMap.containsKey(tableName)) {
            return false;
        }
        if(isSchemaInitialized(conn,tableName)){
            return true;
        }
        String createLine = createSchemaMap.get(tableName);
        try {
            Statement st = conn.createStatement();
            st.execute(createLine);
            System.out.println(createLine);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return isSchemaInitialized(conn,tableName);
    }

    public static List<String> sqlCSVSplit(String line) {
        char[] characters = line.toCharArray();
        List<String> parts = new ArrayList<>();
        StringBuilder pb = new StringBuilder();
        boolean inside = false;
        for (int i = 0; i < characters.length; i++) {
            char c = characters[i];
            if (c == '\'') {
                if (i == 0 || i - 1 >= 0 && characters[i - 1] != '\\') {
                    inside = !inside;
                }
            } else if (!inside && Character.isWhitespace(c)) {
            } else if (c != ',' || inside) {
                pb.append(c);
            } else {
                parts.add(pb.toString());
                pb.setLength(0);
            }
        }
        String finalStr = pb.toString();
        if (finalStr.length() > 0) {
            parts.add(finalStr);
        }
        return parts;
    }

    public static List<List<String>> splitEntries(CoreDataUtils.CoreDataEntry entry) {
        return entry.entries.stream().map(CoreDataUtils::sqlCSVSplit).collect(Collectors.toList());
    }

    public static void insertIntoDB(Connection conn, List<CoreDataEntry> data) {
        for (CoreDataUtils.CoreDataEntry entry : data) {
            StringBuilder isb = new StringBuilder();
            isb.append("insert into ").append(entry.name).append(" VALUES ");
            int lineCount = 0;
            for (String dataLine : entry.entries) {
                lineCount++;
                isb.append("(").append(dataLine).append(")");
                isb.append((lineCount == entry.entries.size()) ? ";" : ",");
            }
            try {
                String stmtAsString = isb.toString();
                System.out.println(stmtAsString);
                PreparedStatement stmt = conn.prepareStatement(stmtAsString);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isSchemaInitialized(Connection conn, String schemaName) {
        try {
            PreparedStatement stmt = conn.prepareStatement(TABLE_EXISTS_QUERY);
            stmt.setString(1, schemaName);
            ResultSet rslts = stmt.executeQuery();
            boolean exists = false;
            while (rslts.next()) {
                exists = rslts.getBoolean(1);
            }
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
