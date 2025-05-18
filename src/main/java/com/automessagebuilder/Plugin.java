package com.automessagebuilder;

import com.google.gson.JsonArray;


import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;


import com.automessagebuilder.Commands.AutoMessageCommand;
import com.automessagebuilder.Database.DatabaseManager;
import com.automessagebuilder.Database.DatabaseTables;
import com.automessagebuilder.Tasks.TaskChat;


public class Plugin extends JavaPlugin implements Listener {

    public DatabaseManager db;
    public DatabaseTables tables;
    public JsonArray messages;
    public TaskChat taskchat;
    public String version;

    @Override
    public void onEnable(){
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getPluginManager().registerEvents(this, this); // 
        } else {
            getLogger().severe("Could not find PlaceholderAPI! This plugin is required."); // 
            Bukkit.getPluginManager().disablePlugin(this);
        }
        
        saveDefaultConfig();


        version = getDescription().getVersion();
        

        this.getConnection();
        this.loadCommands();
        
        
        
    }

    

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        taskchat = new TaskChat(this);
        

        taskchat.createTask(event.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
       
        
        taskchat.handlePlayerQuit(event.getPlayer());
        

        
    }


    @Override
    public void onDisable() {
        if (db != null) {
            db.close();
        }

        this.getLogger().info("GoodBye!!");
    }


    private void getConnection(){
        try {
          
            this.db = new DatabaseManager(this);
            this.tables = new DatabaseTables(db, this);
            
            tables.createTable();
            getMessages();
            getLogger().info("AutoMessageBuilder v1.0.2 Loaded.");
            
        } catch (Exception e) {
            getLogger().severe("Error: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
        
    }
    private void getMessages(){
        messages =  tables.getMessages();
    }
    private void loadCommands(){
        PluginCommand am = this.getCommand("automessage");

        AutoMessageCommand amcmd = new AutoMessageCommand(this);
        am.setExecutor(amcmd); 
        am.setTabCompleter(amcmd);


        getLogger().info("Commands Loaded.");
    }
}
