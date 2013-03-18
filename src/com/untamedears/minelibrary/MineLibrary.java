package com.untamedears.minelibrary;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class MineLibrary extends JavaPlugin implements Listener {
	public Map<Player, Location> inventories = new HashMap<Player, Location>();
	@Override
    public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("MineLibrary has been enabled.");
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("MineLibrary has been disabled.");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.BOOKSHELF)) {
    		Location l = event.getClickedBlock().getLocation();
    		
    		BookshelfIO io = new BookshelfIO();
    		
    		Inventory i = this.getServer().createInventory(null, 9, "Bookshelf");
    		
    		if(io.readBookshelf(l) != null) {
    			ItemStack[] inv = io.readBookshelf(l);
       			i.setContents(inv);
    		}
    		
    		inventories.put(event.getPlayer(), l);
    		
    		event.getPlayer().openInventory(i);
    	}
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
    	if(inventories.containsKey(event.getPlayer())) {
    		Location l = inventories.get(event.getPlayer());
    		
    		BookshelfIO io = new BookshelfIO();
    		io.saveBookshelf(event.getInventory(), l);
    		
    		inventories.remove(event.getPlayer());
    	}
    }
}
