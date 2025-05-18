package com.automessagebuilder.Database;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import com.automessagebuilder.Plugin;

public class DatabaseManager {
    private final Plugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        initializeDataSource();
    }
    public Plugin getPlugin(){
        return plugin;
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initializeDataSource();
        }
        return dataSource.getConnection();
    }

    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            if (plugin.getConfig().getBoolean("database.mysql.enabled")) {
                
                config.setJdbcUrl("jdbc:mysql://" + 
                    plugin.getConfig().getString("database.mysql.connection.host") + ":" + 
                    plugin.getConfig().getInt("database.mysql.connection.port") + "/" + 
                    plugin.getConfig().getString("database.mysql.connection.database"));
                config.setUsername(plugin.getConfig().getString("database.mysql.connection.username"));
                config.setPassword(plugin.getConfig().getString("database.mysql.connection.password"));
            } else {
               
                String dbPath = plugin.getDataFolder().getAbsolutePath() + "/" + 
                plugin.getConfig().getString("database.sqlite.connection.file", "automessages.db");
                config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            }

            config.setMaximumPoolSize(5);
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("AutoMessageBuilderPool");

            dataSource = new HikariDataSource(config);
            
           
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error initializing database: " + e.getMessage());
            throw new RuntimeException("The database could not be initialized.", e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
           
        }
    }
}