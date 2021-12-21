package com.orange451.mcwarfare;

import java.io.BufferedReader;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.orange451.mcwarfare.arena.Arena;
import com.orange451.mcwarfare.arena.ArenaType;
import com.orange451.mcwarfare.arena.BuyableItem;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.arena.VoteMap;
import com.orange451.mcwarfare.arena.killstreaks.AttackDog;
import com.orange451.mcwarfare.arena.killstreaks.MapItem;
import com.orange451.mcwarfare.arena.killstreaks.NonBlockPlayerPlacedItem;
import com.orange451.mcwarfare.listener.BlockListener;
import com.orange451.mcwarfare.listener.PlayerListener;
import com.orange451.mcwarfare.player.Clan;
import com.orange451.mcwarfare.player.DamageType;
import com.orange451.mcwarfare.player.GameKillstreak;
import com.orange451.mcwarfare.player.GamePerk;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.player.GameRank;
import com.orange451.mcwarfare.player.sql.SQLController;
import com.orange451.mcwarfare.util.FileIO;
import com.orange451.mcwarfare.util.Util;
import com.orange451.pvpgunplus.ParticleEffects;
import com.orange451.pvpgunplus.RaycastHelper;

public class MCWarfare extends JavaPlugin {
	private static MCWarfare mcwarfare;
	protected ArrayList<Arena> arenas;
	private List<String> messages = new ArrayList<String>();
	private Random random;
	private Connection con = null;
	private int updateTimer;
	private int secondsAlive = 0;
	private ArenaManager arenaManager;
	private GunManager gunManager;
	private CommandExecuter commandExecuter;
	private SQLController sqlController;
	protected boolean connected;

	public List<MapItem> mapItems = new ArrayList<MapItem>();
	public List<Location> glassThinReplace = new ArrayList<Location>();
	public ArrayList<GamePerk> serverPerks;
	public ArrayList<GameKillstreak> serverKillstreaks;
	public ArrayList<BuyableItem> buyableItems;
	public ArrayList<GameRank> ranks;
	public List<String> guns_gungame;
	public ArrayList<KitGun> loadedGuns = new ArrayList<KitGun>();
	public ArrayList<Clan> loadedClans = new ArrayList<Clan>();
	public double version = 2.5;

	public boolean multiThread = true;
	public boolean silent = false;
	public boolean autoSave = true;

	public void onEnable() {
		MCWarfare.mcwarfare = this;
		this.random = new Random();
		this.arenas = new ArrayList<Arena>();
		this.updateTimer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new GameUpdater(), 20L, 20L);
		this.secondsAlive = 0;
		this.arenaManager = new ArenaManager();
		this.gunManager = new GunManager();
		this.guns_gungame = new ArrayList<String>();
		this.buyableItems = new ArrayList<BuyableItem>();
		this.ranks = new ArrayList<GameRank>();
		this.commandExecuter = new CommandExecuter();
		this.sqlController = new SQLController();
		
		//Start the SQL saver
		new Thread(this.sqlController).start();

		//REGISTER EVENTS
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new BlockListener(), this);

		//Initialise server perks/killstreaks/guns
		this.serverPerks = new ArrayList<GamePerk>();
		this.serverKillstreaks = new ArrayList<GameKillstreak>();
		this.gunManager.initialise();

		//DEFINE PLAYER RANKS
		this.ranks.add(new GameRank("Private", "PVT", 1));
		this.ranks.add(new GameRank("Private II", "PV2", 2));
		this.ranks.add(new GameRank("Private First Class", "PFC", 3));
		this.ranks.add(new GameRank("Specialist", "SPC", 4));
		this.ranks.add(new GameRank("Corporal", "CPL", 5));
		this.ranks.add(new GameRank("Sergeant", "SGT", 6));
		this.ranks.add(new GameRank("Staff Sergeant", "SSG", 7));
		this.ranks.add(new GameRank("Sergeant First Class", "SFC", 8));
		this.ranks.add(new GameRank("Master Sergeant", "MSG", 9));
		this.ranks.add(new GameRank("1st Sergeant", "1SG", 10));
		this.ranks.add(new GameRank("Sergeant Major", "SGM", 11));
		this.ranks.add(new GameRank("Command Sergeant Major", "CSM", 12));
		this.ranks.add(new GameRank("Sergeant Major of the Army", "SMA", 13));
		this.ranks.add(new GameRank("Warrant Officer", "WO1", 14));
		this.ranks.add(new GameRank("Chief Warrant Officer II", "CW2", 15));
		this.ranks.add(new GameRank("Chief Warrant Officer III", "CW3", 16));
		this.ranks.add(new GameRank("Chief Warrant Officer IV", "CW4", 17));
		this.ranks.add(new GameRank("Chief Warrant Officer V", "CW5", 18));
		this.ranks.add(new GameRank("2nd Lieutenant", "2LT", 19));
		this.ranks.add(new GameRank("1st Lieutenant", "1LT", 20));
		this.ranks.add(new GameRank("Captain", "CPT", 21));
		this.ranks.add(new GameRank("Major", "MAJ", 22));
		this.ranks.add(new GameRank("Lieutenant colonel", "LTC", 23));
		this.ranks.add(new GameRank("Colonel", "COL", 24));
		this.ranks.add(new GameRank("Brigadier General", "BG", 25));
		this.ranks.add(new GameRank("Major General", "MG", 26));
		this.ranks.add(new GameRank("Lieutenant General", "LTG", 27));
		this.ranks.add(new GameRank("General", "GEN", 28));
		this.ranks.add(new GameRank("Major League Gamer", "MLG", 29));
		this.ranks.add(new GameRank("Professional Gamer", "PRO", 30));

		//ADD PERKS TO SERVER
		this.serverPerks.add(new GamePerk("Juggernaut", Material.RAW_BEEF, "Bullets do ⅔ less damage/to you"));
		this.serverPerks.add(new GamePerk("Stoppingpower", Material.RAW_CHICKEN, "Bullets do 1 heart more damage"));
		this.serverPerks.add(new GamePerk("SleightOfHand", Material.RAW_FISH, "Guns reload twice as fast"));
		this.serverPerks.add(new GamePerk("Speed", Material.CARROT_ITEM, "Run faster while alive"));
		this.serverPerks.add(new GamePerk("Marathon", Material.BREAD, "Never run out of energy/while running"));
		this.serverPerks.add(new GamePerk("Scavenger", Material.APPLE, "Gain ¼ ammo back/when you get a kill"));
		this.serverPerks.add(new GamePerk("SteadyAim", Material.COOKED_BEEF, "Increases hipfire accuracy"));
		this.serverPerks.add(new GamePerk("Martyrdom", Material.BAKED_POTATO, "Drop a grenade upon/your death"));
		this.serverPerks.add(new GamePerk("Painkiller", Material.POTATO_ITEM, "Regenerate health/when damaged"));
		this.serverPerks.add(new GamePerk("Hardline", Material.EGG, "Earn killstreaks with/one less kill"));
		this.serverPerks.add(new GamePerk("FlakJacket", Material.PUMPKIN_PIE, "Explosives do half/their normal damage"));

		//ADD BUYABLE ITEMS TO SERVER
		this.buyableItems.add(new BuyableItem("Speed", 200, Material.CARROT_ITEM) {
			public void execute(GamePlayer executer) {
				PotionEffect pot = new PotionEffect(PotionEffectType.SPEED, 99999, 0);
				executer.getPlayer().addPotionEffect(pot);
			}
		});

		this.buyableItems.add(new BuyableItem("Grenades", 200, Material.SLIME_BALL) {
			public void execute(GamePlayer executer) {
				executer.getPlayer().getInventory().addItem(new ItemStack(Material.SLIME_BALL, 2));
			}
		});

		this.buyableItems.add(new BuyableItem("Molotov", 100, Material.GLOWSTONE_DUST) {
			public void execute(GamePlayer executer) {
				executer.getPlayer().getInventory().addItem(new ItemStack(Material.GLOWSTONE_DUST, 1));
			}
		});

		this.buyableItems.add(new BuyableItem("Armor", 250, Material.IRON_CHESTPLATE) {
			public void execute(GamePlayer executer) {
				executer.getPlayer().getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
				executer.getPlayer().getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
			}
		});

		this.buyableItems.add(new BuyableItem("Super Armor", 550, Material.DIAMOND_CHESTPLATE) {
			public void execute(GamePlayer executer) {
				executer.getPlayer().getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
				executer.getPlayer().getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
			}
		});

		//ADD KILLSTREAKS TO SERVER
		this.serverKillstreaks.add(new GameKillstreak("Ammo", 1, 5, Material.getMaterial(2256), "Gives 20 more ammo") {
			public void execute(GamePlayer executer) {
				executer.getPlayer().sendMessage(ChatColor.AQUA + "5 Killstreak! Unlocked Ammo!");
				executer.addAmmo(20);
			}
		});

		this.serverKillstreaks.add(new GameKillstreak("CarePackage", 1, 5, Material.getMaterial(2262), "Drops an item-filled crate") {
			public void execute(GamePlayer executer) {
				executer.getPlayer().sendMessage(ChatColor.AQUA + "5 Killstreak! Unlocked Care package!");
				ItemStack itm = Util.namedItemStack(Material.GHAST_TEAR, 1, ChatColor.BLUE + "Care Package", "" + ChatColor.GRAY, "Right click to call for/a care package");
				executer.getPlayer().getInventory().addItem(itm);
				executer.getPlayer().updateInventory();
			}
		});

		this.serverKillstreaks.add(new GameKillstreak("E.M.P", 2, 15, Material.getMaterial(2257), "Disables vision on all enemies") {
			public void execute(GamePlayer executer) {
				if (executer.getArena().getArenaType().equals(ArenaType.FREE_FOR_ALL))
					return;
				executer.getArena().broadcastMessage(executer.getTag() + ChatColor.BOLD + ChatColor.RED + " LAUNCHED EMP!");
				ArrayList<GamePlayer> players = executer.getArena().getPlayers();
				
				for (int i = players.size() - 1; i >= 0; i--) {
					GamePlayer gplayer = (GamePlayer)players.get(i);
					if (!gplayer.getTeam().equals(executer.getTeam())) {
						PotionEffect pot = new PotionEffect(PotionEffectType.BLINDNESS, 20 * 8, 2);
						gplayer.getPlayer().addPotionEffect(pot);
					}
				}
			}
		});

		this.serverKillstreaks.add(new GameKillstreak("PoisonGas", 2, 16, Material.getMaterial(2260), "Poisons all enemies") {
			public void execute(GamePlayer executer) {
				if (executer.getArena().getArenaType().equals(ArenaType.FREE_FOR_ALL))
					return;
				executer.getArena().broadcastMessage(executer.getTag() + ChatColor.BOLD + ChatColor.RED + " RELEASED POISON!");
				ArrayList<GamePlayer> players = executer.getArena().getPlayers();
				
				for (int i = players.size() - 1; i >= 0; i--) {
					GamePlayer gplayer = (GamePlayer)players.get(i);
					if (!gplayer.getTeam().equals(executer.getTeam())) {
						PotionEffect pot = new PotionEffect(PotionEffectType.POISON, 20 * 20, 2);
						gplayer.getPlayer().addPotionEffect(pot);
					}
				}
			}
		});

		this.serverKillstreaks.add(new GameKillstreak("Airstrike", 2, 15, Material.getMaterial(2259), "Drops bombs on enemies") {
			public void execute(GamePlayer executer) {
				if (executer.getArena().getArenaType().equals(ArenaType.FREE_FOR_ALL))
					return;
				executer.getArena().broadcastMessage(executer.getTag() + ChatColor.BOLD + ChatColor.RED + " LAUNCHED AIRSTRIKE!");
				final GamePlayer gplayer = executer;
				final Player damager = executer.getPlayer();
				MCWarfare.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
					public void run() {
						gplayer.onKillstreak = true;
						int amount = 0;
						final Location average = new Location(damager.getWorld(), 0, 0, 0);
						ArrayList<GamePlayer> players = gplayer.getArena().getPlayers();
						
						for (int i = players.size() - 1; i >= 0; i--) {
							GamePlayer gplayer = (GamePlayer)players.get(i);
							if (MCWarfare.getPlugin().canDamagePlayer(damager, gplayer.getPlayer())) {
								average.add(gplayer.getPlayer().getLocation());
								amount++;
							}
						}

						if (amount > 0) {
							average.multiply(1/amount);
							ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone(), 0.2f, 0.2f, 0.2f, 0.3f, 2);
							for (int i = 8; i <= 26; i += 6) {
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(0,  i,  0), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(i,  0,  0), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(-i,  0,  0), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(0,  0,  i), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(0,  0,  -i), 0.2f, 0.2f, 0.2f, 0.3f, 1);

								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(i,  0,  i), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(-i,  0,  i), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(i,  0,  -i), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								ParticleEffects.sendParticle(null, 128, "hugeexplosion", average.clone().add(-i,  0,  -i), 0.2f, 0.2f, 0.2f, 0.3f, 1);
								List<Entity> nearby = RaycastHelper.getNearbyEntities(average, i);
								for (int ii = nearby.size() - 1; ii >= 0; ii--) {
									System.out.println(nearby.get(ii));
									if (nearby.get(ii) instanceof Player) {
										Player damage = (Player)nearby.get(ii);
										damage.setLastDamage(0);
										damage.damage(8, damager);
									}
								}
							}
						}
						gplayer.onKillstreak = false;
					}
				}, 20 * 5);
			}
		});

		this.serverKillstreaks.add(new GameKillstreak("AttackDogs", 3, 21, Material.getMaterial(2261), "Unleashes attack dogs") {
			public void execute(GamePlayer executer) {
				if (executer.getArena().getArenaType().equals(ArenaType.FREE_FOR_ALL))
					return;
				executer.getArena().broadcastMessage(executer.getTag() + ChatColor.BOLD + ChatColor.RED + " RELEASED ATTACK DOGS!");
				int amt = 5;
				for (int i = 0; i < amt; i++) {
					MCWarfare.getPlugin().mapItems.add(new AttackDog(executer, executer.getPlayer().getLocation()));
				}
			}
		});

		this.serverKillstreaks.add(new GameKillstreak("TacticalNuke", 3, 28, Material.getMaterial(2258), "Kills all enemies") {
			public void execute(GamePlayer executer) {
				if (executer.getArena().getArenaType().equals(ArenaType.FREE_FOR_ALL))
					return;
				executer.getArena().broadcastMessage(executer.getTag() + ChatColor.BOLD + ChatColor.RED + " LAUNCHED TACTICAL NUKE!");
				ArrayList<GamePlayer> players = executer.getArena().getPlayers();
				
				for (int i = players.size() - 1; i >= 0; i--) {
					GamePlayer gplayer = (GamePlayer)players.get(i);
					gplayer.getPlayer().playSound(gplayer.getPlayer().getLocation(), Sound.GHAST_DEATH, 1, 1);
				}

				final GamePlayer player = executer;
				MCWarfare.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
					public void run() {
						player.onKillstreak = true;
						ArrayList<GamePlayer> players = player.getArena().getPlayers();
						
						for (int i = players.size() - 1; i >= 0; i--) {
							GamePlayer gplayer = (GamePlayer)players.get(i);
							if (gplayer.getPlayer().isOnline() && gplayer.getTeam().equals(player.getTeam())) {
								gplayer.getPlayer().damage(200, player.getPlayer());
								ParticleEffects.sendParticle(null, 64, "hugeexplosion", gplayer.getPlayer().getLocation().clone().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 0.2f, 2);
							}
						}
						player.onKillstreak = false;
					}
				}, 20 * 7);
			}
		});

		//ADD GUNS TO GUNGAME LIST
		this.guns_gungame.add("usp45");
		this.guns_gungame.add("m9");
		this.guns_gungame.add("magnum");
		this.guns_gungame.add("deserteagle");
		this.guns_gungame.add("executioner");
		this.guns_gungame.add("python");
		this.guns_gungame.add("m1014");
		this.guns_gungame.add("spas12");
		this.guns_gungame.add("aa12");
		this.guns_gungame.add("moddel1887");
		this.guns_gungame.add("spas24");
		this.guns_gungame.add("typhoid");
		this.guns_gungame.add("m16");
		this.guns_gungame.add("m4a1");
		this.guns_gungame.add("ak47");
		this.guns_gungame.add("famas");
		this.guns_gungame.add("lemantation");
		this.guns_gungame.add("skullcrusher");
		this.guns_gungame.add("l118a");
		this.guns_gungame.add("dragunov");
		this.guns_gungame.add("barret50c");
		this.guns_gungame.add("msr");
		this.guns_gungame.add("l120_isolator");
		this.guns_gungame.add("disassembler");
		this.guns_gungame.add("demolisher");
		this.guns_gungame.add("law");

		//RECONNECT TO SQL DB AND LOAD ANNOUNCEMENTS
		reconnect();
		reloadMessages();

		this.arenaManager.loadArenas();

		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Player[] players = Bukkit.getOnlinePlayers();
				for (int i = 0; i < players.length; i++)
					MCWarfare.this.arenaManager.join(players[i]);
			}
		} , 20L);
	}

	public void onDisable() {
		//Fix all broken glass
		for (int i = 0; i < this.glassThinReplace.size(); i++)
			((Location)this.glassThinReplace.get(i)).getWorld().getBlockAt((Location)this.glassThinReplace.get(i)).setType(Material.THIN_GLASS);
		this.glassThinReplace.clear();
		
		//Remove all map items
		for (int i = 0; i < this.mapItems.size(); i++)
			mapItems.get(i).remove();
		this.mapItems.clear();

		//Turn off the main server loop
		getServer().getScheduler().cancelTask(this.updateTimer);
		
		//Turn off the SQL saver
		this.sqlController.stopSaving();
		
		//Stop all the arenas
		for (int i = this.arenas.size() - 1; i >= 0; i--) {
			Arena arena = (Arena)this.arenas.get(i);
			arena.stop();
		}
		
		//Save clans
		saveClans();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		this.commandExecuter.executeCommand(sender, commandLabel, args);

		return false;
	}

	private void reconnect() {

		try {
			if (this.con != null) {
				this.con.close();
				this.con = null;
			}
			this.con = getNewDatabaseConnection();
			if (con != null)
				this.connected = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			this.connected = false;
		}
	}
	
	public Connection getNewDatabaseConnection() {
		String url = "jdbc:mysql://"+getPlugin().getConfig().getString("mySql.ip", "127.0.0.1:3306")+"/"+getPlugin().getConfig().getString("mySql.db", "none");
		String user = getPlugin().getConfig().getString("mySql.user", "none");
		String password = getPlugin().getConfig().getString("mySql.pass", "none");
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection(url, user, password);
		}catch(Exception e) {
			//
		}
		return null;
	}

	private void reloadMessages() {
		this.messages.clear();
		BufferedReader br = FileIO.file_text_open_read(getFTP() + "/announcements.txt");
		String strLine = null;
		while ((strLine = FileIO.file_text_read_line(br)) != null) {
			this.messages.add(strLine);
		}
		FileIO.file_text_close(br);
	}

	public String getFTP() {
		return "/Shared/mcwar";
	}

	public static MCWarfare getPlugin() {
		return mcwarfare;
	}

	public String getPluginFolder() {
		File file = getDataFolder();
		file.mkdir();
		return file.getAbsolutePath();
	}

	public Connection getSQLDatabaseConnection() {
		return con;
	}

	public Random getRandom() {
		return this.random;
	}

	public ArenaManager getArenaManager() {
		return this.arenaManager;
	}

	public GunManager getGunManager() {
		return this.gunManager;
	}

	public boolean hasVoted(String playerName) {
		File file = new File(getFTP() + "/votes/" + playerName.toLowerCase() + ".txt");
		return file.isFile() && System.currentTimeMillis() - file.lastModified() < 24L * 60L * 60L * 1000L;
	}

	public void saveClans() {
		int amt = 0;
		for (int i = 0; i < this.loadedClans.size(); i++)
			if (loadedClans.get(i).hasBeenChanged())
				amt++;
		System.out.println("SAVING " + amt + " CLANS");
		for (int i = 0; i < this.loadedClans.size(); i++) {
			if (loadedClans.get(i).hasBeenChanged()) {
				loadedClans.get(i).save();
			}
		}
	}

	public void loadAllClans() {
		saveClans();
		this.sqlController.loadAllClans();
	}

	public boolean canDamagePlayer(Player attacker, Player defender) {
		GamePlayer g_attacker = this.getArenaManager().getGamePlayer(attacker.getName());
		GamePlayer g_defender = this.getArenaManager().getGamePlayer(defender.getName());
		if (g_attacker  == null || g_defender == null)
			return false;
		if (g_attacker != null && g_defender != null) {
			boolean isOnTeam = false;
			Arena ka = g_attacker.getArena();
			if (ka.getArenaType().equals(ArenaType.FREE_FOR_ALL))
				isOnTeam = false;
			else if (g_attacker.getTeam().equals(g_defender.getTeam()))
				isOnTeam = true;

			if (ka.getArenaType().equals(ArenaType.SPAWN))
				isOnTeam = true;

			if (attacker.getName().equals(defender.getName()))
				isOnTeam = true;

			return !isOnTeam;
		}
		return false;
	}

	public int getServerNumber() {
		return getServer().getPort() - 25565 + 1;
	}

	public void damagePlayer(Player player, int damage, DamageType dmgType, Player damager) {
		GamePlayer kp = this.arenaManager.getGamePlayer(player.getName());
		GamePlayer kp2 = this.arenaManager.getGamePlayer(damager.getName());
		if (kp != null && kp2 != null) {
			kp.onDamage(damage, dmgType, kp2);
		}else{
			player.damage(damage, damager);
		}
	}

	public List<MapItem> getMapItems() {
		return this.mapItems;
	}

	public void removeMapEntity(MapItem mapItem) {
		for (int i = this.mapItems.size() - 1; i >= 0; i--) {
			if (mapItems.get(i).equals(mapItem)) {
				if (!(mapItems.get(i) instanceof NonBlockPlayerPlacedItem))
					mapItems.get(i).getLocation().getWorld().getBlockAt(this.mapItems.get(i).getLocation()).setType(Material.AIR);
				mapItems.remove(i);
			}
		}
	}

	public BuyableItem matchBuyableItem(Material type) {
		for (int i = 0; i < this.buyableItems.size(); i++) {
			BuyableItem buyable = buyableItems.get(i);
			if (buyable.mat.equals(type)) {
				return buyable;
			}
		}
		return null;
	}

	public GamePerk matchPerk(String perk) {
		for (int i = 0; i < this.serverPerks.size(); i++) {
			GamePerk perk1 = serverPerks.get(i);
			if (perk1.getName().equalsIgnoreCase(perk)) {
				return perk1;
			}
		}
		return null;
	}

	public GameKillstreak matchKillstreak(String ks1) {
		for (int i = 0; i < this.serverKillstreaks.size(); i++) {
			GameKillstreak ks = serverKillstreaks.get(i);
			if (ks.getName().equalsIgnoreCase(ks1)) {
				return ks;
			}
		}
		return null;
	}

	public SQLController getSQLController() {
		return this.sqlController;
	}

	public GameRank matchRank(int level) {
		for (int i = 0; i < this.ranks.size(); i++) {
			GameRank rank = this.ranks.get(i);
			if (rank.getlevel() == level) {
				return this.ranks.get(i);
			}
		}
		return null;
	}

	public Clan loadClan(String name) {
		try {
			String query = "SELECT * FROM `MCWarClans` WHERE `clan_name` = '" + name + "'";
			Statement st = getPlugin().getSQLDatabaseConnection().createStatement();
			ResultSet result = st.executeQuery(query);
			if (!result.first()) {
				String params = "`clan_name`";
				String query2 = "INSERT INTO MCWarClans(" + params + ") VALUES('" + name + "')";
				st.execute(query2);

				Clan ret = new Clan(name);
				if (getClanByTag(name) == null) {
					this.loadedClans.add(ret);
				}
				return ret;
			}
			Clan ret = new Clan(result);
			if (getClanByTag(name) == null) {
				this.loadedClans.add(ret);
			}
			return ret;
		} catch (Exception localException) {
			//
		}

		return null;
	}

	public Clan getUserClan(String playerName) {
		for (int i = 0; i < this.loadedClans.size(); i++) {
			Clan c = this.loadedClans.get(i);
			if (c.hasMember(playerName)) {
				return c;
			}
		}
		return null;
	}

	public Clan getClanByTag(String name) {
		for (int i = 0; i < this.loadedClans.size(); i++) {
			Clan c = this.loadedClans.get(i);
			if (c.getName().equalsIgnoreCase(name)) {
				return c;
			}
		}
		return null;
	}

	public void storeVotes(ArrayList<VoteMap> votes) {
		try{
			for (int i = 0; i < votes.size(); i++) {
				String query = "SELECT * FROM `MCWarVotes` WHERE `map_name` = '" + votes.get(i).getArenaName() + "'";
				Statement st = MCWarfare.getPlugin().getSQLDatabaseConnection().createStatement();
				ResultSet result = st.executeQuery(query);
				if (!result.first()) { //No clan found, create a new one
					String params = "`map_name`, `map_votes`"; 
					String query2 = "INSERT INTO MCWarVotes(" + params + ") VALUES('" + votes.get(i).getArenaName() + "', '" + votes.get(i).getVotes() + "')";
					st.execute(query2);
				}else{
					String query2 = "UPDATE `MCWarVotes` SET map_votes=mapvotes+" + votes.get(i).getVotes() + " WHERE `map_name`='" + votes.get(i).getArenaName() + "'";
					st.executeUpdate(query2);
				}
			}
		}catch(Exception e) {
			//
		}
	}

	class GameUpdater implements Runnable {
		private int lastAnnouncement;

		public void run() {
			secondsAlive++;
			for (int i = arenas.size() - 1; i >= 0; i--) { //Tick the arenas
				arenas.get(i).tick();
			}

			if (secondsAlive % 320 == 0) { //Reconnect to DB every 5 minutes
				reconnect();
				sqlController.reconnectSQL();
			}

			if (secondsAlive % 15 == 0) { //After 10 seconds display an announcement message
				if (messages.size() == 0)
					return;
				if (lastAnnouncement >= messages.size())
					lastAnnouncement = 0;
				String message = messages.get(lastAnnouncement);
				if (message != null) {
					for (int i = 0; i < arenas.size(); i++) {
						if (arenas.get(i).getArenaType().equals(ArenaType.SPAWN)) {
							arenas.get(i).broadcastMessage(ChatColor.AQUA + "[BROADCAST]");
							arenas.get(i).broadcastMessage(ChatColor.GREEN + " " + message);
						}
					}
					lastAnnouncement += 1;
				}
			}

			if (secondsAlive % 60 == 0) { //Reload the announcements file every minute
				reloadMessages();
			}

			if (secondsAlive > (60 * 60) * 48) { //After 2 days, restart servers
				Player[] players = Bukkit.getOnlinePlayers();
				for (int i = players.length - 1; i >= 0; i--) {
					players[i].kickPlayer(ChatColor.GREEN + "The server is restarting, please rejoin");
				}
				Bukkit.shutdown();
			}
		}
	}

	public void print(String string) {
		if (this.silent)
			return;
		System.out.println(string);
	}
}