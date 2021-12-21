package com.orange451.mcwarfare.arena;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.orange451.mcwarfare.player.GameItem;
import com.orange451.mcwarfare.player.GameProfile;

public class KitGun extends GameItem {
	public String desc;
	public String slot;
	public String gunClass = "";
	public int cost;
	public int level;
	public int type;
	public int amount = 1;

	public KitGun(String name, String desc, int cost, int level, int type) {
		this.name = name;
		this.desc = desc;
		this.cost = cost;
		this.level = level;
		this.type = type;
	}

	public KitGun() {
	}

	public boolean isUnlocked(GameProfile kp) {
		return kp.getLevel() >= this.level;
	}
	
	public ItemStack getItemStack() {
		ItemStack itm = new ItemStack(type, amount);
		ItemMeta meta = itm.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + name);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + desc);
		meta.setLore(lore);
		itm.setItemMeta(meta);
		return itm;
	}
}