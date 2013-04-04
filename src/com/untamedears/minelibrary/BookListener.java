package com.untamedears.minelibrary;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookListener implements Listener {
	private final MineLibrary ml;
	private ResultSet r;
	
	public BookListener(MineLibrary mineLibrary) {
		this.ml = mineLibrary;
	}
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.BOOKSHELF)) {
    		if(!event.getPlayer().getItemInHand().getType().isBlock() || !event.getPlayer().isSneaking()) {
    			Location l = event.getClickedBlock().getLocation();
        		
    			ml.inventories.put(event.getPlayer(), l);
	        		
	        	event.getPlayer().openInventory(readBookshelf(l));
    		}
    	}
    }
    
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
    	if(event.getEntity().getShooter().getTargetBlock(null, 50).getType().equals(Material.BOOKSHELF)) {
    		event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
    	if(ml.inventories.containsKey(event.getPlayer())) {
    		Location l = (Location) ml.inventories.get(event.getPlayer());

    		String contents = "";
    		String sep = "<@split!>";
    		String book = "<@book!>";
    		
    		Inventory i = event.getInventory();
    		for(ItemStack is : i) {
				if(is != null && is.hasItemMeta() && (is.getItemMeta() instanceof BookMeta) && is.getType().equals(Material.WRITTEN_BOOK)) {
					BookMeta bm =  ((BookMeta)is.getItemMeta());
					
					String author = (bm.hasAuthor()) ? bm.getAuthor() : "";
					String title = (bm.hasTitle()) ? bm.getTitle() : "";
					List<String> pages = (bm.hasPages()) ? bm.getPages() : new ArrayList<String>();
					
					contents += book + author + sep + title;
					
					if(!pages.isEmpty()) {
						for(String page : pages) {
							contents += sep + page;
						}
					}
				} else { //if it's not a book, throw it out of inventory
					if(is != null) l.getWorld().dropItem(l, is);
				}
    		}
    		try {
    			PreparedStatement ps = ml.sqlite.prepare("UPDATE shelves SET contents = ? WHERE x = ? AND y = ? AND z = ?");
        		ps.setString(1, contents);
        		ps.setInt(2, l.getBlockX());
        		ps.setInt(3, l.getBlockY());
        		ps.setInt(4, l.getBlockZ());
        		ml.sqlite.query(ps);
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    		
    		ml.inventories.remove(event.getPlayer());
    	}
    }
    
    //TODO check compatibility with Citadel so books don't drop when reinforced block is broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	if(event.getBlock().getType().equals(Material.BOOKSHELF)) {
    		Location l = event.getBlock().getLocation();
    		
			for(ItemStack is : readBookshelf(l)) {
				if(is != null) {
					l.getWorld().dropItemNaturally(l, is);
				}
			}
			
			try {
				ml.sqlite.query("DELETE FROM shelves WHERE x = "+l.getBlockX()+" AND y = "+l.getBlockY()+" AND z = "+l.getBlockZ());
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
    	if(!event.getPlayer().isSneaking() && event.getBlockAgainst().getType().equals(Material.BOOKSHELF)) {
    		event.setCancelled(true);
    	}
    }
    
    public Inventory readBookshelf(Location l) {
    	Inventory i = Bukkit.createInventory(null, 9, "Bookshelf");
    	
    	try {
    		r = ml.sqlite.query("SELECT * FROM shelves WHERE x="+l.getBlockX()+" AND y="+l.getBlockY()+" AND z="+l.getBlockZ()+";");
			
    		if(r.next()) {
    			String contents = r.getString("contents");
    			r.close();
    			
    			if(contents != null) {
        			String[] books = contents.split("<@book!>");
        			if(books.length > 0) {
        				for(String book : books) {
        					String[] bookContents = book.split("<@split!>");
        					if(bookContents.length > 2) {
    							ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
    							
    							BookMeta bookMeta = ((BookMeta)is.getItemMeta());
    							
    							bookMeta.setAuthor(bookContents[0]);
    							bookMeta.setTitle(bookContents[1]);
    							List<String> pages = new ArrayList<String>();
    							for(int j = 2; j < bookContents.length; j++) {
    								pages.add(bookContents[j]);
    							}
    							if(!pages.isEmpty()) {
    								bookMeta.setPages(pages);
    							}
    							
    							is.setItemMeta(bookMeta);
    							
    							i.addItem(is);
    						}
        				}
        			}
    			} 
    		} else {
				r.close();
    			ml.sqlite.query("INSERT INTO shelves (x,y,z) VALUES ("+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ()+");");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return i;
    }
}
