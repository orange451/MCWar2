package com.orange451.mcwarfare;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.orange451.mcwarfare.arena.Arena;
import com.orange451.mcwarfare.player.GameProfile;
import com.orange451.mcwarfare.player.GameRank;

public class CommandExecuter {

	public void executeCommand(CommandSender sender, String commandLabel, String[] args) {
		Player player = null;
		boolean fromConsole = false;
		boolean candocommand = false;
		
		if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
			candocommand = true;
			fromConsole = true;
		}
		if (sender instanceof Player) {
			player = (Player)sender;
			if (sender.isOp())
				candocommand = true;
		}

		if (commandLabel.equals("stop")) {
			Player[] players = Bukkit.getOnlinePlayers();
			ArrayList<Arena> arenas = MCWarfare.getPlugin().arenas;
			for (int i = 0; i < arenas.size(); i++) {
				arenas.get(i).FORCECLOSE();
			}
			MCWarfare.getPlugin().arenas.clear();
			for (int i = players.length - 1; i >= 0; i--) {
				players[i].kickPlayer("Server is restarting. Please rejoin in 10 seconds!");
			}
		}
		
		if (commandLabel.equals("profile") && args.length == 1) {
			GameProfile profile = new GameProfile(args[0], true);
			if (profile != null && profile.loaded) {
				if (fromConsole) {
					profile.dumpClass();
				}else{
					GameRank rank = profile.getRank();
					player.sendMessage("-----------------------------------------------------");
					player.sendMessage(ChatColor.DARK_GRAY + "Viewing profile of: " + ChatColor.WHITE + profile.getPlayername());
					player.sendMessage(ChatColor.DARK_GRAY + "Rank: " + ChatColor.GRAY + rank.getRankName() + " (" + rank.getTag() + ")");
					player.sendMessage(ChatColor.DARK_GRAY + "Level: " + ChatColor.GRAY + profile.getLevel());
					player.sendMessage(ChatColor.DARK_GRAY + "Kills: " + ChatColor.GRAY + profile.getKills());
					player.sendMessage(ChatColor.DARK_GRAY + "Deaths: " + ChatColor.GRAY + profile.getDeaths());
					player.sendMessage(ChatColor.DARK_GRAY + "KDR: " + ChatColor.GRAY + profile.getKDR());
					player.sendMessage("-----------------------------------------------------");
				}
				return;
			}
		}
		
		if (candocommand) {
			if (args.length == 1) {
				GameProfile profile = new GameProfile(args[0], true);
				if (profile != null && profile.loaded) {
					if (commandLabel.equals("giveclass")) { //Give a player a new class
						profile.addClass();
						System.out.println("new class added to profile");
						profile.save(false,MCWarfare.getPlugin().getSQLController());
						return;
					}
				}
			}
			if (args.length == 2) {
				GameProfile profile = new GameProfile(args[0], true);
				if (profile != null && profile.loaded) {
			
					
					if (commandLabel.equals("givegun")) { //Give a gun to a player
						profile.giveGun(args[1]);
						System.out.println("Gun added to profile");
						profile.save(false, MCWarfare.getPlugin().getSQLController());
						return;
					}
					
					if (commandLabel.equals("removegun")) { //Remove a gun from a player
						profile.removeGun(args[1]);
						System.out.println("Gun removed from profile");
						profile.save(false, MCWarfare.getPlugin().getSQLController());
						return;
					}
					
					if (commandLabel.equals("givexp")) { //Give xp to a player
						try{
							profile.giveXp(Integer.parseInt(args[1]));
							System.out.println("XP added to profile");
							profile.save(false, MCWarfare.getPlugin().getSQLController());
						}catch(Exception e) {
							//
						}
						return;
					}
					
					if (commandLabel.equals("giveperk")) { //Give a perk to a player
						profile.givePerk(args[1]);
						System.out.println("Perk added to profile");
						profile.save(false, MCWarfare.getPlugin().getSQLController());
						return;
					}
					
					if (commandLabel.equals("givekillstreak")) { //Give a killstreak to a player
						profile.giveKillstreak(args[1]);
						System.out.println("Killstreak added to profile");
						profile.save(false, MCWarfare.getPlugin().getSQLController());
						return;
					}
				}else{
					System.out.println("ERROR READING PROFILE: " + args[1]);
				}
			}
			
			if (commandLabel.equals("permission")) { //Permission commands
				if (args.length == 3) {
					GameProfile profile = new GameProfile(args[1], true);
					if (profile != null && profile.loaded) {
						if (args[0].equals("add")) {
							profile.givePermission(args[2]);
							System.out.println("Permission added to profile");
							profile.save(false,MCWarfare.getPlugin().getSQLController());
						}else if (args[0].equals("remove")) {
							profile.removePermission(args[2]);
							System.out.println("Permission remove from profile");
							profile.save(false,MCWarfare.getPlugin().getSQLController());
						} 
					}else{
						System.out.println("ERROR READING PROFILE: " + args[1]);
					}
				}
				return;
			}
		}
	}

}
