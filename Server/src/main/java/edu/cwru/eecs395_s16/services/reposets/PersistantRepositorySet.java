package edu.cwru.eecs395_s16.services.reposets;

import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.services.cache.RedisCacheService;
import edu.cwru.eecs395_s16.services.herorepository.PostgresHeroRepository;
import edu.cwru.eecs395_s16.services.maprepository.PostgresMapRepository;
import edu.cwru.eecs395_s16.services.playerrepository.PostgresPlayerRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.RedisSessionRepository;
import edu.cwru.eecs395_s16.services.weaponrepository.InMemoryWeaponRepository;
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
    public void initialize(Map<String, List<String>> baseData) {
        //Initialize all data here to defaults.
        List<String> nonexistantTables = baseData.keySet().stream().filter(key -> !schemaInitialized(key)).collect(Collectors.toList());
        if(nonexistantTables.size() > 0)
        {
            if(!runSQL("create_schema.sql")){
                return;
            }
            for(String tableName : nonexistantTables){
                initializeSchemaData(tableName, baseData.get(tableName));
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
    public void resetToDefaultData() {

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
        PreparedStatement stmt;
    }

    private List<String> sqlCSVSplit(String line){
        char[] characters = line.toCharArray();
        List<String> parts = new ArrayList<>();
        StringBuilder pb = new StringBuilder();
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
            if (st != null) st.close();
        }
        return true;
    }
}
