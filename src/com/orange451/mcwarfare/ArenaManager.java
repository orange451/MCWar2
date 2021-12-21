package com.orange451.mcwarfare;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.orange451.mcwarfare.arena.Arena;
import com.orange451.mcwarfare.arena.ArenaCreator;
import com.orange451.mcwarfare.arena.ArenaType;
import com.orange451.mcwarfare.player.GamePlayer;

public class ArenaManager {

	private ArrayList<ArenaCreator> arenaCreators = new ArrayList<ArenaCreator>();
	
	public void loadArenas() {
		MCWarfare.getPlugin().arenas.clear();
		System.out.println("LOADING ARENAS");
		
		String path = MCWarfare.getPlugin().getPluginFolder() + "/arenas";
		File dir = new File(path);
		String[] children = dir.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				this.loadArena(filename);
			}
		}
		
		System.out.println("ALL ARENAS LOADED, STARTING SPAWN");
		Arena arena = this.getFirstArenaByType(ArenaType.SPAWN);
		if (arena != null) {
			arena.onStart();
		}
		System.out.println("SPAWN STARTED");
	}
	
	public Arena getFirstArenaByType(ArenaType spawn) {
		for (int i = MCWarfare.getPlugin().arenas.size() - 1; i >= 0; i--) {
			if (MCWarfare.getPlugin().arenas.get(i).getArenaType().equals(spawn))
				return MCWarfare.getPlugin().arenas.get(i);
		}
		return null;
	}

	public Arena getRandomArenaExcluding(Arena excluding) {
		Arena ret = null;
		ArrayList<Arena> possible = new ArrayList<Arena>();
		int players = excluding.getPlayers().size();
		for (int i = MCWarfare.getPlugin().arenas.size() - 1; i >= 0; i--) {
			if (!MCWarfare.getPlugin().arenas.get(i).getArenaType().equals(excluding.getArenaType())) {
				if (players >= MCWarfare.getPlugin().arenas.get(i).minPlayers && players <= MCWarfare.getPlugin().arenas.get(i).maxPlayers) {
					possible.add(MCWarfare.getPlugin().arenas.get(i));
				}
			}
		}
		
		if (possible.size() > 0) {
			ret = possible.get(MCWarfare.getPlugin().getRandom().nextInt(possible.size()));
		}
		
		return ret;
	}
	
	public List<Arena> getRandomArenas(int amount) {
		List<Arena> ret = new ArrayList<Arena>();
		if (MCWarfare.getPlugin().arenas.size() - 1 >= amount) {
			while (ret.size() < amount) {
				Arena arena = MCWarfare.getPlugin().arenas.get(MCWarfare.getPlugin().getRandom().nextInt(MCWarfare.getPlugin().arenas.size()));
				boolean inside = false;
				if (arena.getArenaType().equals(ArenaType.SPAWN))
					inside = true;
				for (int i = 0; i < ret.size(); i++) {
					Arena check = ret.get(i);
					if (arena.getRealName().equalsIgnoreCase(check.getRealName())) {
						inside = true;
					}
				}
				if (!inside) {
					ret.add(arena);
				}
			}
		}else{
			System.out.println("NOT ENOUGH ARENAS TO PLAY");
		}
		return ret;
	}
	
	public Arena getRandomArenaIncludingType(ArenaType including) {
		Arena ret = null;
		ArrayList<Arena> possible = new ArrayList<Arena>();
		for (int i = MCWarfare.getPlugin().arenas.size() - 1; i >= 0; i--) {
			if (MCWarfare.getPlugin().arenas.get(i).getArenaType().equals(including)) {
				possible.add(MCWarfare.getPlugin().arenas.get(i));
			}
		}
		
		if (possible.size() > 0) {
			ret = possible.get(MCWarfare.getPlugin().getRandom().nextInt(possible.size()));
		}
		
		return ret;
	}
	
	public Arena getRandomArenaByName(String arenaName) {
		Arena ret = null;
		ArrayList<Arena> possible = new ArrayList<Arena>();
		for (int i = MCWarfare.getPlugin().arenas.size() - 1; i >= 0; i--) {
			if (MCWarfare.getPlugin().arenas.get(i).getName().toLowerCase().contains(arenaName.toLowerCase())) {
				possible.add(MCWarfare.getPlugin().arenas.get(i));
			}
		}
		
		if (possible.size() > 0) {
			ret = possible.get(MCWarfare.getPlugin().getRandom().nextInt(possible.size()));
		}
		
		return ret;
	}
	
	public GamePlayer getGamePlayer(String name) {
		for (int i = MCWarfare.getPlugin().arenas.size() - 1; i >= 0; i--) {
			GamePlayer gamePlayer = MCWarfare.getPlugin().arenas.get(i).getPlayer(name);
			if (gamePlayer != null) {
				return gamePlayer;
			}
		}
		return null;
	}

	public void join(Player player) {
		if (!MCWarfare.getPlugin().connected) {
			player.kickPlayer(ChatColor.BLUE + "Server could not establish database connection! \n\n"
					+ ChatColor.GRAY + "Please try again later");
			return;
		}
		Arena arena = this.getFirstArenaByType(ArenaType.SPAWN);
		/*int amtPlayers = Bukkit.getOnlinePlayers().length;
		if (amtPlayers > 8)
			arena = this.getRandomArenaIncludingType(ArenaType.Spawn);*/
		
		if (arena != null) {
			GamePlayer gamePlayer = new GamePlayer(arena, player);
			arena.onJoin(gamePlayer);
		}else{
			player.kickPlayer(ChatColor.BLUE + "This server contains no arena data! \n\n"
					+ ChatColor.GRAY + "Please Visit\n"
					+ ChatColor.RED+ ChatColor.UNDERLINE + "http://www.multiplayerservers.com/\n"
					+ ChatColor.RESET + ChatColor.GRAY + "and report this!");
		}
	}
	
	public void leave(Player player) {
		GamePlayer gamePlayer = this.getGamePlayer(player.getName());
		if (gamePlayer != null) {
			gamePlayer.disconnect();
		}
	}

	public void loadArena(String arenaName) {
		String path = MCWarfare.getPlugin().getPluginFolder() + "/arenas";
		File f = new File(path + "/" + arenaName);
		if (f.exists()) {
			Arena ka = new Arena(arenaName);
			MCWarfare.getPlugin().arenas.add(ka);
			System.out.println("Loaded arena: " + arenaName + " (" + ka.getArenaType() + "/" + ka.getArenaModifier() + ")");
		}
	}


	public void stopMakingArena(Player player) {
		for (int i = 0; i < arenaCreators.size(); i++) {
			if (arenaCreators.get(i).player.getName().equals(player.getName())) {
				arenaCreators.remove(i);
			}
		}
	}
	
	public ArenaCreator getArenaMaker(Player player) {
		for (int i = 0; i < arenaCreators.size(); i++) {
			if (arenaCreators.get(i).player.getName().equals(player.getName())) {
				return arenaCreators.get(i);
			}
		}
		return null;
	}

	public void startMakingArena(ArenaCreator creator) {
		arenaCreators.add(creator);
	}
}
