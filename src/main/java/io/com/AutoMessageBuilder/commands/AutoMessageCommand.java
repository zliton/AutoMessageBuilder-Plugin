package io.com.automessagebuilder.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import io.com.automessagebuilder.AutoMessageBuilder;
import io.com.automessagebuilder.loop.MessageTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class AutoMessageCommand implements CommandExecutor, TabCompleter {
    
    private final AutoMessageBuilder plugin;
    private FileConfiguration config;
    private final MessageTask tasks;

    public AutoMessageCommand(AutoMessageBuilder plugin) {
        this.plugin = plugin;
        this.tasks = new MessageTask(this.plugin, plugin.getConfig());
    }
    public void setConfig(FileConfiguration config){
        this.config = config;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser ejecutado por jugadores.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("automessagebuilder.use")) {
                
            return true;
        }
       
        if (args.length == 0) {
            this.commanderror(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {

            if (!player.hasPermission("automessagebuilder.add")) {
                
                return true;
            }

            if (args.length < 4) {
                this.commanderror(player);
                return true;
            }

            try {
                int segundos = Integer.parseInt(args[2]);
                this.saveMessage(args[1], String.join(" ", Arrays.copyOfRange(args, 3, args.length)), segundos);
                player.sendMessage("§aMensaje guardado correctamente!");
            } catch (NumberFormatException e) {
                player.sendMessage("§cEl intervalo debe ser un número válido.");
                return true;
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            

            if (!player.hasPermission("automessagebuilder.delete")) {
                
                return true;
            }

            if(!config.contains("messages." + args[1])) {
                player.sendMessage("§cNo existe ese id de mensaje automatico");
                return true;
            }
            

            Component borrarmensaje = Component.text("Confirmar").clickEvent(ClickEvent.callback( event  -> {
                
                Player clicker = (Player) player;
                
                
                
                borrarMessage(clicker, args[1]);
            })).hoverEvent(HoverEvent.showText(Component.text("Confirma para eliminar el mensaje")));

           
            player.sendMessage(Component.text("Estas a punto de borrar el mensaje automatico §4"+args[1]+"§r ").append(borrarmensaje));
            
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {

            if (!player.hasPermission("automessagebuilder.list")) {
                
                return true;
            }

            if(config.get("messages") == null || config.getConfigurationSection("messages").getKeys(false).isEmpty()){
                sender.sendMessage("§cNo hay mensajes Automaticos en la base de datos");
                return true;
            }

            player.sendMessage("Lista de mensajes automaticos:");

            for (String id : config.getConfigurationSection("messages").getKeys(false)) {
                String message = config.getString("messages."+id + ".message", "§cMensaje no definido");
                int interval = config.getInt("messages."+id + ".interval", 60);
                
                
                Component messageLine = Component.text()
                    .append(Component.text("§eID: §b" + id + " §7| "))
                    .append(Component.text("§eIntervalo: §a" + interval + "s §7| "))
                    .append(Component.text("§eMensaje: §f" + message))
                    .append(Component.space())
                    .build();
                    
                player.sendMessage(messageLine);
            }   

            
            return true;
        }

        this.commanderror(player);
        return true;
    }

    private void commanderror(Player sender) {
        sender.sendMessage("§6===== AutoMessageBuilder =====");
        sender.sendMessage("§a/automessage set <id> <intervalo> <mensaje> §7- Crea un mensaje automático");
        sender.sendMessage("§a/automessage list §7- Lista todos los mensajes");
        sender.sendMessage("§a/automessage delete <id> §7- Elimina un mensaje");
    }

    private void saveMessage(String id, String message, int interval) {
        this.config.set("messages." + id + ".message", message);
        this.config.set("messages." + id + ".interval", interval);
        this.plugin.saveConfig();

        this.tasks.reloadTasks();
    }  

    public void borrarMessage(Player player, String id) {
        
        
    
        
        config.set("messages." + id, null);
        plugin.saveConfig();
        
        
        try {
            if (tasks != null) {
                tasks.reloadTasks();
            }
            player.sendMessage("§aMensaje eliminado correctamente!");
        } catch (Exception e) {
            player.sendMessage("§cError al recargar los mensajes automáticos.");
            plugin.getLogger().severe("Error al recargar tareas: " + e.getMessage());
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "reload", "list", "delete"));
        } 
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("welcome", "rules", "reminder", "announcement"));
            } 
            else if (args[0].equalsIgnoreCase("delete")) {
                if (config.getConfigurationSection("messages") != null) {
                    completions.addAll(config.getConfigurationSection("messages").getKeys(false));
                }
            }
        } 
        else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("10", "30", "60", "120", "300"));
        }
        
       
        if (args.length > 0) {
            String currentArg = args[args.length - 1].toLowerCase();
            completions = completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}
