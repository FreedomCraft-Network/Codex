package cx.ajneb97.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import cx.ajneb97.Codex;
import cx.ajneb97.utilidades.UtilidadesOtros;

public class MySQL {
	
	public static boolean isEnabled(FileConfiguration config){
		if(config.getString("mysql_database.enabled").equals("true")){
			return true;
		}else{
			return false;
		}
	}
	
	public static void createTable(Codex plugin) {
        try(Connection connection = plugin.getConnection()){
        	PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS codex_data (`uuid` VARCHAR(36) PRIMARY KEY, `player_name` VARCHAR(50), `discoveries` TEXT)");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static JugadorCodex getJugador(final UUID uuid, final Codex plugin){
		JugadorCodex j = null;
		try(Connection connection = plugin.getConnection()){
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM codex_data WHERE uuid=?");
			statement.setString(1, uuid.toString());
			ResultSet resultado = statement.executeQuery();
			if(resultado.next()){	
				String name = resultado.getString("player_name");
				String discoveries = resultado.getString("discoveries");
				List<String> discoveriesList = UtilidadesOtros.textToDiscoveries(discoveries);
				j = new JugadorCodex(uuid, name);
				j.setDiscoveries(discoveriesList);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return j;
	}
	
	public static void actualizarDiscoveriesJugador(final Codex plugin, final JugadorCodex jugadorCodex){
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
				UUID uuid = jugadorCodex.getUuid();
            	String name = jugadorCodex.getName();
            	String discoveries = UtilidadesOtros.discoveriesToText(jugadorCodex.getDiscoveries());
            	int resultado = 0;
            	try (Connection connection = plugin.getConnection()){
            		PreparedStatement statement = connection.prepareStatement("UPDATE codex_data SET discoveries=? WHERE (uuid=?)");
            		statement.setString(1, discoveries);
            		statement.setString(2, uuid.toString());
    				resultado = statement.executeUpdate();
        		} catch (SQLException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
            	if(resultado == 0) {
            		try(Connection connection = plugin.getConnection()){
            			PreparedStatement insert = connection
            					.prepareStatement("INSERT INTO codex_data (uuid,player_name,discoveries) VALUE (?,?,?)");
            			insert.setString(1, uuid.toString());
            			insert.setString(2, name);
            			insert.setString(3, discoveries);
            			insert.executeUpdate();
            		} catch (SQLException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            	}
            	
            }
		});
	}
}
