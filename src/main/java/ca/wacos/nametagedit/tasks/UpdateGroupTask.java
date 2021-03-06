package ca.wacos.nametagedit.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagEdit;

public class UpdateGroupTask extends BukkitRunnable {
    
    private String table, group, field, oper;

    public UpdateGroupTask(String table, String group, String field, String oper) {
        this.table = table;
        this.group = group;
        this.field = field;
        this.oper = oper;
    }
    
    @Override
    public void run() {
        Connection connection = null;

        try {
            connection = NametagEdit.getInstance().getConnectionPool().getConnection();

            String query = "UPDATE `" + table + "` SET `" + field + "`=? WHERE `name`=?;";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, oper);
            p.setString(2, group);
            p.execute();
            p.close();
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
        }
    }
}