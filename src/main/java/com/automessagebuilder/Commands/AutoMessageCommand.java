package com.automessagebuilder.Commands;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.automessagebuilder.Plugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AutoMessageCommand implements CommandExecutor, TabCompleter {
    
    //Plugin principal
    private final Plugin plugin;



    /**      
     *  Sub Comandos del comando /automessage 
     */
    private final List<String> subCommands = Arrays.asList("add", "list", "delete");



    public AutoMessageCommand(Plugin pl){
        this.plugin = pl;
    }

    @Override


    //Tab Completer

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        switch (args[0].toLowerCase()) {

            case "delete":
                if (args.length == 2) {
                    List<String> messageIds = new ArrayList<>();
    
                    for (JsonElement element : plugin.messages) {
                        JsonObject messageObj = element.getAsJsonObject();
                        messageIds.add(messageObj.get("id").getAsString());
                    }
                    
                    // Filtrar según lo que el jugador ya ha escrito
                    String partialId = args[1].toLowerCase();
                    return messageIds.stream()
                        .filter(id -> id.toLowerCase().startsWith(partialId))
                        .collect(Collectors.toList());
                }
                break;
            case "add":
                switch(args.length) {
                    case 2:
                        completions.add("<identifier>");
                        break;
                    case 3: 
                        completions.add("<interval>");
                        break;
                    case 4: 
                        completions.add("<message>");
                        break;
                }
                break;
        }

        return completions;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        
        if(args.length == 0) return helpMessage(sender);
        
        if(args[0].equalsIgnoreCase( "add")){

            if(!sender.hasPermission("automessagebuilder.automessage.add")) return msgpermission(sender);

            if(args[1].length() == 0) return helpMessage(sender);
            if(args[2].length() == 0) return helpMessage(sender);
            if(args[3].length() == 0) return helpMessage(sender);

            int interval = Integer.parseInt(args[2]);
            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            if(plugin.tables.addMessage(args[1], message, interval, true)) return successadd(sender);
        }

        if(args[0].equalsIgnoreCase( "list")){

            if(!sender.hasPermission("automessagebuilder.automessage.list")) return msgpermission(sender);
            
            return getListMessage(sender);

        }

        

        if(args[0].equalsIgnoreCase( "delete")){

            if(!sender.hasPermission("automessagebuilder.automessage.delete")) return msgpermission(sender);
            
            if(args[1].length() == 0) return helpMessage(sender);

            int id = Integer.parseInt(args[1]);

            
            plugin.tables.deleteMessage(sender, id);
        }

       

        return true;
    }

    //Mensaje de ayuda
    private boolean helpMessage(CommandSender sender){
        sender.sendMessage(

            MiniMessage.miniMessage().deserialize(
                "<color:#ff9900>Running</color> <color:#ff080><b><click:open_url:'https://github.com/zliton/AutoMessageBuilder-Plugin'>AutoMessageBuilder v"+plugin.version+"</click></b></color>\n" + //
                " <i><color:#b5bcff> > </color>/automessage <color:#ffbf00>add  <identifier> <interval> <text></color></i>\n" + //
                " <i><color:#b5bcff> > </color>/automessage <color:#ffbf00>list </color></i>\n" + //
                " <i><color:#b5bcff> > </color>/automessage <color:#ffbf00>delete <identifier></color></i>\n" + //
                " <i><color:#b5bcff> > </color>/automessage </i>"
                )
        );  

        return true;
    }
    
    private boolean getListMessage(CommandSender sender){
        if(plugin.messages.isJsonNull()){
             sender.sendMessage(

                MiniMessage.miniMessage().deserialize(
                    "<color:#ff9900>Running</color> <color:#ff080><b><click:open_url:'https://github.com/zliton/AutoMessageBuilder-Plugin'>AutoMessageBuilder v"+plugin.version+"</click></b></color>\n"
                    + " <i><color:#b5bcff> > </color></i>"
                ).append(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getConfig().getString("messages.non-message", "§4 There are no automatic messages to run."))
                )

            );
            return true;
        }

        

        sender.sendMessage(

                MiniMessage.miniMessage().deserialize(
                    "<color:#ff9900>Running</color> <color:#ff080><b><click:open_url:'https://github.com/zliton/AutoMessageBuilder-Plugin'>AutoMessageBuilder v"+plugin.version+"</click></b></color>\n"
                )

            );

        
        for (JsonElement msgElement : plugin.messages){

            JsonObject message = msgElement.getAsJsonObject();

            


            sender.sendMessage(
                MiniMessage.miniMessage().deserialize(

                    " <i><color:#b5bcff> > </color> ID: "+message.get("id").getAsInt()+
                    " | "+message.get("title").getAsString()+
                    " | <hover:show_text:'"+message.get("text").getAsString()+"'>Message</hover>"+  
                    " | Interval: "+message.get("interval").getAsInt()+" seconds </i>\n"

                )
            );

               

        }


        return true;
    }

    private boolean msgpermission(CommandSender sender){

        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
            "&b[AutoMessageBuilder]&r "+plugin.getConfig().getString("messages.non-permission", "§4You don't have permission to use this command.")
        ));

        return true;
    }

    private boolean successadd(CommandSender sender){

        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
            "&b[AutoMessageBuilder]&r "+plugin.getConfig().getString("messages.success.create", "§2 Message created succesfully!!")
        ));

        return true;
    }
}   
