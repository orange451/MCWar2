package com.orange451.mcwarfare.player;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameKillstreak extends GameItem {
	private int tier;
	private Material itemRepresentation;
	private int killsNeeded = 0;
	private String description;
	
	public GameKillstreak(String name, int tier, int killsNeeded, Material itemRepresentation, String string) {
		this.name = name;
		this.tier = tier;
		this.itemRepresentation = itemRepresentation;
		this.killsNeeded = killsNeeded;
		this.description = string;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public int getTier() {
		return this.tier;
	}
	
	public ItemStack getKillstreakAsItem() {
		ItemStack itm = new ItemStack(this.itemRepresentation, 1);
		ItemMeta meta = itm.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + name);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + description);
		lore.add(ChatColor.WHITE + "Needs " + ChatColor.GREEN + killsNeeded + ChatColor.WHITE + " kills");
		lore.add(ChatColor.WHITE + "Tier " + ChatColor.GREEN + tier);
		meta.setLore(lore);
		itm.setItemMeta(meta);
		return itm;
	}
	
	public int getKillsNeeded() {
		return this.killsNeeded;
	}
	
	public void execute(GamePlayer executer) {
	}
}