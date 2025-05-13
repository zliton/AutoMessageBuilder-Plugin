package io.com.automessagebuilder.loop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import io.com.automessagebuilder.AutoMessageBuilder;

public class MessageTask {
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final FileConfiguration config;
    private final AutoMessageBuilder plugin;
    private final List<UUID> activePlayers = new ArrayList<>();

    public MessageTask(AutoMessageBuilder plugin, FileConfiguration configuration) {
        this.plugin = plugin;
        this.config = configuration;
    }

    public void startTasks(Player player) {
        // Evitar duplicados
        if (activePlayers.contains(player.getUniqueId())) {
            if (config.getBoolean("debug", false)) {
                plugin.getLogger().info("El jugador "+ player.getName() +" ya tiene tareas activas");
            }
            return;
        }

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) {
            if (config.getBoolean("debug", false)) {
                plugin.getLogger().warning("No hay mensajes configurados en config.yml!");
            }
            return;
        }

        activePlayers.add(player.getUniqueId());
        
        for (String id : messagesSection.getKeys(false)) {
            long interval = config.getInt("messages." + id + ".interval", 60) * 20L;
            String message = config.getString("messages." + id + ".message", "");
            
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!player.isOnline()) {
                    cancelPlayerTasks(player);
                    return;
                }
                
                if (config.getBoolean("debug", false)) {
                    plugin.getLogger().info("Enviando mensaje automático a "+player.getName());
                }
                
                player.sendMessage(""+message);
            }, 100L, interval);

            tasks.add(task);
        }
    }

    public void stopTasks() {
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
        stopTasks();
        
        Bukkit.getOnlinePlayers().forEach(this::startTasks);
    }

  
    public void handlePlayerQuit(Player player) {
        activePlayers.remove(player.getUniqueId());
        
    }

}
