package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by james on 3/20/16.
 */
public abstract class DBRepository implements Repository {

    protected abstract List<String> getTables();

    protected Connection conn;

    protected DBRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public final void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        Set<String> tableNames = new HashSet<>(getTables());
        List<CoreDataUtils.CoreDataEntry> data = new ArrayList<>(baseData.values().stream().filter(entry -> getTables().contains(entry.name) && !CoreDataUtils.isSchemaInitialized(conn,entry.name)).collect(Collectors.toList()));
        data.sort((o1, o2) -> Integer.compare(o1.order, o2.order));
        data.forEach(entry -> {
            if(!CoreDataUtils.createTableForData(conn, entry.name)){
                System.err.println("Failed to make a table for : "+entry.name);
            } else {
                tableNames.remove(entry.name);
            }
        });
        tableNames.stream().map(name ->{
            boolean val = CoreDataUtils.createTableForData(conn,name);
            return new AbstractMap.SimpleEntry<>(name, val);
        }).filter(entry->!entry.getValue()).forEach(entry->System.err.println("Failed to make a table for: "+entry.getKey()));
        CoreDataUtils.insertIntoDB(conn, data);
    }

    @Override
    public final void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        try {
            List<String> tables = getTables();
            StringBuilder sb = new StringBuilder();
            sb.append("drop table if exists ");
            for (int i = 0; i < tables.size(); i++) {
                sb.append(tables.get(i));
                if (i != tables.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append(" cascade;");
            PreparedStatement stmt = conn.prepareStatement(sb.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize(baseData);
    }

}
