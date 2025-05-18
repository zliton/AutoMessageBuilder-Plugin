package com.automessagebuilder.Tasks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;


import com.automessagebuilder.Plugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.papermc.paper.util.Tick;


public class TaskChat {
    public Plugin plugin;


    public List<BukkitTask> tasks = new ArrayList<>();
    public List<UUID> activePlayers = new ArrayList<>();

    public TaskChat(Plugin pl){
        this.plugin = pl;
    }

    public void createTask(Player player){
        activePlayers.add(player.getUniqueId());
        if(plugin.messages.isEmpty()) return; 
        if (plugin.getConfig().getBoolean("automessage.random")){
            Random r= new Random();
            int random = r.nextInt(plugin.messages.size());
        
            JsonObject message = plugin.messages.get(random).getAsJsonObject();

            if(message.get("enabled").getAsBoolean()){

                int interval = plugin.getConfig().getInt("automessage.interval") * 20;

                BukkitTask tsk = Bukkit.getScheduler().runTaskTimer(plugin, ()->{
                    Random r2= new Random();
                    int random2 = r2.nextInt(plugin.messages.size());
                    JsonObject message2 = plugin.messages.get(random2).getAsJsonObject();

                    if(!player.isOnline()){
                            cancelPlayerTasks(player);
                            return;
                    }

                    String text = message2.get("text").getAsString();
                    

                    player.sendMessage(
                            LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(player, text))
                    );
                }, 200, interval);

                
                tasks.add(tsk);
                
            }
            
        } else {
            for (JsonElement msgElement : plugin.messages){
                JsonObject message = msgElement.getAsJsonObject();

                if(message.get("enabled").getAsBoolean()){

                    int interval = message.get("interval").getAsInt();

                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        if(!player.isOnline()){
                            cancelPlayerTasks(player);
                            return;
                        }

                        String text = message.get("text").getAsString();
                        
                    

                        player.sendMessage(
                                LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(player, text))
                        );

                    }, Tick.tick().fromDuration(Duration.ofSeconds(15)), Tick.tick().fromDuration(Duration.ofSeconds(interval)));

                    tasks.add(task);
                } else {
                    return;
                }

                
            }
        }
        
    }

    public void deleteTasks(){
       tasks.forEach(task -> {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
       });

       tasks.clear();
       activePlayers.clear();
    }

    public void cancelPlayerTasks(Player player) {
        
        activePlayers.remove(player.getUniqueId());
        
        reloadTasks();
    }

    public void reloadTasks() {
        deleteTasks();
        
        Bukkit.getOnlinePlayers().forEach(this::createTask);
    }

     public void handlePlayerQuit(Player player) {
        activePlayers.remove(player.getUniqueId());
        
    }
}
