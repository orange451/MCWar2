package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.ArenaManager;
import com.orange451.mcwarfare.GunManager;
import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.killstreaks.MapItem;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.player.GameProfile;
import com.orange451.mcwarfare.player.sql.SQLController;
import com.orange451.mcwarfare.util.PacketUtils;
import com.orange451.mcwarfare.util.Util;
import com.orange451.pvpgunplus.PVPGunPlus;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Arena {
	private int gameTimer = 999;
	private int maxTime = 999;
	private int ticksAlive;
	private Arena toArena;
	private String name;
	private ArenaStats arenaStats;
	private SQLController sqlController;
	protected ArenaType arenaType = ArenaType.SPAWN;
	protected ArenaModifier arenaModifier = ArenaModifier.NONE;
	protected String mood = "day";
	protected Rectangle field;
	protected ArrayList<ArenaSpawn> spawns;
	private ArrayList<ArenaItem> arenaItems;
	//protected HashMap<String, GamePlayer> players;
	protected ArrayList<GamePlayer> players;
	protected int bluekills;
	protected int redkills;
	protected int bluescore;
	protected int redscore;
	protected String creator = "N/A";
	protected String realName;
	public boolean active = false;
	public int teamRedKills = 0;
	public int teamBlueKills = 0;
	public int maxPlayers = Bukkit.getMaxPlayers();
	public int minPlayers = 0;
	public ArrayList<VoteMap> voteMaps;

	public Arena(String name) {
		this.name = name;

		this.players = new ArrayList<GamePlayer>();
		this.spawns = new ArrayList();
		this.arenaStats = new ArenaStats(this);
		this.voteMaps = new ArrayList();
		this.arenaItems = new ArrayList();

		this.arenaStats.loadArena();
	}

	public void tick() {
		//MAP VOTING STUFF
		if ((this.arenaType.equals(ArenaType.SPAWN)) && (players.size() >= 2)) {
			//Used to pick the next map and stuff
			doLobbyLogic();
		}

		//ACTUAL ARENA STUFF
		if (!this.active)
			return;
		
		if (this.gameTimer > 0)
			this.gameTimer -= 1;
		
		if ((this.arenaType.equals(ArenaType.SPAWN)) && (this.arenaStats.getAmountPlayersOnTeam(Team.BLUE) <= 1)) {
			this.gameTimer += 1;
		}
		this.ticksAlive += 1;
		
		//Remove all items already on the ground
		if (this.ticksAlive == 3) {
			for (int i = players.size() - 1; i >= 0; i--) {
				GamePlayer player = players.get(i);
				List<Entity> entities = player.getPlayer().getNearbyEntities(64, 64, 64);
				for (int ii = entities.size() - 1; ii >= 0; ii--) {
					Entity e = entities.get(ii);
					if (e instanceof Item) {
						e.remove();
					}
				}
			}
		}

		//Tick the spawns
		for (int i = 0; i < this.spawns.size(); i++) {
			((ArenaSpawn)this.spawns.get(i)).tick();
		}

		//play the match start music
		if ((this.ticksAlive == 2) && (!this.arenaType.equals(ArenaType.SPAWN))) {
			for (int i = players.size() - 1; i >= 0; i--) {
				GamePlayer gplayer = (GamePlayer)players.get(i);
				gplayer.getPlayer().playSound(gplayer.getPlayer().getLocation(), Sound.BLAZE_DEATH, 6.0F, 1.0F);
			}
		}

		//set the arenas weather mood
		((World)Bukkit.getWorlds().get(0)).setStorm(false);
		if (this.mood.equals("day")) {
			((World)Bukkit.getWorlds().get(0)).setTime(4000L);
		} else if (this.mood.equals("night")) {
			((World)Bukkit.getWorlds().get(0)).setTime(12800L);
		} else if (this.mood.equals("rain")) {
			((World)Bukkit.getWorlds().get(0)).setTime(4000L);
			((World)Bukkit.getWorlds().get(0)).setStorm(true);
		}

		//Tick all the map items
		for (int i = getArenaItems().size() - 1; i >= 0; i--) {
			try{
				((ArenaItem)getArenaItems().get(i)).tick();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		//Tick all of the players
		for (int i = players.size() - 1; i >= 0; i--) {
			try{
				GamePlayer gplayer = (GamePlayer)players.get(i);
				gplayer.tick();
				
				//Remove GUI at top of screen
				if (!this.arenaType.equals(ArenaType.SPAWN))
					PacketUtils.sendPacket(gplayer.getPlayer(), PacketUtils.getDestroyEntityPacket());
	
				//Play coutdown sound
				if (this.gameTimer <= 10)
					gplayer.getPlayer().playEffect(gplayer.getPlayer().getLocation(), Effect.CLICK1, 0);
			}catch(Exception e) {
				//
			}
		}

		checkTeams();

		//Check if the game has run out of time
		if (this.gameTimer == 0)
			endGame(EndGameReason.OUT_OF_TIME);
	}

	public void checkTeams() {
		if (this.arenaType.equals(ArenaType.SPAWN) || this.ticksAlive < 8)
			return;
		
		int amtBlue = this.arenaStats.getAmountPlayersOnTeam(Team.BLUE);
		int amtRed = this.arenaStats.getAmountPlayersOnTeam(Team.RED);

		if (this.arenaType.equals(ArenaType.FREE_FOR_ALL)) {
			if (amtBlue <= 1) {
				this.endGame(EndGameReason.PLAYER_WIN);
			}			
		}else{
			if (amtBlue == 0 || amtRed == 0) {
				this.endGame(EndGameReason.TEAM_WIN);
			}
		}
	}

	public void endGame(EndGameReason reason) {
		this.active = false;
		for (int i = 0; i < this.getArenaItems().size(); i++)
			this.getArenaItems().get(i).remove();
		this.getArenaItems().clear();

		if (this.arenaType.equals(ArenaType.SPAWN)) { //Send players off to their arena
			this.sendPlayersToArena(toArena, "");
			return;
		}

		String message = "game over!";

		if (reason.equals(EndGameReason.OUT_OF_TIME)) {
			if (this.arenaType.equals(ArenaType.TEAM_DEATHMATCH)) {
				Team winningTeam = getWinningTeam();
				message = "Stalemate!";
				if (winningTeam != null) {
					message = getTeamColor(winningTeam) + winningTeam.toString().toLowerCase() + ChatColor.WHITE + " team has won!";

					for (int i = players.size() - 1; i >= 0; i--) {
						GamePlayer gplayer = (GamePlayer)players.get(i);
						if (gplayer.getTeam().equals(winningTeam)) {
							gplayer.getProfile().addCredits(MCWarfare.getPlugin().getGunManager().getCreditserKill(gplayer) * 10);
						}
					}
				}
			}else if (this.arenaType.equals(ArenaType.FREE_FOR_ALL)) {
				GamePlayer mostKills = this.arenaStats.getPlayerWithMostKills();
				if (mostKills != null) {
					message = mostKills.getTag() + ChatColor.WHITE + " has won!";
					mostKills.getProfile().addCredits(MCWarfare.getPlugin().getGunManager().getCreditserKill(mostKills) * 15);
				}
			}
		}else if (reason.equals(EndGameReason.PLAYER_WIN)) {
			GamePlayer mostKills = this.arenaStats.getPlayerWithMostKills();
			if (mostKills != null) {
				message = mostKills.getTag() + ChatColor.WHITE + " has won!";
				mostKills.getProfile().addCredits(MCWarfare.getPlugin().getGunManager().getCreditserKill(mostKills) * 15);
			}
		}else if (reason.equals(EndGameReason.TEAM_WIN)) {
			int amtBlue = this.arenaStats.getAmountPlayersOnTeam(Team.BLUE);
			int amtRed = this.arenaStats.getAmountPlayersOnTeam(Team.RED);
			if (amtBlue == 0) { //Award red team
				message = getTeamColor(Team.RED) + Team.RED.toString().toLowerCase() + ChatColor.WHITE + " team has won!";
				awardTeam(Team.RED, 10 - ((this.arenaModifier.equals(ArenaModifier.INFECTION)?1:0) * 5)); //In infection, zombies get less credits if they win
			}else if (amtRed == 0) { //Award blue team
				message = getTeamColor(Team.BLUE) + Team.BLUE.toString().toLowerCase() + ChatColor.WHITE + " team has won!";
				awardTeam(Team.BLUE, 10 + ((this.arenaModifier.equals(ArenaModifier.INFECTION)?1:0) * 5)); //In infection, survivors get more credits if they win
			}
		}
		
		for (int i = 0; i < MCWarfare.getPlugin().glassThinReplace.size(); i++)
			MCWarfare.getPlugin().glassThinReplace.get(i).getWorld().getBlockAt(MCWarfare.getPlugin().glassThinReplace.get(i)).setType(Material.THIN_GLASS);
		MCWarfare.getPlugin().glassThinReplace.clear();
		
		for (int i = 0; i < MCWarfare.getPlugin().mapItems.size(); i++)
			MCWarfare.getPlugin().mapItems.get(i).remove();
		
		this.sendPlayersToArena(toArena, message);
		this.resetTimer();
	}

	public void awardTeam(Team team, int creditMultiplier) {
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (gplayer.getTeam().equals(team)) {
				gplayer.getProfile().addCredits(MCWarfare.getPlugin().getGunManager().getCreditserKill(gplayer) * creditMultiplier);
			}
		}		
	}

	public void onStart() {
		//Called when an arena first starts
		this.resetTimer();
		this.active = true;
		
		if (this.sqlController != null)
			this.sqlController.stopSaving();
		this.sqlController = new SQLController();
		new Thread(this.sqlController).start();

		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			this.arenaStats.getSpawn(gplayer).spawn(gplayer.getPlayer());
		}

		if (this.arenaType.equals(ArenaType.SPAWN)) {
			this.toArena = null;
			this.voteMaps.clear();
			List<Arena> arenas = MCWarfare.getPlugin().getArenaManager().getRandomArenas(3);
			for (int i = 0; i < arenas.size(); i++) {
				this.voteMaps.add(new VoteMap(arenas.get(i).realName));
			}
			displayVoteMaps(null);
			if (Bukkit.getPluginManager().getPlugin("PVPGunPlus") != null)
				PVPGunPlus.getPlugin().reload(true);
			MCWarfare.getPlugin().loadAllClans();
		}else{
			this.toArena = MCWarfare.getPlugin().getArenaManager().getFirstArenaByType(ArenaType.SPAWN);
			this.balanceTeams();
		}

		this.redkills = 0;
		this.redscore = 0;
		this.bluekills = 0;
		this.bluescore = 0;

		if (this.arenaModifier.equals(ArenaModifier.CAPTURE_THE_FLAG)) {
			ArenaSpawn temp1 = spawns.get(spawns.size() - 2); //blue
			ArenaSpawn temp2 = spawns.get(spawns.size() - 1); //red

			arenaItems.add(new ArenaItemFlagStand(this, temp1.getLocation(), Team.BLUE));
			arenaItems.add(new ArenaItemFlagStand(this, temp2.getLocation(), Team.RED));
		}else if (this.arenaModifier.equals(ArenaModifier.GOLDEN_GUN)) {
			this.arenaItems.add(new ArenaItemGoldGun(this));
		}else if (this.arenaModifier.equals(ArenaModifier.DOMINATION)) {
			ArenaSpawn temp1 = spawns.get(spawns.size() - 3); //flag1
			ArenaSpawn temp2 = spawns.get(spawns.size() - 2); //flag2
			ArenaSpawn temp3 = spawns.get(spawns.size() - 1); //flag3

			arenaItems.add(new ArenaItemDominationFlag(this, temp1.getLocation(), "A"));
			arenaItems.add(new ArenaItemDominationFlag(this, temp2.getLocation(), "B"));
			arenaItems.add(new ArenaItemDominationFlag(this, temp3.getLocation(), "C"));
		}

		//Fix for FFA spawning (at the start of a game)
		if (this.arenaType.equals(ArenaType.FREE_FOR_ALL)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
				public void run() {
					for (int i = players.size() - 1; i >= 0; i--) {
						GamePlayer gplayer = (GamePlayer)players.get(i);
						ArenaSpawn spawn = arenaStats.getSpawnAtRoundStart(gplayer);
						if (spawn != null) {
							spawn.spawn(gplayer.getPlayer());
							gplayer.hasSpawnProtection = true;
							gplayer.spawnLocation = spawn.getLocation();
							gplayer.setInvincibleTicks(5);
							gplayer.forceRespawn();
						}
					}

				}
			}, 4L);
		}

		for (int i = 0; i < this.arenaItems.size(); i++) {
			this.arenaItems.get(i).start();
		}
		
		if (this.arenaModifier.equals(ArenaModifier.INFECTION)) {
			GamePlayer random = players.get(MCWarfare.getPlugin().getRandom().nextInt(players.size()));
			random.setTeam(Team.RED);
			random.forceRespawn();
			random.getPlayer().sendMessage(ChatColor.RED + "You are the root zombie!");
		}
		
		//In case anyone is moving before a match starts
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			gplayer.getPlayer().setVelocity(new Vector(0, 0, 0));
		}
	}

	public void onJoin(GamePlayer player) {
		this.players.add(player);
		if (this.arenaType.equals(ArenaType.SPAWN)) {
			player.boughtItems.clear();
			player.tags.clear();
			player.hasVoted = false;
			if (this.voteMaps.size() > 0) {
				displayVoteMaps(player);
			}
			sendCurrentMapChoice(player);
			player.forceRespawn();
		}
		
		player.setTeam(chooseTeam(player));		
		if ((this.arenaModifier.equals(ArenaModifier.INFECTION)) && (this.ticksAlive >= 10))
			player.setTeam(Team.RED);
		player.setArena(this);
		
		System.out.println("Joining " + player.getPlayer().getName() + " to " + this.getName() + " on team " + player.getTeam().toString());

		ArenaSpawn spawn = player.getArena().arenaStats.getSpawn(player);
		if (spawn != null)
			spawn.spawn(player.getPlayer());
	}

	public void onLeave(GamePlayer player) {
		for (int i = players.size() - 1; i >= 0; i--) {
			if (players.get(i).getPlayer().getName().equalsIgnoreCase(player.getPlayer().getName())) {
				this.players.remove(i);
			}
		}
	}

	public void onDamage(EntityDamageByEntityEvent event, GamePlayer attacker, GamePlayer defender) {
		int originalDamage = (int) event.getDamage();
		if (defender.dead || defender.getPlayer().getHealth() <= 0) {
			return;
		}

		defender.onDamagedByEvent(event);
		attacker.onAttack(event);

		boolean usingSword = false;
		ItemStack item = attacker.getPlayer().getItemInHand();
		if (item != null) {
			if (item.getType().toString().toLowerCase().contains("sword")) {
				usingSword = true;
				event.setDamage(9);
				if (item.getType().toString().toLowerCase().contains("diamond"))
					event.setDamage(event.getDamage() * 2);
			}
		}

		if (getArenaModifier().equals(ArenaModifier.INFECTION)) {
			if (attacker.getTeam().equals(Team.RED)) {
				if (event.getDamage() > 0) {
					if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
						if ((attacker.getPlayer().getHealth() > 0) && (!attacker.getPlayer().isDead())) {
							double distance = attacker.getPlayer().getLocation().distance(defender.getPlayer().getLocation());
							event.setDamage(event.getDamage() * 28);
						}
					} else if (event.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM)) {
						event.setCancelled(true);
					}
				}
			} else if (!defender.hasTag("rootZombie"))
				event.setDamage((int)(event.getDamage() * 2.5D));
			else {
				event.setDamage((int)(event.getDamage() / 1.5D));
			}
		}

		if (!event.isCancelled()) {
			defender.lastDamager = attacker;
			Util.playEffect(Effect.STEP_SOUND, defender.getPlayer().getLocation(), Material.REDSTONE_BLOCK.getId());
			Util.playEffect(Effect.STEP_SOUND, defender.getPlayer().getLocation().add(0.0D, 1.0D, 0.0D), Material.REDSTONE_BLOCK.getId());
			attacker.getPlayer().playSound(attacker.getPlayer().getLocation(), Sound.ARROW_HIT, 2, 2);

			if (getArenaModifier().equals(ArenaModifier.ONE_IN_THE_CHAMBER) && !usingSword) {
				event.setDamage(99999);
				defender.getPlayer().setHealth(0);
			}
		}
	}

	public void onDeath(GamePlayer killed) {
		for (int i = 0; i < this.arenaItems.size(); i++) {
			((ArenaItem)this.arenaItems.get(i)).onDeath(killed);
		}
		if ((killed.lastDamager != null) && (!killed.dead)) {
			killed.lastDamager.onKill(killed);

			String ASCII = "ï¸»â•¦â•¤â”€";
			if (killed.lastDamager.getPlayer() != null) {
				ItemStack item = killed.lastDamager.getPlayer().getItemInHand();
				if (item != null) {
					KitGun gun = MCWarfare.getPlugin().getGunManager().getGun(item);
					if (gun != null) {
						if (gun.gunClass.equalsIgnoreCase("sniper"))
							ASCII = "ï¸»â”³ãƒ‡â•�â€”";
						if (gun.gunClass.equalsIgnoreCase("smg"))
							ASCII = "â•¦â•¤â”€";
						if (gun.gunClass.equalsIgnoreCase("shotgun"))
							ASCII = "â•¦â•�ï¸»â•�";
						if (gun.gunClass.equalsIgnoreCase("pistol"))
							ASCII = "â•¦â”€";
					}
					else if (item.getType().toString().toLowerCase().contains("sword")) {
						ASCII = "oxx[=====>";
					}
				}
			}
			String killfeed = ChatColor.GRAY + killed.lastDamager.getTag() + ChatColor.WHITE + " " + ASCII + " " + ChatColor.GRAY + killed.getTag();
			//Replaced broadcastMessage with this (so I could check for killfeed setting)
			for (int i = players.size() - 1; i >= 0; i--) {
				GamePlayer gplayer = (GamePlayer)players.get(i);
				if (gplayer.disableChat < 0 && (gplayer.hasKillfeedEnabled || killed.lastDamager.equals(gplayer)))
					gplayer.getPlayer().sendMessage(killfeed);
			}

			//Create a dogtag (if on killconfirmed)
			if (this.arenaModifier.equals(ArenaModifier.KILL_CONFIRMED)) {
				this.arenaItems.add(new ArenaItemDogTag(this, killed.getPlayer().getLocation(), killed));
			}

			//Handle logic for one in the chamber
			if ((this.arenaType.equals(ArenaType.FREE_FOR_ALL)) && (this.arenaModifier.equals(ArenaModifier.ONE_IN_THE_CHAMBER))) {
				final Player player = killed.getPlayer();
				MCWarfare.getPlugin().getArenaManager().leave(player);
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
					public void run() {
						MCWarfare.getPlugin().getArenaManager().join(player);
						player.sendMessage("You are out!");
					}
				}
				, 10L);
			}
		}

		killed.onDeath();
	}
	
	public Team chooseTeam(GamePlayer gplayer) {
		if (this.arenaType.equals(ArenaType.TEAM_DEATHMATCH)) {
			int amtBlue = this.arenaStats.getAmountPlayersOnTeam(Team.BLUE);
			int amtRed = this.arenaStats.getAmountPlayersOnTeam(Team.RED);
			if (amtBlue > amtRed)
				return Team.RED;
			return Team.BLUE;
		}
		return Team.BLUE;
	}

	private Team getWinningTeam() {
		if (this.arenaModifier.equals(ArenaModifier.INFECTION)) {
			int survivors = this.getArenaStats().getAmountPlayersOnTeam(Team.BLUE);
			if (survivors > 0)
				return Team.BLUE;
			return Team.RED;
		}
		if (this.arenaModifier.equals(ArenaModifier.CAPTURE_THE_FLAG) || this.arenaModifier.equals(ArenaModifier.KILL_CONFIRMED) || this.arenaModifier.equals(ArenaModifier.DOMINATION)) {
			//Get the winning team from a Team score game
			Team winningTeam = null;
			if (this.redscore > this.bluescore)
				winningTeam = Team.RED;
			else if (this.bluescore > this.redscore)
				winningTeam = Team.BLUE;

			return winningTeam;
		}else{
			//Get the winning team from a regular Team kill based gamemode
			Team winningTeam = null;
			if (this.redkills > this.bluekills)
				winningTeam = Team.RED;
			else if (this.bluekills > this.redkills)
				winningTeam = Team.BLUE;

			return winningTeam;
		}
	}

	private void sendCurrentMapChoice(final GamePlayer player)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable()
		{
			public void run() {
				String nextmap = ChatColor.LIGHT_PURPLE + "Picking map... Please wait";
				if (Arena.this.toArena != null) {
					if (Arena.this.toArena.active)
						nextmap = ChatColor.LIGHT_PURPLE + "Current map: " + ChatColor.RED + Arena.this.toArena.getRealName() + ChatColor.LIGHT_PURPLE + " by: " + ChatColor.AQUA + Arena.this.toArena.creator + ChatColor.GREEN + "  -Please wait";
					else {
						nextmap = ChatColor.LIGHT_PURPLE + "Next map: " + ChatColor.RED + Arena.this.toArena.getRealName() + ChatColor.LIGHT_PURPLE + " by: " + ChatColor.AQUA + Arena.this.toArena.creator;
					}
				}
				PacketUtils.sendPacket(player.getPlayer(), PacketUtils.getDestroyEntityPacket());
				PacketUtils.displayTextBar(nextmap, player.getPlayer(), 4500);
			}
		}
		, 20L);
	}
	
	public String getGameModeAsString() {
		if (this.arenaModifier.equals(ArenaModifier.NONE))
			return this.arenaType.toString().toLowerCase().replace("_", " ");
		return this.arenaModifier.toString().toLowerCase().replace("_", " ");
	}
	
	public String getCreator() {
		return this.creator;
	}

	public ChatColor getTeamColor(Team team) {
		if (this.arenaType.equals(ArenaType.FREE_FOR_ALL))
			return ChatColor.LIGHT_PURPLE;
		if (team.equals(Team.BLUE))
			return ChatColor.BLUE;
		if (team.equals(Team.RED))
			return ChatColor.RED;
		return ChatColor.WHITE;
	}

	@SuppressWarnings("deprecation")
	public void sendPlayersToArena(final Arena to, final String message) {
		if (to == null)
			return;
		MCWarfare.getPlugin().getSQLController().doSave = false;
		final SQLController currentStats = new SQLController();

		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);

			gplayer.lastDamager = null;
			gplayer.getPlayer().setLastDamageCause(null);
			gplayer.getPlayer().setHealth(0.0D);
			gplayer.getPlayer().damage(200.0D);

			if (to.getArenaType().equals(ArenaType.SPAWN)) {
				gplayer.getProfile().save(false, currentStats);
				//gplayer.getProfile().reload();
				for (int ii = 0; ii < MCWarfare.getPlugin().mapItems.size(); ii++) {
					MCWarfare.getPlugin().removeMapEntity((MapItem)MCWarfare.getPlugin().mapItems.get(ii));
				}
				sendCurrentMapChoice(gplayer);
			}
				to.onJoin(gplayer);

			if ((message != null) && (message.length() > 0)) {
				for (int ii = 0; ii < 20; ii++)
					gplayer.getPlayer().sendMessage("");
				gplayer.getPlayer().sendMessage("------------------------------");
				gplayer.getPlayer().sendMessage("| " + message);
				gplayer.getPlayer().sendMessage("------------------------------");
			}
			
		}
		
		this.players.clear();
		to.onStart();
		//THE BELOW CODE IS UGLY, AND WILL BE CHANGED

		if (to.getArenaType().equals(ArenaType.SPAWN)) {
			Runnable r = new Runnable() {
				public void run() {
					Location loc = ((ArenaSpawn)to.spawns.get(0)).getLocation().clone().add(0.0D, 4.0D, 0.0D);
					Color c = Color.RED;
					if (message.toLowerCase().contains("blue"))
						c = Color.BLUE;
					if (message.toLowerCase().contains("stalemate"))
						c = Color.WHITE;
					if (arenaType.equals(ArenaType.FREE_FOR_ALL))
						c = Color.PURPLE;
					
					FireworkExplosion firework = new FireworkExplosion(loc);
					firework.explode(c);
				}
			};
			for (int i = 1; i < 12; i++) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), r, 20 * i);
			}
			
			//Arena's specific profile saving stuff
			this.saveStats();
			
			//Start profile saving
			Bukkit.getScheduler().scheduleAsyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
				public void run() {
					System.out.println("STARTING TO SAVE PROFILES");
					MCWarfare.getPlugin().getSQLController().doSave = true;
					currentStats.doSave = true;
				}
			}, 20);
			
			//Stop profile saving
			Bukkit.getScheduler().scheduleAsyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
				public void run() {
					MCWarfare.getPlugin().getSQLController().doSave = false;
					currentStats.doSave = false;
					currentStats.stopSaving();
				}
			}, 40);
		}
	}

	private void displayVoteMaps(GamePlayer player) {
		if (player != null) {
			player.getPlayer().sendMessage("------------------------------");
			player.getPlayer().sendMessage("| " + ChatColor.DARK_GRAY + "Please vote for a map! " + ChatColor.AQUA + "/vote #");
			for (int i = 0; i < this.voteMaps.size(); i++) {
				player.getPlayer().sendMessage("| " + Integer.toString(i + 1) + ": " + ChatColor.GRAY + ((VoteMap)this.voteMaps.get(i)).getArenaName());
			}
			player.getPlayer().sendMessage("------------------------------");
		} else {
			broadcastMessage("------------------------------");
			broadcastMessage("| " + ChatColor.DARK_GRAY + "Please vote for a map! " + ChatColor.AQUA + "/vote #");
			for (int i = 0; i < this.voteMaps.size(); i++) {
				broadcastMessage("| " + Integer.toString(i + 1) + ": " + ChatColor.GRAY + ((VoteMap)this.voteMaps.get(i)).getArenaName());
			}
			broadcastMessage("------------------------------");
		}
	}

	public boolean canPlayerJoin(GamePlayer player) {
		if (this.ticksAlive < 10)
			return true;
		if (this.arenaModifier == null || this.arenaModifier.equals(ArenaModifier.NONE))
			return true;

		if (this.arenaModifier.equals(ArenaModifier.CAPTURE_THE_FLAG) 
				|| this.arenaModifier.equals(ArenaModifier.DOMINATION)
				|| this.arenaModifier.equals(ArenaModifier.GOLDEN_GUN)
				|| this.arenaModifier.equals(ArenaModifier.GUN_GAME)
				|| this.arenaModifier.equals(ArenaModifier.INFECTION)
				|| this.arenaModifier.equals(ArenaModifier.KILL_CONFIRMED))
			return true;

		return false;
	}

	public String getName() {
		return this.name;
	}

	private void balanceTeams() {
		if (this.arenaType.equals(ArenaType.FREE_FOR_ALL))
			return;
		if (this.arenaModifier.equals(ArenaModifier.INFECTION))
			return;
		
		int amtred = arenaStats.getAmountPlayersOnTeam(Team.RED);
		int amtblue = arenaStats.getAmountPlayersOnTeam(Team.BLUE);
		if (amtred >= amtblue + 1)
			balance(Team.RED, Team.BLUE, amtred - amtblue);
		if (amtblue >= amtred + 1)
			balance(Team.BLUE, Team.RED, amtblue - amtred);
	}

	private void balance(Team from, Team to, double amtPlayers) {
		amtPlayers -= 1;
		amtPlayers /= 2d;
		amtPlayers = (int)Math.ceil(amtPlayers);
		int switched = 0;

		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (gplayer.getTeam().equals(from) && (switched < amtPlayers)) {
				gplayer.setTeam(to);
				switched++;
			}
		}
		/*
		if (switched < amtPlayers) {
			for (Entry<String, GamePlayer> pairs : players.entrySet()) {
				GamePlayer gplayer = (GamePlayer)pairs.getValue();
				if (gplayer.getTeam().equals(from) && (switched < amtPlayers)) {
					gplayer.setTeam(to);
					switched++;
				}
			}
		}*/
	}
	
	private void doLobbyLogic() {
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);

			if (this.toArena != null && gplayer != null) {
				gplayer.getPlayer().setLevel(this.toArena.getTimeRemaining());
				gplayer.tick();
			}
		}
		if (this.gameTimer == 25) {
			MCWarfare.getPlugin().storeVotes(this.voteMaps);

			VoteMap popular = null;
			for (int i = 0; i < this.voteMaps.size(); i++) {
				VoteMap map = (VoteMap)this.voteMaps.get(i);
				if ((popular == null) || (map.getVotes() > popular.getVotes())) {
					popular = map;
				}
			}

			this.toArena = MCWarfare.getPlugin().getArenaManager().getRandomArenaExcluding(this);
			if (popular != null) {
				Arena temp = MCWarfare.getPlugin().getArenaManager().getRandomArenaByName(popular.getArenaName());
				if (temp != null) {
					this.toArena = MCWarfare.getPlugin().getArenaManager().getRandomArenaByName(popular.getArenaName());
				}
			}

			broadcastMessage("------" + ChatColor.GRAY + "[Vote Results]" + ChatColor.WHITE + "------");
			broadcastMessage("| Map: " + ChatColor.RED + this.toArena.getRealName());
			broadcastMessage("| GameMode: " + ChatColor.RED + this.toArena.getGameModeAsString());
			broadcastMessage("| Creator: " + ChatColor.GRAY + this.toArena.getCreator());
			broadcastMessage("------------------------");
			
			this.voteMaps.clear();

			for (int i = players.size() - 1; i >= 0; i--) {
				GamePlayer gplayer = (GamePlayer)players.get(i);
				PacketUtils.sendPacket(gplayer.getPlayer(), PacketUtils.getDestroyEntityPacket());

				sendCurrentMapChoice(gplayer);
			}
		}
	}

	public GamePlayer getPlayer(String name) {
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (gplayer.getPlayer().getName().equalsIgnoreCase(name)) {
				return gplayer;
			}
		}
		return null;
	}

	public GamePlayer getPlayer(Player player) {
		return getPlayer(player.getName());
	}

	public void resetTimer() {
		this.maxTime = 60 * 4;
		if (this.arenaType.equals(ArenaType.SPAWN))
			this.maxTime = 60;
		
		if (this.arenaType.equals(ArenaType.FREE_FOR_ALL))
			this.maxTime = (int) (this.maxTime/2d);
		
		this.gameTimer = this.maxTime;

		this.ticksAlive = 0;
	}

	public ArenaType getArenaType() {
		return this.arenaType;
	}

	public ArenaModifier getArenaModifier() {
		return this.arenaModifier;
	}

	public boolean isInside(Location location) {
		int x = location.getBlockX();
		int y = location.getBlockZ();

		return this.field.contains(x, y);
	}

	public ArenaStats getArenaStats() {
		return this.arenaStats;
	}

	public void broadcastMessage(String string) {
		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			if (gplayer.disableChat < 0) {
				if (gplayer.getPlayer() != null)
					gplayer.getPlayer().sendMessage(string);
			}
		}
	}
	
	public int getTimeRemaining() {
		return this.gameTimer;
	}

	public void stop() {
		for (int i = 0; i < getArenaItems().size(); i++)
			((ArenaItem)getArenaItems().get(i)).remove();
		getArenaItems().clear();

		for (int i = players.size() - 1; i >= 0; i--) {
			GamePlayer gplayer = (GamePlayer)players.get(i);
			gplayer.getProfile().save(false, MCWarfare.getPlugin().getSQLController());
		}
		this.players.clear();
		
		if (this.sqlController != null)
			this.sqlController.stopSaving();
	}

	public int getTicksSinceStart() {
		return this.ticksAlive;
	}

	public String getRealName() {
		return this.realName;
	}

	public void doVote(int vote) {
		if (vote < this.voteMaps.size())
			((VoteMap)this.voteMaps.get(vote)).vote();
	}

	public ArrayList<ArenaItem> getArenaItems()
	{
		return this.arenaItems;
	}

	public void removeArenaItem(ArenaItem item) {
		this.arenaItems.remove(item);
	}

	public Arena getToArena() {
		return this.toArena;
	}

	public void FORCECLOSE() {
		this.players.clear();
	}

	public ArrayList<GamePlayer> getPlayers() {
		return this.players;
	}

	public SQLController getSQLController() {
		return this.sqlController;
	}

	public void saveStats() {
		sqlController.doSave = true;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
			public void run() {
				sqlController.doSave = false;
			}
		}, 20);
		
		System.out.println(this.sqlController.doSave);
	}
}