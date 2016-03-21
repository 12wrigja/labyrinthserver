package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by james on 3/20/16.
 */
public abstract class DBRepository implements Repository {

    protected abstract List<String> getTables();

    protected Connection conn;

    protected DBRepository(Connection conn, Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        this.conn = conn;
    }

    @Override
    public void initialize(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        List<CoreDataUtils.CoreDataEntry> data = new ArrayList<>(baseData.values().stream().filter(entry-> getTables().contains(entry.name)).collect(Collectors.toList()));
        data.sort((o1, o2) -> Integer.compare(o1.order, o2.order));
        CoreDataUtils.insertIntoDB(conn,data);
    }

    @Override
    public void resetToDefaultData(Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        try {
            List<String> tables = getTables();
            StringBuilder sb = new StringBuilder();
            sb.append("drop table if exists ");
            for (int i = 0; i < tables.size(); i++) {
                sb.append(tables.get(i));
                if(i != tables.size()-1){
                    sb.append(",");
                }
            }
            sb.append(" cascade;");
            PreparedStatement stmt = conn.prepareStatement(sb.toString());
            ResultSet rslts = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
