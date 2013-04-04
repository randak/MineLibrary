package com.untamedears.minelibrary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MineLibrary extends JavaPlugin {
	private final BookListener bl = new BookListener(this);
	public SQLite sqlite;
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public Map<Player, Location> inventories = new HashMap<Player, Location>();
	
	@Override
    public void onEnable(){
		dbConnect();
        dbSetup();
        getServer().getPluginManager().registerEvents(this.bl, this);
		getLogger().info("MineLibrary has been enabled.");
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("MineLibrary has been disabled.");
    }
    
    private void dbConnect() {
    	sqlite = new SQLite(this.logger, "MineLibrary", this.getDataFolder().getAbsolutePath(), "bookshelves");
    	try {
    		sqlite.open();
    	} catch (Exception e) {
    		logger.info(e.getMessage());
            getPluginLoader().disablePlugin(this);
    	}
    }
    
    private void dbSetup() {
    	try {
    		sqlite.query("CREATE TABLE IF NOT EXISTS shelves (id INTEGER PRIMARY KEY, x INT, y INT, z INT, contents STRING, UNIQUE ( x, y, z ))");
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
}
