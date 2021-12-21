package com.orange451.mcwarfare;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.util.FileIO;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.gun.Gun;

public class GunManager {

	public int getGunAmmo(int typeId) { //returns the type of ammo a gun uses (requires the guns item id)
		Plugin p = Bukkit.getPluginManager().getPlugin("PVPGunPlus");
		if (p != null) {
			PVPGunPlus pv = (PVPGunPlus)p;
			if (pv != null) {
				Gun g = pv.getGun(typeId);
				if (g != null) {
					return g.getAmmoMaterial().getId();
				}
			}
		}
		return 0;
	}
	
	public int getXpPerKill(GamePlayer player) { //Returns the amount of XP you gain per kill
		int ret = 25;
		if (player.getProfile().hasPermission("doublexp"))
			ret += 25;
		if (player.getProfile().hasPermission("vip"))
			ret += 10;
		if (player.getProfile().hasPermission("mvp"))
			ret += 20;
		
		return ret;
	}
	
	public int getCreditserKill(GamePlayer player) { //Returns the amount of credits you gain per kill
		if (player.getProfile().hasPermission("mvp"))
			return 3;
		if (player.getProfile().hasPermission("vip"))
			return 2;
		return 1;
	}
	
	public int getMaxAmmo(GamePlayer player) { //Returns the max amount of ammo
		int baseAmmo = 64;
		if (player.getProfile().hasPermission("mvp")) {
			return baseAmmo * 2;
		}else if (player.getProfile().hasPermission("vip")) {
			return (int) (baseAmmo * 1.5);
		}
		return baseAmmo;
	}

	public void initialise() { //LOAD GUNS INTO MCWAR
		String path = MCWarfare.getPlugin().getFTP() + "/buyable/weapons";
		BufferedReader out = FileIO.file_text_open_read(path);
		boolean reading = true;
		boolean isReadingGun = false;
		KitGun gun = null;
		int line = 0;
		while ((reading) && (line < 512)) {
			line++;
			String read = FileIO.file_text_read_line(out);
			if (read == null) {
				reading = false;
				return;
			}
			if (read.equalsIgnoreCase("::defgun")) {
				isReadingGun = true;
				gun = new KitGun();
			}
			if (read.equalsIgnoreCase("::endgun")) {
				isReadingGun = false;
				MCWarfare.getPlugin().loadedGuns.add(gun);
			}

			if (isReadingGun) {
				if (read.contains("name")) {
					gun.name = read.substring(read.indexOf("=") + 1);
				}
				if (read.contains("desc"))
					gun.desc = read.substring(read.indexOf("=") + 1);
				if (read.contains("cost"))
					gun.cost = Integer.parseInt(read.substring(read.indexOf("=") + 1));
				if (read.contains("level"))
					gun.level = Integer.parseInt(read.substring(read.indexOf("=") + 1));
				if (read.contains("type"))
					gun.type = Integer.parseInt(read.substring(read.indexOf("=") + 1));
				if (read.contains("amount"))
					gun.amount = Integer.parseInt(read.substring(read.indexOf("=") + 1));
				if (read.contains("slot")) {
					gun.slot = read.substring(read.indexOf("=") + 1);
				}
				if (read.contains("class")) {
					gun.gunClass = read.substring(read.indexOf("=") + 1);
				}
			}
		}
		FileIO.file_text_close(out);
	}

	public int getFirstGunByType(String string) {
		ArrayList<KitGun> guns = MCWarfare.getPlugin().loadedGuns;
		for (int i = 0; i < guns.size(); i++) {
			if (guns.get(i).slot.equals(string)) {
				return guns.get(i).type;
			}
		}
		return Material.WOOD_HOE.getId();
	}

	public KitGun getGun(ItemStack a) {
		for (int i = 0; i < MCWarfare.getPlugin().loadedGuns.size(); i++) {
			if (a.getTypeId() == MCWarfare.getPlugin().loadedGuns.get(i).type) {
				return MCWarfare.getPlugin().loadedGuns.get(i);
			}
		}
		return null;
	}

	public KitGun getGun(String temp) {
		for (int i = 0; i < MCWarfare.getPlugin().loadedGuns.size(); i++) {
			if (MCWarfare.getPlugin().loadedGuns.get(i).name.equalsIgnoreCase(temp)) {
				return MCWarfare.getPlugin().loadedGuns.get(i);
			}
		}
		return null;
	}

}
