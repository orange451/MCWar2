package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class ArenaCreator {
	public Player player;
	public Location corner1;
	public Location corner2;
	public String arenaName;
	public String creator = "N/A";
	public ArrayList<Location> ffa_spawns = new ArrayList<Location>();
	public ArrayList<Location> tdm_spawns = new ArrayList<Location>();
	public ArrayList<Location> ctf_flags = new ArrayList<Location>();
	public ArrayList<Location> dom_flags = new ArrayList<Location>();

	public ArenaCreator(Player player, String arenaName2) {
		this.player = player;
		this.arenaName = arenaName2;
		player.sendMessage(ChatColor.GRAY + "STARTING TO CREATE ARENA!");
		player.sendMessage(ChatColor.GRAY + "  PLEASE SET CORNER 1 LOCATION");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "    /war map setpoint");
		
		System.out.println(player.getName() + " has started creating an arena");
	}

	public void setPoint() {
		Location ploc = this.player.getLocation();
		boolean changed = false;
		if (this.corner1 == null) {
			this.corner1 = ploc;
			changed = true;
			this.player.sendMessage(ChatColor.GRAY + "CORNER 1 LOCATION SET");
			this.player.sendMessage(ChatColor.GRAY + "  SET CORNER 2 LOCATION");
			return;
		}
		if (this.corner2 == null) {
			this.corner2 = ploc;
			changed = true;
			this.player.sendMessage(ChatColor.GRAY + "CORNER 2 LOCATION SET");
			this.player.sendMessage(ChatColor.GRAY + "/war map addspawn tdm " + ChatColor.WHITE + " set spawns for TDM games (blue-->red)");
			this.player.sendMessage(ChatColor.GRAY + "/war map addspawn ffa " + ChatColor.WHITE + " set spawns for FFA games");
			this.player.sendMessage(ChatColor.GRAY + "/war map addspawn ctf " + ChatColor.WHITE + " add CTF flag (blue-->red)");
			this.player.sendMessage(ChatColor.GRAY + "/war map addspawn dom " + ChatColor.WHITE + " add domination flagpoint");
			this.player.sendMessage(ChatColor.GRAY + "/war map setcreator {name} " + ChatColor.WHITE + " set the name of the creator");
			this.player.sendMessage(ChatColor.GRAY + "/war map done " + ChatColor.WHITE + " When you are done");
			return;
		}

		if (!changed) {
			finish();
		}

		MCWarfare.getPlugin().getArenaManager().loadArena(this.arenaName);
	}

	public void finish() {
		MCWarfare.getPlugin().getArenaManager().stopMakingArena(this.player);
		saveArena();
	}

	private void saveArena() {
		if (this.ffa_spawns.size() > 0) {
			this.saveArenaByType(ArenaType.FREE_FOR_ALL.toString(), ArenaModifier.NONE.toString());
			this.saveArenaByType(ArenaType.FREE_FOR_ALL.toString(), ArenaModifier.ONE_IN_THE_CHAMBER.toString());
			this.saveArenaByType(ArenaType.FREE_FOR_ALL.toString(), ArenaModifier.GUN_GAME.toString());
			this.saveArenaByType(ArenaType.FREE_FOR_ALL.toString(), ArenaModifier.GOLDEN_GUN.toString());
		}
		
		if(this.tdm_spawns.size() > 0) {
			if (this.ctf_flags.size() > 0) {
				this.saveArenaByType(ArenaType.TEAM_DEATHMATCH.toString(), ArenaModifier.CAPTURE_THE_FLAG.toString());
			}
			if (this.dom_flags.size() > 0) {
				this.saveArenaByType(ArenaType.TEAM_DEATHMATCH.toString(), ArenaModifier.DOMINATION.toString());
			}
			this.saveArenaByType(ArenaType.TEAM_DEATHMATCH.toString(), ArenaModifier.INFECTION.toString());
			this.saveArenaByType(ArenaType.TEAM_DEATHMATCH.toString(), ArenaModifier.KILL_CONFIRMED.toString());
			this.saveArenaByType(ArenaType.TEAM_DEATHMATCH.toString(), ArenaModifier.NONE.toString());
		}
	}
	
	private void saveArenaByType(String type, String modifier) {
		String path = MCWarfare.getPlugin().getPluginFolder() + "/arenas/" + this.arenaName + "_" + type + "_" + modifier;
		FileWriter outFile = null;
		PrintWriter out = null;
		try {
			outFile = new FileWriter(path);
			out = new PrintWriter(outFile);
			out.println(this.corner1.getBlockX() + "," + this.corner1.getBlockZ());
			out.println(this.corner2.getBlockX() + "," + this.corner2.getBlockZ());

			out.println("--config--");
			out.println("type=" + type);
			out.println("modifier=" + modifier);
			out.println("realname=" + this.arenaName);
			out.println("creator=" + this.creator);
			out.println("mood=day");
			out.println("maxPlayers=" + Bukkit.getServer().getMaxPlayers());
			out.println("minPlayers=" + (Bukkit.getServer().getMaxPlayers() - 40));
			if (this.tdm_spawns.size() > 0 && type.contains("TEAM_")) {
				for (int i = 0; i < this.tdm_spawns.size(); i++) {
					out.println("addspawn=" + ((Location)this.tdm_spawns.get(i)).getBlockX() + "," + ((Location)this.tdm_spawns.get(i)).getBlockY() + "," + ((Location)this.tdm_spawns.get(i)).getBlockZ());
				}
			}
			if (this.ffa_spawns.size() > 0 && type.contains("FREE")) {
				for (int i = 0; i < this.ffa_spawns.size(); i++) {
					out.println("addspawn=" + ((Location)this.ffa_spawns.get(i)).getBlockX() + "," + ((Location)this.ffa_spawns.get(i)).getBlockY() + "," + ((Location)this.ffa_spawns.get(i)).getBlockZ());
				}
			}

			if (modifier.contains("THE_FLAG")) {
				for (int i = 0; i < this.ctf_flags.size(); i++) {
					out.println("addspawn=" + ((Location)this.ctf_flags.get(i)).getBlockX() + "," + ((Location)this.ctf_flags.get(i)).getBlockY() + "," + ((Location)this.ctf_flags.get(i)).getBlockZ());
				}
			}
			
			if (modifier.contains("DOMINATION")) {
				for (int i = 0; i < this.dom_flags.size(); i++) {
					out.println("addspawn=" + ((Location)this.dom_flags.get(i)).getBlockX() + "," + ((Location)this.dom_flags.get(i)).getBlockY() + "," + ((Location)this.dom_flags.get(i)).getBlockZ());
				}
			}
			
			System.out.println("ARENA: " + this.arenaName + "_" + modifier + " SUCCESFULLY SAVED!");
			this.player.sendMessage(ChatColor.YELLOW + "Arena " + this.arenaName + "_" + modifier + " Saved!");
		} catch (IOException localIOException) {
			
		}
		try {
			out.close();
			outFile.close();
		}
		catch (Exception localException) {
		}
	}

	public void addSpawn(String type) {
		if (type.equals("tdm"))
			this.tdm_spawns.add(this.player.getLocation().clone());
		else if (type.equals("ctf"))
			this.ctf_flags.add(this.player.getLocation().clone());
		else if (type.equals("dom"))
			this.dom_flags.add(this.player.getLocation().clone());
		else
			this.ffa_spawns.add(this.player.getLocation().clone());
	}

	public void doCommand(String[] args) {
		String command = args[1];
		if (command.equals("addspawn")) {
			this.addSpawn(args[2]);
		}
		if (command.equals("setcreator")) {
			this.creator = args[2];
		}
		if (command.equals("setpoint")) {
			this.setPoint();
		}
		if (command.equals("done")) {
			this.finish();
		}
	}
}