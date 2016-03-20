package edu.cwru.eecs395_s16.services.reposets;

import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.services.cache.RedisCacheService;
import edu.cwru.eecs395_s16.services.herorepository.PostgresHeroRepository;
import edu.cwru.eecs395_s16.services.maprepository.PostgresMapRepository;
import edu.cwru.eecs395_s16.services.playerrepository.PostgresPlayerRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.RedisSessionRepository;
import edu.cwru.eecs395_s16.services.weaponrepository.InMemoryWeaponRepository;
import edu.cwru.eecs395_s16.utils.CoreDataParser;
import redis.clients.jedis.JedisPool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by james on 3/20/16.
 */
public class PersistantRepositorySet implements RepositorySet {

    final Connection postgresConnection;
    final JedisPool jedisPool;

    private static final String TABLE_EXISTS_QUERY = "select exists (select 1 from information_schema.tables where table_schema = 'public' and table_name = ?);";

    public PersistantRepositorySet(Connection postgresConnection, JedisPool jedisPool) {
        this.postgresConnection = postgresConnection;
        this.jedisPool = jedisPool;
    }

    @Override
    public void initialize(List<CoreDataParser.CoreDataEntry> baseData) {
        //Initialize all data here to defaults.
        List<String> nonexistantTables = baseData.stream().filter(key -> !schemaInitialized(key.name)).map(entry -> entry.name).collect(Collectors.toList());
        if(nonexistantTables.size() > 0)
        {
            System.out.println("Initializing the persistent repository set.");
            if(!runSQL("create_schema.sql")){
                return;
            }
            baseData.sort((o1,o2)-> Integer.compare(o1.order,o2.order));
            for(CoreDataParser.CoreDataEntry entry : baseData){
                initializeSchemaData(entry.name,entry.entries);
            }
        }
    }

    @Override
    public ServiceContainerBuilder addServicesToContainer(ServiceContainerBuilder scb) {
        PostgresPlayerRepository playerRepository = new PostgresPlayerRepository(postgresConnection);
        scb.setPlayerRepository(playerRepository);
        scb.setHeroRepository(new PostgresHeroRepository(postgresConnection));
        scb.setMapRepository(new PostgresMapRepository(postgresConnection));
        scb.setCacheService(new RedisCacheService(jedisPool));
        scb.setWeaponRepository(new InMemoryWeaponRepository());
        scb.setSessionRepository(new RedisSessionRepository(jedisPool, playerRepository));
        return scb;
    }

    @Override
    public void resetToDefaultData(List<CoreDataParser.CoreDataEntry> baseData) {
        dropAllTables();
        initialize(baseData);
    }

    private boolean dropAllTables(){
        try {
            PreparedStatement stmt = postgresConnection.prepareStatement("select 'drop table if exists \"' || tablename || '\" cascade;' from pg_tables where schemaname = 'public';");
            ResultSet rslts = stmt.executeQuery();
            List<String> dropQueries = new ArrayList<>();
            while(rslts.next()){
                dropQueries.add(rslts.getString(1));
            }
            for(String query : dropQueries){
                stmt = postgresConnection.prepareStatement(query);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean initializeSchemaData(String schemaName, List<String> data) {
        StringBuilder isb = new StringBuilder();
        isb.append("insert into ").append(schemaName).append(" VALUES ");
        int lineCount = 0;
        for(String dataLine : data){
            lineCount++;
            isb.append("(").append(dataLine).append(")");
            isb.append((lineCount == data.size())?";":",");
        }
        try {
            String stmtAsString = isb.toString();
            PreparedStatement stmt = postgresConnection.prepareStatement(stmtAsString);
            int results = stmt.executeUpdate();
            return results == lineCount;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean schemaInitialized(String schemaName) {
        try {
            PreparedStatement stmt = postgresConnection.prepareStatement(TABLE_EXISTS_QUERY);
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

    public boolean runSQL(String filename) {
        Scanner s;
        try {
            s = new Scanner(new BufferedReader(new FileReader(filename)));
        } catch (IOException e) {
            return false;
        }
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        try {
            st = postgresConnection.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                if (line.trim().length() > 0) {
                    st.execute(line);
                }
            }
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                if (st != null) st.close();
            } catch (SQLException e){
            }
        }
        return true;
    }
}
