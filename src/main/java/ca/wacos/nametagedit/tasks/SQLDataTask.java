package ca.wacos.nametagedit.tasks;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagEdit;

/**
 * This class is responsible for grabbing all data from the database and caching
 * it
 * 
 * @author sgtcaze
 */
public class SQLDataTask extends BukkitRunnable {

    private NametagEdit plugin = NametagEdit.getInstance();

    @Override
    public void run() {
        Connection connection = null;

        final HashMap<String, String> tPerms = new HashMap<>();
        final HashMap<String, List<String>> groupDataTemp = new HashMap<>();
        final HashMap<String, List<String>> playerDataTemp = new HashMap<>();

        String groupQuery = "SELECT * FROM `groups`;";
        String playerQuery = "SELECT * FROM `players`;";

        try {
            connection = plugin.getConnectionPool().getConnection();

            ResultSet groupResults = connection.prepareStatement(groupQuery).executeQuery();

            while (groupResults.next()) {
                groupDataTemp.put(groupResults.getString("name"), Arrays.asList(groupResults.getString("permission"), groupResults.getString("prefix"), groupResults.getString("suffix")));
                tPerms.put(groupResults.getString("permission"), groupResults.getString("name"));
            }

            groupResults.close();

            ResultSet playerResults = connection.prepareStatement(playerQuery).executeQuery();

            while (playerResults.next()) {
                playerDataTemp.put(playerResults.getString("uuid"), Arrays.asList(playerResults.getString("name"), colorize(playerResults.getString("prefix")),
                                colorize(playerResults.getString("suffix"))));
            }

            playerResults.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getLogger().info("[MySQL] Found " + groupDataTemp.size() + " groups");
                    plugin.getLogger().info("[MySQL] Found " + playerDataTemp.size() + " players");

                    plugin.getNTEHandler().setPermissionsMap(tPerms);
                    plugin.getNTEHandler().setGroupDataMap(groupDataTemp);
                    plugin.getNTEHandler().setPlayerDataMap(playerDataTemp);

                    plugin.getNTEHandler().getAllGroups().clear();

                    for (String s : groupDataTemp.keySet()) {
                        plugin.getNTEHandler().getAllGroups().add(s);
                    }

                    plugin.getNTEHandler().applyTags();
                }
            }.runTask(plugin);
        }
    }
    
    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}