package io.com.automessagebuilder;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.com.automessagebuilder.commands.AutoMessageCommand;
import io.com.automessagebuilder.loop.MessageTask;
public class AutoMessageBuilder extends JavaPlugin implements Listener {
    
    public MessageTask tasks;

    @Override
    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);
       

        saveDefaultConfig();
        

        PluginCommand command = this.getCommand("automessage");
        if (command != null) {
            AutoMessageCommand cmd = new AutoMessageCommand(this);
            cmd.setConfig(getConfig());
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        }

        getLogger().info("Plugin AutoMessageBuilder activated!");
    }

    @Override
    public void onDisable(){
        tasks.stopTasks();
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        
        tasks = new MessageTask(this, getConfig());
        

        tasks.startTasks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerJoinEvent event){
        
        
        tasks.handlePlayerQuit(event.getPlayer());
    }
}