package com.orange451.mcwarfare.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Util {
	public static void playEffect(Effect e, Location l, int num) {
		for(int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; ++i) {
			Bukkit.getServer().getOnlinePlayers()[i].playEffect(l, e, num);
		}
	}
	
	public static void playSound(Sound e, Location l, int volume, double pitch) {
		for(int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; ++i) {
			Bukkit.getServer().getOnlinePlayers()[i].playSound(l, e, volume, (float) pitch);
		}
	}

	public static Player MatchPlayer(String player) {
		if (player == null)
			return null;
		List<Player> players = Bukkit.getServer().matchPlayer(player);

		if (players.size() == 1) {
			return (Player)players.get(0);
		}
		return null;
	}


	public static ItemStack namedItemStack(Material mat, int amount, String name, String loreColor, String description) {
		ItemStack ret = new ItemStack(mat, amount);
		ItemMeta meta = ret.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(new ArrayList<String>());
		if (description != null) {
			ArrayList<String> lore = new ArrayList<String>();
			String[] temp = description.split("/");
			for (int i = 0; i < temp.length; i++) {
				lore.add(loreColor + temp[i]);
			}
			meta.setLore(lore);
		}
		ret.setItemMeta(meta);
		return ret;
	}

	public static double point_distance(Location loc1, Location loc2) {
		double p1x = loc1.getX();
		double p1y = loc1.getY();
		double p1z = loc1.getZ();
		double p2x = loc2.getX();
		double p2y = loc2.getY();
		double p2z = loc2.getZ();
		double xdist = p1x - p2x;
		double ydist = p1y - p2y;
		double zdist = p1z - p2z;
		return Math.sqrt(xdist * xdist + ydist * ydist + zdist * zdist);
	}

	public static int random(int x) {
		Random rand = new Random();
		return rand.nextInt(x);
	}

	public static double lengthdir_x(double len, double dir) {
		return len * Math.cos(Math.toRadians(dir));
	}

	public static double lengthdir_y(double len, double dir) {
		return -len * Math.sin(Math.toRadians(dir));
	}

	public static double point_direction(double x1, double y1, double x2, double y2) {
		double d;
		try {
			d = Math.toDegrees(Math.atan((y2 - y1) / (x2 - x1)));
		} catch (Exception var11) {
			d = 0.0D;
		}

		if(x1 > x2 && y1 > y2) {
			return -d + 180.0D;
		} else if(x1 < x2 && y1 > y2) {
			return -d;
		} else {
			if(x1 == x2) {
				if(y1 > y2) {
					return 90.0D;
				}

				if(y1 < y2) {
					return 270.0D;
				}
			}

			if(x1 > x2 && y1 < y2) {
				return -d + 180.0D;
			} else if(x1 < x2 && y1 < y2) {
				return -d + 360.0D;
			} else {
				if(y1 == y2) {
					if(x1 > x2) {
						return 180.0D;
					}

					if(x1 < x2) {
						return 0.0D;
					}
				}

				return 0.0D;
			}
		}
	}

	public static boolean isBlockSolid(Block block) {
		return block.getType().isSolid();
	}

	public static ItemStack appendItemStackLore(ItemStack itemStack, String strClass) {
		ItemMeta meta = itemStack.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null)
			lore = new ArrayList<String>();
		lore.add(strClass);
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
}
