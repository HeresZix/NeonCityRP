package com.Zix.CityRP;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItems {
	public static Main plugin;
	
	static String batonname = ChatColor.GOLD + "Police Baton";
	static ItemStack baton = new ItemStack(Material.STICK);
	
	@SuppressWarnings("deprecation")
	public static void giveBaton(Player p){		
		ItemMeta batonmeta = baton.getItemMeta();
		batonmeta.setDisplayName(batonname);
		baton.setItemMeta(batonmeta);		
		p.getInventory().addItem(baton);
		p.updateInventory();
	}
	
	public static boolean hasBaton(Player p){
		Inventory inv = p.getInventory();
		if(inv.contains(baton)){
			return true;
		} else {
			return false;
		}
	}
}