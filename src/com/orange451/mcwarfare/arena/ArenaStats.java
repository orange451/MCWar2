package com.orange451.mcwarfare.arena;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.killstreaks.MapItem;
import com.orange451.mcwarfare.arena.killstreaks.TacticalInsertion;
import com.orange451.mcwarfare.player.GamePlayer;

public class ArenaStats {
	private Arena arena;
	
	public ArenaStats(Arena arena) {
		this.arena = arena;
	}
	
	public GamePlayer getPlayerWithMostKills() { //Returns the player with the most kills
		GamePlayer ret = null;
		int kills = 0;
		ArrayList<GamePlayer> players = this.arena.getPlayers();
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (gplayer.getKills() > kills) {
				ret = gplayer;
				kills = gplayer.getKills();
			}
		}
		return ret;
	}
	
	public GamePlayer getLastPlayer() { //Returns the last player alive
		ArrayList<GamePlayer> players = this.arena.getPlayers();
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (gplayer != null && gplayer.getPlayer().isOnline()) {
				return gplayer;
			}
		}

		return null;
	}
	
	public int getAmountPlayersOnTeam(Team team) {
		int a = 0;
		ArrayList<GamePlayer> players = this.arena.getPlayers();
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (gplayer.getTeam().equals(team)) {
				a++;
			}
		}
		return a;
	}
	
	public ArenaSpawn getSpawn(GamePlayer player) { //Return the most appropriate spawn for a player
		if (!arena.getArenaType().equals(ArenaType.SPAWN)) {
			List<MapItem> items = MCWarfare.getPlugin().mapItems;
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i) instanceof TacticalInsertion) {
					TacticalInsertion tacinsert = (TacticalInsertion)items.get(i);
					if (tacinsert.getOwner().equals(player)) {
						tacinsert.remove();
						return new ArenaSpawn(tacinsert.getLocation(), Team.BLUE);
					}
				}
			}
		}
		if (this.arena.getArenaType().equals(ArenaType.TEAM_DEATHMATCH)) { //This returns the first spawn when your team color on it
			return getFirstSpawnByTeam(player.getTeam());
		}else if (this.arena.getArenaType().equals(ArenaType.FREE_FOR_ALL)) { //This returns the spawn that is farthest away from all players
			double farthest = 0;
			ArenaSpawn ret = arena.spawns.get(0);
			for (int i = 0; i < this.arena.spawns.size(); i++) {
				ArenaSpawn spawn = this.arena.spawns.get(i);
				double nearest = 9999999;
				ArrayList<GamePlayer> players = this.arena.getPlayers();
				for (int ii = players.size() - 1; ii >= 0; ii--) {
					GamePlayer gplayer = (GamePlayer)players.get(ii);
					if (!gplayer.equals(player)) {
						double distance = spawn.getLocation().distance(player.getPlayer().getLocation());
						if (distance < nearest) {
							nearest = distance;
						}
					}
				}
				
				if (nearest > farthest) {
					farthest = nearest;
					ret = spawn;
				}
			}
			return ret;
		}
		if (arena.spawns.size() > 0)
			return arena.spawns.get(0); //Return the first spawn added into the arena
		return null;
	}
	
	public ArenaSpawn getSpawnAtRoundStart(GamePlayer player) { //Return the most appropriate spawn for a player
		if (this.arena.getArenaType().equals(ArenaType.FREE_FOR_ALL)) {
			double least = 1000;
			ArenaSpawn ret = arena.spawns.get(0);
			for (int i = 0; i < this.arena.spawns.size(); i++) {
				ArenaSpawn check = this.arena.spawns.get(i);
				if (check.getLastTick() < least) {
					least = check.getLastTick();
					ret = check;
				}
			}
			return ret;
		}
		if (arena.spawns.size() > 0)
			return arena.spawns.get(0); //Return the first spawn added into the arena
		return null;
	}
	
	public ArenaSpawn getFirstSpawnByTeam(Team team) { //Returns the first spawn with a specific team color
		for (int i = 0; i < arena.spawns.size(); i++) {
			if (arena.spawns.get(i).getTeam().equals(team)) {
				return arena.spawns.get(i);
			}
		}
		System.out.println("COULD NOT FIND SPAWN");
		return null;
	}
	
	public int getTeamKills(Team team) {
		if (team.equals(Team.BLUE))
			return this.arena.bluekills;
		if (team.equals(Team.RED))
			return this.arena.redkills;
		return 0;
	}
	
	public String getLeader() { //Return the leader in string form
		String ret = ChatColor.GRAY + "none  ";
		if (this.arena.getArenaType().equals(ArenaType.TEAM_DEATHMATCH)) {
			if (this.arena.getArenaModifier().equals(ArenaModifier.CAPTURE_THE_FLAG) || this.arena.getArenaModifier().equals(ArenaModifier.KILL_CONFIRMED) || this.arena.getArenaModifier().equals(ArenaModifier.DOMINATION)) {
				if (this.arena.bluescore > this.arena.redscore)
					return ChatColor.BLUE + "blue(" + Integer.toString(this.arena.bluescore - this.arena.redscore) + ")  " + ChatColor.WHITE;
				if (this.arena.bluescore < this.arena.redscore)
					return ChatColor.RED + "red(" + Integer.toString(this.arena.redscore - this.arena.bluescore) + ")   " + ChatColor.WHITE;
			} else {
				if (this.arena.bluekills > this.arena.redkills)
					return ChatColor.BLUE + "blue(" + Integer.toString(this.arena.bluekills - this.arena.redkills) + ")  " + ChatColor.WHITE;
				if (this.arena.bluekills < this.arena.redkills) {
					return ChatColor.RED + "red(" + Integer.toString(this.arena.redkills - this.arena.bluekills) + ")   " + ChatColor.WHITE;
				}
			}

			return ChatColor.YELLOW + "tie(0)  " + ChatColor.WHITE;
		}else if (this.arena.getArenaType().equals(ArenaType.FREE_FOR_ALL)) {
			if (this.arena.getArenaModifier().equals(ArenaModifier.GUN_GAME)) {
				/*GamePlayer mostkills = getPlayerWithHighestGunGameRank();
				if (mostkills != null)
					return ChatColor.RED + mostkills.player.getName() + "(" + mostkills.gungameLevel + ")  ";*/
			} else {
				GamePlayer mostkills = getPlayerWithMostKills();
				if (mostkills != null) {
					return ChatColor.RED + mostkills.getPlayer().getName() + "(" + mostkills.getKills() + ")  ";
				}
			}
		}
		return ret;
	}

	protected void loadArena() {
		String path = MCWarfare.getPlugin().getPluginFolder() + "/arenas/" + this.arena.getName();
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			fstream = new FileInputStream(path);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			Location loc1 = getLocationFromString(br.readLine());
			Location loc2 = getLocationFromString(br.readLine());

			this.arena.field = new Rectangle();
			this.arena.field.setBounds((int)loc1.getX(), (int)loc1.getZ(), (int)Math.abs(loc2.getX() - loc1.getX()), (int)Math.abs(loc2.getZ() - loc1.getZ()));
		} catch (Exception e) {
			System.err.print("ERROR READING ARENA");
		}
		loadConfig(br, 0);
		try { br.close(); } catch (Exception localException1) { }
		try { in.close(); } catch (Exception localException2) { }
		try { fstream.close(); } catch (Exception localException3) { }
	}

	private void loadConfig(BufferedReader br, int i) {
		ArrayList<String> file = new ArrayList<String>();
		if (i > 2)
			return;
		try {
			String str = br.readLine();
			if (str.split(",").length == 3) {
				this.createSpawn(this.getLocationFromString(str));
			}
			if ((str != null) && (str.equals("--config--"))) {
				String strLine;
				while ((strLine = br.readLine()) != null) {
					file.add(strLine);
				}
				for (int ii = 0; ii < file.size(); ii++) {
					computeConfigData((String)file.get(ii));
				}
			}else{ 
				String temp = br.readLine();
				if (temp.split(",").length == 3) {
					this.createSpawn(this.getLocationFromString(temp));
				}
				loadConfig(br, i + 1);
			}
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
		if (this.arena.minPlayers == -1) {
			this.arena.minPlayers = (this.arena.maxPlayers - 40);
		}
		if (this.arena.maxPlayers >= Bukkit.getServer().getMaxPlayers())
			this.arena.maxPlayers = (Bukkit.getServer().getMaxPlayers() * 2);
	}

	private void computeConfigData(String str) {
		if (str.indexOf("=") > 0) {
			String str2 = str.substring(0, str.indexOf("="));
			if (str2.equalsIgnoreCase("type")) {
				String temp = str.substring(str.indexOf("=") + 1);
				if (temp.equals("tdm")) {
					this.arena.arenaType = ArenaType.TEAM_DEATHMATCH;
				}else if (temp.equals("ffa")) {
					this.arena.arenaType = ArenaType.FREE_FOR_ALL;
				}
				
				if (temp.length() > 1) {
					ArenaType temptype = null;
					try{ temptype = ArenaType.valueOf(temp); } catch(Exception e) { }
					if (temptype != null)
						this.arena.arenaType = temptype;
				}
			}
			if (str2.equalsIgnoreCase("mood"))
				this.arena.mood = str.substring(str.indexOf("=") + 1);
			if (str2.equalsIgnoreCase("creator"))
				this.arena.creator = str.substring(str.indexOf("=") + 1);
			if (str2.equalsIgnoreCase("realname")) {
				String tname = str.substring(str.indexOf("=") + 1);
				String startingLetter = tname.substring(0, 1);
				String everythingElse = tname.substring(1);
				this.arena.realName = startingLetter.toUpperCase() + everythingElse;
			}
			if (str2.equalsIgnoreCase("maxPlayers"))
				this.arena.maxPlayers = Integer.parseInt(str.substring(str.indexOf("=") + 1));
			if (str2.equalsIgnoreCase("minPlayers"))
				this.arena.minPlayers = Integer.parseInt(str.substring(str.indexOf("=") + 1));
			if (str2.equalsIgnoreCase("modifier")) {
				String check = str.substring(str.indexOf("=") + 1).toUpperCase();
				if (check.length() > 1) {
					ArenaModifier temp = null;
					try{ temp = ArenaModifier.valueOf(check); } catch(Exception e) { e.printStackTrace(); }
					if (temp != null)
						this.arena.arenaModifier = temp;
				}
			}
			if (str2.equalsIgnoreCase("addspawn")) {
				Location spawnloc = getLocationFromString(str.substring(str.indexOf("=") + 1));
				if (spawnloc != null) {
					createSpawn(spawnloc);
				}
			}
		}
	}

	private void createSpawn(Location spawnloc) {
		Team team = Team.BLUE;
		if (this.arena.spawns.size() == 1)
			team = Team.RED;
		//System.out.println("NEW SPAWN " + team);
		ArenaSpawn ks = new ArenaSpawn(spawnloc, team);
		this.arena.spawns.add(ks);		
	}

	private Location getLocationFromString(String str) {
		String[] arr = str.split(",");
		if (arr.length == 2)
			return new Location((World)Bukkit.getServer().getWorlds().get(0), Integer.parseInt(arr[0]), 0.0D, Integer.parseInt(arr[1]));
		if (arr.length == 3) {
			return new Location((World)Bukkit.getServer().getWorlds().get(0), Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
		}
		return null;
	}

	public ArrayList<GamePlayer> getPlayersOnTeam(Team team) {
		ArrayList<GamePlayer> ret = new ArrayList<GamePlayer>();
		ArrayList<GamePlayer> players = this.arena.getPlayers();
		
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (team == null || gplayer.getTeam().equals(team)) {
				ret.add(gplayer);
			}
		}
		return ret;
	}
}
