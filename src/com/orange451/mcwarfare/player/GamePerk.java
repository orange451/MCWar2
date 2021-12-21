package com.orange451.mcwarfare.player;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GamePerk extends GameItem {
	private Material itemRepresentation;
	private String description;
	
	public GamePerk(String name, Material itemRepresentation, String description) {
		this.name = name;
		this.itemRepresentation = itemRepresentation;
		this.description = description;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ItemStack getPerkAsItem() {
		ItemStack itm = new ItemStack(this.itemRepresentation, 1);
		ItemMeta meta = itm.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Perk " + ChatColor.GREEN + name);
		if (description != null) {
			ArrayList<String> lore = new ArrayList<String>();
			String[] temp = description.split("/");
			for (int i = 0; i < temp.length; i++)
				lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + temp[i]);
			meta.setLore(lore);
			
		}
		itm.setItemMeta(meta);
		return itm;
	}
	
	public GamePerk clone() {
		return new GamePerk(name, this.itemRepresentation, this.description);
	}
}
