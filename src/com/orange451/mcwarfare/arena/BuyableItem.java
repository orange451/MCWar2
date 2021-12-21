package com.orange451.mcwarfare.arena;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.orange451.mcwarfare.player.GamePlayer;

public class BuyableItem {
	public String name;
	public int cost;
	public Material mat;
	
	public BuyableItem(String name, int cost, Material mat) {
		this.name = name;
		this.cost = cost;
		this.mat = mat;
	}
	
	public ItemStack getAsItem() {
		ItemStack item = new ItemStack(mat, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + name + " " + ChatColor.GREEN + "$" + cost);
		item.setItemMeta(meta);
		return item;
	}
	
	public void execute(GamePlayer player) {
	}
}
