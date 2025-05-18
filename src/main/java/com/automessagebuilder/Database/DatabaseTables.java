package com.automessagebuilder.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import com.automessagebuilder.Plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;



public class DatabaseTables {
    private final DatabaseManager dbManager;
    private final Plugin plugin;

    public DatabaseTables(DatabaseManager dbManager, Plugin pl) {
        this.dbManager = dbManager;
        this.plugin = pl;
    }

    public void createTable() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS automessage (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Title VARCHAR(128) NOT NULL," +
                "Message VARCHAR(1024) NOT NULL," +
                "isEnabled BOOLEAN DEFAULT 1," +
                "Interval INTEGER DEFAULT 60," +
                "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
            
            stmt.execute(sql);
            
            // Crear trigger para updatedAt
            String triggerSql = "CREATE TRIGGER IF NOT EXISTS update_automessage_timestamp " +
                              "AFTER UPDATE ON automessage " +
                              "FOR EACH ROW BEGIN " +
                              "UPDATE automessage SET updatedAt = CURRENT_TIMESTAMP WHERE ID = OLD.ID; " +
                              "END;";
            stmt.execute(triggerSql);
            
            
            
        } catch (SQLException e) {
            dbManager.getPlugin().getLogger().severe("Error creating tables: " + e.getMessage());
            throw new RuntimeException("Tables could not be created", e);
        }
    }
    public boolean addMessage(String title, String text, int interval, boolean enabled){
        String sql = "insert into automessage (Title,Message,IsEnabled,Interval) values (?, ?, ?, ?)";
        
        

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setString(2, text);
            pstmt.setInt(3, interval);
            pstmt.setBoolean(4, enabled);
           

            int affectedRows = pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            
            if (affectedRows == 0) {
                return false;
            }

            if (generatedKeys.next()) {
                JsonObject msg = new JsonObject();
                msg.addProperty("id", generatedKeys.getInt(1)); // Usar índice en lugar de nombre de columna
                msg.addProperty("title", title);
                msg.addProperty("text", text);
                msg.addProperty("interval", interval);
                msg.addProperty("enabled", enabled);

                // Añadir a la lista de mensajes de forma segura
                synchronized (plugin.messages) {
                    plugin.messages.add(msg);
                }

                plugin.taskchat.reloadTasks();
                
                return true;
            }


            

            
                               
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public JsonArray getMessages(){
        JsonArray messages = new JsonArray();

        try (Statement stmt = dbManager.getConnection().createStatement()){
            String sql = "SELECT * FROM automessage ORDER BY ID ASC";

            ResultSet res = stmt.executeQuery(sql);

            while(res.next()){
                JsonObject msg = new JsonObject();
                msg.addProperty("id", res.getInt("ID"));
                msg.addProperty("title", res.getString("Title"));
                msg.addProperty("text", res.getString("Message"));
                msg.addProperty("interval", res.getInt("Interval"));
                msg.addProperty("enabled", res.getBoolean("isEnabled"));

                messages.add(msg);
            }

            stmt.close();
            
        } catch (SQLException e){

        }

        return messages;
    }

    public void deleteMessage(CommandSender sender, int id){
        try (Statement stmt = dbManager.getConnection().createStatement()){
            String sql = "DELETE FROM automessage WHERE ID="+id+";";

            int affectedRows = stmt.executeUpdate(sql);


        
            if (affectedRows > 0) {
                // Eliminar de la lista en memoria
                synchronized (plugin.messages) {
                    Iterator<JsonElement> iterator = plugin.messages.iterator();
                    while (iterator.hasNext()) {
                        JsonObject object = iterator.next().getAsJsonObject();
                        if (object.get("id").getAsInt() == id) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                
                
                plugin.taskchat.reloadTasks();
                sender.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                            "&b[AutoMessageBuilder]&r "+plugin.getConfig().getString("messages.success.delete", "§2 Message deleted succesfully!!")
                        )
                );
        }
        
        } catch (SQLException e){

        }
    }

    
    
}