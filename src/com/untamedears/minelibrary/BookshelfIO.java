package com.untamedears.minelibrary;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

public class BookshelfIO {
	public final Plugin plugin;
	
	public BookshelfIO() {
		this.plugin = Bukkit.getServer().getPluginManager().getPlugin("MineLibrary");
	}
	
	public void saveBookshelf(Inventory i, Location l) {
		String loc = this.getLocString(l);
		
		int item = 0;
		for(ItemStack is : i) {
			String bookStr = "books."+loc+"."+item+".";
			//if it's a book
			if(is != null && is.hasItemMeta() && (is.getItemMeta() instanceof BookMeta) && is.getType().equals(Material.WRITTEN_BOOK)) {
				BookMeta bm =  ((BookMeta)is.getItemMeta());
				
				String author = (bm.hasAuthor()) ? bm.getAuthor() : "";
				String title = (bm.hasTitle()) ? bm.getTitle() : "";
				List<String> pages = (bm.hasPages()) ? bm.getPages() : new ArrayList<String>();
				
				this.plugin.getConfig().set(bookStr + "isBook", true);
				this.plugin.getConfig().set(bookStr + "author", author);
				this.plugin.getConfig().set(bookStr + "title", title);
				this.plugin.getConfig().set(bookStr + "pages", pages);
			} else { //if it's not a book, throw it out of inventory
				this.plugin.getConfig().set(bookStr + "isBook", false);
				
				if(is != null) l.getWorld().dropItem(l, is);
			}
			item++;
		}
		
		this.plugin.saveConfig();
		this.plugin.reloadConfig();
	}
	
	public ItemStack[] readBookshelf(Location l) {
		String loc = this.getLocString(l);
		
		ItemStack[] inventory = new ItemStack[9];
		
		FileConfiguration c = this.plugin.getConfig();
		
		for(int i = 0; i < 9; i++) {
			String bookStr = "books."+loc+"."+i+".";
			if(c.getBoolean(bookStr+"isBook")) {
				
				String author = c.getString(bookStr+"author");
				String title = c.getString(bookStr+"title");
				List<String> pages = c.getStringList(bookStr+"pages");
				
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
				
				BookMeta bookMeta = ((BookMeta)is.getItemMeta());
				
				bookMeta.setAuthor(author);
				bookMeta.setTitle(title);
				bookMeta.setPages(pages);
				
				is.setItemMeta(bookMeta);
				
				inventory[i] = is;
			} else {
				inventory[i] = new ItemStack(Material.AIR, 0);
			}
			
		}
		
		return inventory;
	}
	
	public void emptyBookshelf(Location l) {
		this.plugin.getConfig().set("books."+this.getLocString(l), null);
		this.plugin.saveConfig();
		this.plugin.reloadConfig();
	}
	
	private String getLocString(Location l) {
		int x = (int)l.getX();
		int y = (int)l.getY();
		int z = (int)l.getZ();
		
		return "" + x + "" + y + "" + z;
	}
}