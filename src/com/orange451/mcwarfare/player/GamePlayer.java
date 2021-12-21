package com.orange451.mcwarfare.player;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.Arena;
import com.orange451.mcwarfare.arena.ArenaModifier;
import com.orange451.mcwarfare.arena.ArenaType;
import com.orange451.mcwarfare.arena.BuyableItem;
import com.orange451.mcwarfare.arena.EndGameReason;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.arena.Team;
import com.orange451.mcwarfare.util.InventoryHelper;
import com.orange451.mcwarfare.util.PacketUtils;
import com.orange451.mcwarfare.util.Util;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.gun.GunPlayer;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.kitteh.tag.TagAPI;
import org.mcsg.double0negative.tabapi.TabAPI;

public class GamePlayer {
	private Player player;
	private GameProfile profile;
	private Arena arena;
	private Team team = Team.BLUE;
	private int kills;
	private int deaths;
	private int killstreak;
	private int aliveTicks;
	private int gungameLevel;
	private int invincibleTicks = 0;
	private int xpTo;
	private int lastAnnouncedMapInfo = 0;
	private int clanInviteTimer;
	private Scoreboard board;
	private Objective scoreboard;
	
	public int disableChat = 4;
	public int suicideTicks;
	public boolean hasSpawnProtection;
	public boolean hasKillfeedEnabled = true;
	public boolean onKillstreak = false;
	public boolean dead = false;
	public boolean hasVoted = false;
	public boolean hasLoaded;
	public String clanInviteTo = "";
	public Location spawnLocation;
	public Location lastLocation;
	public GamePlayer lastDamager;
	public ArrayList<BuyableItem> boughtItems;
	public ArrayList<String> tags;
	
	public boolean useTabAPI;

	public GamePlayer(Arena arena, Player player) {
		
		this.useTabAPI = Bukkit.getPluginManager().getPlugin("TabAPI") != null;
		
		this.arena = arena;
		this.player = player;
		this.profile = new GameProfile(player.getName(), true);
		this.boughtItems = new ArrayList<BuyableItem>();
		this.tags = new ArrayList<String>();

		for (int i = 0; i < 20; i++) {
			this.player.sendMessage("");
		}
		
		this.player.sendMessage("v" + MCWarfare.getPlugin().version + "                   " + ChatColor.BLUE + ChatColor.UNDERLINE + "MULTIPLAYER" + ChatColor.RED + ChatColor.UNDERLINE + "SERVERS" + ChatColor.RESET);
		this.player.sendMessage(ChatColor.GRAY + "                   http://multiplayerservers.com");
		this.player.sendMessage("");
		this.player.sendMessage("                        " + ChatColor.GREEN + ChatColor.BOLD + "Welcome to MCWar!" + ChatColor.RESET);
		this.player.sendMessage("                               " + ChatColor.YELLOW + "Server: " + ChatColor.WHITE + MCWarfare.getPlugin().getServerNumber());
		this.player.sendMessage("                         " + ChatColor.YELLOW + "Currently in: " + ChatColor.WHITE + arena.getArenaType().toString());
		this.player.sendMessage("                           " + ChatColor.YELLOW + "Players online: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().length + "§3 §6 §3 §6 §3 §6 §e");
		this.player.sendMessage("");
		this.player.sendMessage("                       Need help? Type" + ChatColor.RED + " /help");
		this.player.sendMessage("-----------------------------------------------------");

		this.forceRespawn();

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		this.board = manager.getNewScoreboard();

		this.scoreboard = this.board.registerNewObjective("scoreboard", "dummy");
		this.scoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.scoreboard.setDisplayName("Scoreboard");

		this.player.setScoreboard(this.board);
	}

	public void tick() {
		this.disableChat -= 1;
		
		if (this.profile.corrupted) {
			player.kickPlayer(ChatColor.BLUE + "Corrupted Profile! \n\n"
					+ ChatColor.GRAY + "Please Visit\n"
					+ ChatColor.RED+ ChatColor.UNDERLINE + "http://www.multiplayerservers.com/\n"
					+ ChatColor.RESET + ChatColor.GRAY + "and report this!");
		}
		
		if (!this.sucessfullyLoaded()) {
			if (!this.arena.getArenaType().equals(ArenaType.SPAWN)) {
				player.chat("/spawn");
			}
			return;
		}
		
		if (!hasLoaded) {
			this.spawn();
			this.hasLoaded = true;
		}
		
		this.xpTo = getXpTo(this.profile.getLevel());
		if (!this.player.isDead()) {
			decideHat();
			this.aliveTicks += 1;
			this.invincibleTicks -= 1;
			this.suicideTicks -= 1;
			this.lastAnnouncedMapInfo -= 1;
			
			this.lastLocation = player.getLocation();
			this.player.setLevel(this.arena.getTimeRemaining());

			double frac = this.profile.getXp() / (this.xpTo + 0.001);
			this.player.setExp((float)frac);

			Score score4 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Credits"));
			score4.setScore(this.profile.getCredits());

			Score score = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Kills"));
			if (this.arena.getArenaType().equals(ArenaType.SPAWN))
				score.setScore(this.profile.getKills());
			else {
				score.setScore(this.kills);
			}
			Score score2 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Deaths"));
			if (this.arena.getArenaType().equals(ArenaType.SPAWN))
				score2.setScore(this.profile.getDeaths());
			else {
				score2.setScore(this.deaths);
			}
			Score score5 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Level"));
			score5.setScore(this.profile.getLevel());

			Score score3 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Killstreak"));
			score3.setScore(this.killstreak);

			if ((this.hasSpawnProtection) && (this.spawnLocation != null) && 
					(this.player.getLocation().distance(this.spawnLocation) > 1.75D)) {
				this.hasSpawnProtection = false;
			}

			if (this.invincibleTicks < 0) {
				this.hasSpawnProtection = false;
			}

			this.clanInviteTimer -= 1;
			if (this.clanInviteTimer < 0) {
				this.clanInviteTo = "";
			}

			try
			{
				if (this.player.isSprinting()) {
					if (!hasPerk("marathon"))
						this.player.setFoodLevel(this.player.getFoodLevel() - 1);
				} else if (this.player.getFoodLevel() < 19) {
					this.player.setFoodLevel(this.player.getFoodLevel() + 2);
				} else {
					this.player.setFoodLevel(20);
				}

				if ((this.player.getHealth() < 20.0D) && (this.player.getHealth() >= 0.0D)) {
					this.player.setHealth(this.player.getHealth() + 1.0D);
				}

				if (useTabAPI)
					updateTabList();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (this.profile.getXp() >= this.xpTo) {
			this.profile.setXp(this.profile.getXp() - this.xpTo);
			this.profile.setLevel(this.profile.getLevel() + 1);

			if (this.profile.getLevel() == 10) {
				this.profile.addClass();
			}

			String message = "" + ChatColor.GREEN + ChatColor.BOLD + "Level up!" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " You are now level " + ChatColor.DARK_PURPLE + this.profile.getLevel();
			this.player.sendMessage(message);
			this.player.playSound(this.player.getLocation(), Sound.LEVEL_UP, 20.0F, 1.0F);

			PacketUtils.displayTextBar(message, this.player, 150);
		}
	}

	public void forceRespawn() {
		if (player == null)
			return;
		
		//Clear all potion effects
		for (PotionEffect effect : this.player.getActivePotionEffects())
			this.player.removePotionEffect(effect.getType());
		
		//Clear your inventory
		this.player.getInventory().clear();
		this.player.getInventory().setHelmet(null);
		this.player.getInventory().setChestplate(null);
		this.player.getInventory().setLeggings(null);
		this.player.getInventory().setBoots(null);
		
		//Clear your chat
		if (!arena.getArenaType().equals(ArenaType.SPAWN)) {
			if (arena.getTicksSinceStart() < 4) {
				for (int i = 0; i < 20; i++)
					this.player.sendMessage("");
			}
		}
		
		//Allow map into to be sent to client
		this.lastAnnouncedMapInfo = -1;
		
		//Call spawn method
		spawn();
	}
	
	public String getTeamName() {
		String ret = "BLUE";
		if (this.team.equals(Team.RED))
			ret = "RED";
		
		if (this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)) {
			if (this.team.equals(Team.RED))
				ret = "Infected";
			else
				ret = "Survivor";
		}
		
		if (this.arena.getArenaType().equals(ArenaType.FREE_FOR_ALL))
			ret = "FFA";
		
		return ret;
	}
	
	private void giveInfoBooks() {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta bmeta = (BookMeta)book.getItemMeta();
		bmeta.setAuthor("orange451");
		bmeta.setTitle("How to play");
		bmeta.addPage(new String[] { ChatColor.RED + "How to play MCWar! \n\n" + ChatColor.GOLD + "1:" + ChatColor.GRAY + "Index\n" + ChatColor.GOLD + "2:" + ChatColor.GRAY + "Guns\n" + ChatColor.GOLD + "3:" + ChatColor.GRAY + "Donating\n" + ChatColor.GOLD + "4:" + ChatColor.GRAY + "Objectives" });
		bmeta.addPage(new String[] { ChatColor.RED + "Guns \n Normal Minecraft items are what make up GUNS in MCWar. \n\n" + 
				ChatColor.DARK_GRAY + "To shoot them, right click. \nTo aim in, left click" });
		bmeta.addPage(new String[] { ChatColor.RED + "Donating \n" + 
				ChatColor.DARK_GRAY + " Donators in MCWar get special benefits! \n\n You can get different guns, grenades, knives, or perks! \n\n if you are at all interested, type " + ChatColor.BLUE + "/buy" });
		bmeta.addPage(new String[] { ChatColor.RED + "Objectives \n" + 
				ChatColor.DARK_GRAY + " Every game has an objective, which is the goal you or you and your team are supposed to do. \n" });
		bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
				ChatColor.RED + " TDM:\n" + 
				ChatColor.DARK_GRAY + "  Your team has to get the most kills to win" });
		bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
				ChatColor.RED + " CTF:\n" + 
				ChatColor.DARK_GRAY + "  Your team has to capture the most flags to win" });
		bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
				ChatColor.RED + " FFA:\n" + 
				ChatColor.DARK_GRAY + "  You have to be the first person to reach 20 kills to win" });
		bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
				ChatColor.RED + " GUNGAME:\n" + 
				ChatColor.DARK_GRAY + "  Every person starts off with a level 1 gun. With each kill, their gun gets upgraded. \n\n The first person to 25 kills wins." });
		bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
				ChatColor.RED + " INFECT:\n" + 
				ChatColor.DARK_GRAY + "  You have to survive the longest without dying. \n\n At the start, one person is spawned as a zombie. When a person on the " + ChatColor.BLUE + "survivor" + ChatColor.DARK_GRAY + " team dies, they become a zombie." });
		bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
				ChatColor.RED + " ONEIN:" + ChatColor.GRAY + " (one in the chamber)\n" + 
				ChatColor.DARK_GRAY + "  Avoid getting shot, but shoot other people! \n  When you are shot, you lose a life. When your three lives are up, you are \"out\", the last person left standing wins." });
		book.setItemMeta(bmeta);

		this.player.getInventory().setItem(5, book);

		ItemStack book2 = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta bmeta2 = (BookMeta)book2.getItemMeta();
		bmeta2.setAuthor("orange451");
		bmeta2.setTitle(ChatColor.RED + "NEW UPDATE (1-14-2014)");
		bmeta2.addPage(new String[] { ChatColor.BLACK + "Updates for\nMinecraft 1.7.4" });
		bmeta2.addPage(new String[] { ChatColor.RED + "COMPLETE REWRITE\n\n " + ChatColor.BLACK + "We completely rewrote the MC-War server.\n\nIt should run much faster, and it also has better features."});
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "You can now have multiple classes." });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "SMG's have been added into the buyable guns." });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "There are now a lot more killstreaks in MCWar.\n\n-Attack Dogs\n-E.M.P\n-Care Package" });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "There are a bunch of new gamemodes.\n\n-Golden Gun\n-Kill Confirmed\n-Domination" });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "There are more perks.\n\n-Steady aim\n-Painkiller perks" });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "Added tactical insertions" });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "Added killfeeds in both chat and on HUD" });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "Added new command\n  /profile {name}\n\n\nShows you that users profile" });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "Added map voting" });
		bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "Speed was added back into /war buy" });
		bmeta2.addPage(new String[] { ChatColor.RED + "BUG FIXES\n\n " + ChatColor.BLACK + "Too many bug-fixes to count...\n\nMost of the ones in the old MC-War should be fixed." });
		book2.setItemMeta(bmeta2);

		this.player.getInventory().setItem(6, book2);
	}

	private void spawn() {
		this.killstreak = 0;
		if (player == null)
			return;
		this.player.setSprinting(false);
		
		if (lastAnnouncedMapInfo <= 0 && !arena.getArenaType().equals(ArenaType.SPAWN)) {
			lastAnnouncedMapInfo = 4;
			if (arena.getTicksSinceStart() < 4) {
				for (int i = 0; i < 20; i++)
					player.sendMessage("");
			}
			player.sendMessage("---------------------");
			player.sendMessage("| Map: " + ChatColor.GRAY + this.arena.getRealName());
			player.sendMessage("| GameMode: " + ChatColor.GRAY + this.arena.getGameModeAsString());
			player.sendMessage("| Team: " + this.arena.getTeamColor(this.team) + getTeamName());
			player.sendMessage("---------------------");
		}

		//Attempt to fix TAB API
		try{
			TagAPI.refreshPlayer(this.player);
			if (useTabAPI) {
				try {
					TabAPI.setPriority(MCWarfare.getPlugin(), this.player, -2);
					TabAPI.updatePlayer(this.player);
				} catch (Exception localException) {
					//
				}
				TabAPI.setPriority(MCWarfare.getPlugin(), this.player, 2);
			}
		}catch(Exception e) {
			//
		}

		//ADD POTIONS
		if ((this.team.equals(Team.RED)) && (this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)))
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 0));
		if (hasPerk("speed"))
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 0));

		this.dead = false;
		
		if (!this.sucessfullyLoaded())
			return;

		if (this.arena.getArenaType().equals(ArenaType.SPAWN)) {
			this.player.getInventory().setItem(0, Util.namedItemStack(Material.EYE_OF_ENDER, 1, ChatColor.GRAY + "MCWar Menu", null, null));
			this.player.getInventory().setItem(1, Util.namedItemStack(Material.BOOK, 1, ChatColor.RED + "Donation Link", null, null));
			this.player.getInventory().setItem(2, Util.namedItemStack(Material.BOOK, 1, ChatColor.RED + "Voting Link", null, null));
			this.player.getInventory().setItem(3, Util.namedItemStack(Material.BOOK, 1, ChatColor.RED + "Website Link", null, null));
			
			this.giveInfoBooks();
		} else {
			if (this.arena.getArenaModifier().equals(ArenaModifier.GUN_GAME)) {
				spawn_gunGame();
			} else if (this.arena.getArenaModifier().equals(ArenaModifier.ONE_IN_THE_CHAMBER)) {
				spawn_oneInChamber();
			} else {
				GameClass mclass = this.profile.getCurrentClass();
				if ((!this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)) || (!this.team.equals(Team.RED))) {
					this.player.getInventory().setItem(0, mclass.getKnife());
					this.player.getInventory().setItem(1, mclass.getPrimary());
					this.player.getInventory().setItem(2, mclass.getSecondary());
				}
				
				if (this.arena.getArenaModifier().equals(ArenaModifier.GOLDEN_GUN))
					this.player.getInventory().setItem(7, Util.namedItemStack(Material.COMPASS, 1, ChatColor.GRAY + "Golden Gun Tracker", null, null));

				if (!mclass.getLethal().getType().equals(Material.SNOW_BALL)) {
					KitGun tgun = MCWarfare.getPlugin().getGunManager().getGun(mclass.getLethal());
					if (tgun != null)
						this.player.getInventory().setItem(3, tgun.getItemStack());
					else {
						this.player.getInventory().setItem(3, mclass.getLethal());
					}

				}

				if (!mclass.getTactical().getType().equals(Material.SNOW_BALL)) {
					KitGun tgun = MCWarfare.getPlugin().getGunManager().getGun(mclass.getTactical());
					if (tgun != null)
						this.player.getInventory().setItem(4, tgun.getItemStack());
					else {
						this.player.getInventory().setItem(4, mclass.getTactical());
					}
				}

				if ((!this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)) || (!this.team.equals(Team.RED)))
				{
					int ammo = MCWarfare.getPlugin().getGunManager().getMaxAmmo(this);
					int primaryAmmo = MCWarfare.getPlugin().getGunManager().getGunAmmo(mclass.getPrimary().getTypeId());
					int secondaryAmmo = MCWarfare.getPlugin().getGunManager().getGunAmmo(mclass.getSecondary().getTypeId());
					if (primaryAmmo > 0)
						this.player.getInventory().setItem(18, Util.namedItemStack(Material.getMaterial(primaryAmmo), ammo, ChatColor.GRAY + "Primary Ammo", null, null));
					if (secondaryAmmo > 0) {
						this.player.getInventory().setItem(26, Util.namedItemStack(Material.getMaterial(secondaryAmmo), ammo / 2, ChatColor.GRAY + "Secondary Ammo", null, null));
					}
					this.player.getInventory().setItem(8, Util.namedItemStack(Material.EYE_OF_ENDER, 1, ChatColor.GRAY + "Class Selection", null, null));
				}

			}

			for (int i = 0; i < this.boughtItems.size(); i++) {
				((BuyableItem)this.boughtItems.get(i)).execute(this);
			}

			if (this.arena.getTicksSinceStart() <= 2) {
				this.player.playSound(this.player.getLocation(), Sound.AMBIENCE_CAVE, 8.0F, 1.0F);
			}
		}
		
		if (this.arena.getArenaModifier().equals(ArenaModifier.INFECTION) && this.team.equals(Team.RED)) {
			
		}
		
		this.player.updateInventory();
	}

	private void spawn_oneInChamber() {
		GameClass mclass = this.profile.getCurrentClass();

		int ammo = 1;
		int gunId = MCWarfare.getPlugin().getGunManager().getGun(mclass.getSecondary()).type;
		int secondaryAmmo = MCWarfare.getPlugin().getGunManager().getGunAmmo(mclass.getSecondary().getTypeId());

		if (secondaryAmmo > 0) {
			this.player.getInventory().setItem(18, Util.namedItemStack(Material.getMaterial(secondaryAmmo), ammo, ChatColor.GRAY + "Secondary Ammo", null, null));
		}
		ItemStack gun = new ItemStack(gunId, 1);

		this.player.getInventory().addItem(new ItemStack[] { gun });
		this.player.getInventory().addItem(new ItemStack[] { mclass.getKnife() });
	}

	private void spawn_gunGame() {
		if (this.gungameLevel >= MCWarfare.getPlugin().guns_gungame.size())
			return;
		this.player.getInventory().clear();
		GameClass mclass = this.profile.getCurrentClass();

		int ammo = MCWarfare.getPlugin().getGunManager().getMaxAmmo(this);
		int gunId = MCWarfare.getPlugin().getGunManager().getGun((String)MCWarfare.getPlugin().guns_gungame.get(this.gungameLevel)).type;
		int primaryAmmo = MCWarfare.getPlugin().getGunManager().getGunAmmo(gunId);

		if (primaryAmmo > 0) {
			this.player.getInventory().setItem(18, Util.namedItemStack(Material.getMaterial(primaryAmmo), ammo, ChatColor.GRAY + "Primary Ammo", null, null));
		}
		ItemStack gun = new ItemStack(gunId, 1);

		this.player.getInventory().addItem(new ItemStack[] { mclass.getKnife() });
		this.player.getInventory().addItem(new ItemStack[] { gun });
	}

	private void giveItem(int id, int amt, int slot) {
		if (id > 0) {
			Material mat = Material.getMaterial(id);
			if ((mat != null) && (!mat.equals(Material.AIR))) {
				ItemStack itm = new ItemStack(mat, amt);
				this.player.getInventory().setItem(slot, itm);
			}
		}
	}

	public void onDeath() {
		this.killstreak = 0;
		this.dead = true;
		this.deaths += 1;
		this.aliveTicks = 0;
		if ((this.lastDamager != null) || ((this.arena.getTicksSinceStart() > 10) && (!this.arena.getArenaType().equals(ArenaType.SPAWN)))) {
			this.profile.addDeath();
			if (this.lastDamager != null) {
				final GamePlayer lastdmg = this.lastDamager;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
					public void run() {
						PacketUtils.displayTextBar(ChatColor.GRAY + "You were killed by " + ChatColor.WHITE + lastdmg.getTag(), GamePlayer.this.player, 100);
					}
				}
				, 4L);
			}
		}
		
		if (this.arena.getTicksSinceStart() > 4) {
			PVPGunPlus pvpgunplus = PVPGunPlus.getPlugin();
			GunPlayer gplayer = pvpgunplus.getGunPlayer(this.player);
			if (gplayer != null) {
				gplayer.reloadAllGuns();
				if (hasPerk("martyrdom")) {
					gplayer.forceFireGun("grenade");
				}
			}
		}
		
		if (this.arena.getArenaModifier().equals(ArenaModifier.INFECTION))
			this.setTeam(Team.RED);

		this.lastDamager = null;
	}

	public void onDamagedByEvent(EntityDamageByEntityEvent event) {
		if (hasPerk("juggernaut"))
			event.setDamage((int)(event.getDamage() / 2.0D));
		if (hasPerk("painkiller")) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 25, 4));
		}

		if ((this.invincibleTicks > 0) || (this.hasSpawnProtection))
			event.setCancelled(true);
	}

	public void onDamage(double damage, DamageType dmgType, GamePlayer damager) {
		if (this.suicideTicks > 0)
			return;
		this.lastDamager = damager;
		if ((dmgType.equals(DamageType.EXPLOSION)) && 
				(hasPerk("flakjacket"))) {
			damage *= 0.4D;
		}

		if ((this.invincibleTicks <= 0) && (!this.hasSpawnProtection)) {
			this.player.damage(damage, damager.player);
			this.player.setLastDamage(0.0D);
		}
	}

	public void onAttack(EntityDamageByEntityEvent event) {
		if (this.suicideTicks > 0) {
			event.setCancelled(true);
		}
		if (hasPerk("stoppingpower"))
			event.setDamage(event.getDamage() + 2.0D);
	}

	public void onKill(GamePlayer kp) {
		if (this.suicideTicks > 0)
			return;
		int xp = MCWarfare.getPlugin().getGunManager().getXpPerKill(this);
		int credits = MCWarfare.getPlugin().getGunManager().getCreditserKill(this);

		this.killstreak += 1;
		this.kills += 1;
		this.profile.addKill();
		this.profile.giveXp(xp);
		this.profile.addCredits(credits);
		if ((hasPerk("scavenger")) && (
				(!this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)) || ((this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)) && (this.team.equals(Team.BLUE))))) {
			addAmmo(10);
		}

		PacketUtils.displayTextBar(ChatColor.GRAY + "Killed " + ChatColor.WHITE + kp.getTag() + ChatColor.YELLOW + "  +" + xp + " xp!  +" + credits + " credits!", this.player, 60);

		if ((this.arena.getArenaType().equals(ArenaType.FREE_FOR_ALL)) && (this.arena.getArenaModifier().equals(ArenaModifier.NONE)) && 
				(this.kills >= 30)) {
			this.arena.endGame(EndGameReason.PLAYER_WIN);
		}

		if (this.arena.getArenaModifier().equals(ArenaModifier.GUN_GAME))
		{
			if (this.player.getItemInHand().getType().toString().toLowerCase().contains("sword")) {
				kp.gungameLevel -= 1;
				if (kp.gungameLevel <= 0)
					kp.gungameLevel = 0;
				kp.getPlayer().sendMessage(ChatColor.RED + "You have been demoted!");
				return;
			}

			this.gungameLevel += 1;

			if (this.gungameLevel >= MCWarfare.getPlugin().guns_gungame.size()) {
				this.arena.endGame(EndGameReason.PLAYER_WIN);
				return;
			}

			this.player.getInventory().clear();
			spawn_gunGame();
			String newGun = (String)MCWarfare.getPlugin().guns_gungame.get(this.gungameLevel);
			this.player.sendMessage(ChatColor.YELLOW + "RANK UP! gun:" + ChatColor.WHITE + newGun);
			spawn_gunGame();
		}

		if (!this.onKillstreak)
			doKillStreak();
		else
			MCWarfare.getPlugin().print("GOT KILL ON KILLSTREAK");
	}

	public void addAmmo(int amt) {
		try {
			ItemStack itm1 = new ItemStack(MCWarfare.getPlugin().getGunManager().getGunAmmo(this.profile.getCurrentClass().getPrimary().getTypeId()), amt);
			ItemStack itm2 = new ItemStack(MCWarfare.getPlugin().getGunManager().getGunAmmo(this.profile.getCurrentClass().getSecondary().getTypeId()), (int)(amt * 1.5D));
			int slot = InventoryHelper.getItemPosition(this.player.getInventory(), itm1.getType());
			int slot2 = InventoryHelper.getItemPosition(this.player.getInventory(), itm2.getType());

			if (this.arena.getArenaModifier().equals(ArenaModifier.ONE_IN_THE_CHAMBER)) {
				itm1.setAmount(0);
				itm2.setAmount(1 + (hasPerk("scavenger") ? 1 : 0));
			}

			if (itm1.getAmount() > 0) {
				if (slot > -1)
					this.player.getInventory().getItem(slot).setAmount(this.player.getInventory().getItem(slot).getAmount() + itm1.getAmount());
				else {
					this.player.getInventory().setItem(InventoryHelper.getFirstFreeSlot(this.player.getInventory()), itm1);
				}
			}
			if (slot2 > -1)
				this.player.getInventory().getItem(slot2).setAmount(this.player.getInventory().getItem(slot2).getAmount() + itm2.getAmount());
			else
				this.player.getInventory().setItem(InventoryHelper.getFirstFreeSlot(this.player.getInventory()), itm2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateTabList() {
		int hei = TabAPI.getVertSize() - 1;

		MCWarfare plugin = MCWarfare.getPlugin();

		TabAPI.clearTab(this.player);

		int k = this.kills;
		int d = this.deaths;
		double kdr = this.profile.getKDR();
		if (this.arena.getArenaType().equals(ArenaType.SPAWN)) {
			k = this.profile.getKills();
			d = this.profile.getDeaths();
			kdr = (Math.round( ((double)k) / ((double)d) * 100.0))/100d;
		}

		TabAPI.setTabString(plugin, this.player, 0, 2, ChatColor.DARK_RED + "KILLS " + ChatColor.AQUA + Integer.toString(k));
		TabAPI.setTabString(plugin, this.player, 1, 2, ChatColor.DARK_RED + "DEATH " + ChatColor.AQUA + Integer.toString(d));
		TabAPI.setTabString(plugin, this.player, 2, 2, ChatColor.DARK_RED + "KDR " + ChatColor.AQUA + Double.toString(kdr));

		TabAPI.setTabString(plugin, this.player, 1, 0, ChatColor.GOLD + "LEVEL " + ChatColor.YELLOW + Integer.toString(this.profile.getLevel()));
		TabAPI.setTabString(plugin, this.player, 0, 0, drawXp());
		TabAPI.setTabString(plugin, this.player, 2, 1, ChatColor.DARK_GREEN + "$$ " + ChatColor.GREEN + Integer.toString(this.profile.getCredits()));

		TabAPI.setTabString(plugin, this.player, 2, 0, ChatColor.UNDERLINE + "TEAM " + arena.getTeamColor(this.team) + this.team.toString().toLowerCase());
		TabAPI.setTabString(plugin, this.player, 1, 1, ChatColor.GREEN + "killstreak " + Integer.toString(this.killstreak));
		TabAPI.setTabString(plugin, this.player, 0, 1, this.arena.getArenaStats().getLeader());

		int bottom = hei;

		displayTeam(null, 0, 5, bottom, false);

		TabAPI.updatePlayer(this.player);
		
	}

	private void displayTeam(Team team, int startX, int startY, int maxY, boolean collumn) {
		ArrayList<GamePlayer> players1 = this.arena.getArenaStats().getPlayersOnTeam(team);
		TabAPI.setTabString(MCWarfare.getPlugin(), this.player, startY - 1, startX, "" + ChatColor.GRAY + ChatColor.UNDERLINE + "players " + ChatColor.YELLOW + Integer.toString(players1.size()));
		for (int i = 0; i < players1.size(); i++)
			if (collumn) {
				int posy = startY + i;
				if (posy < maxY) {
					if (players1.get(i).getPlayer() != null) {
						ChatColor teamColor = players1.get(i).arena.getTeamColor(players1.get(i).team);
						String name = players1.get(i).getPlayer().getName();
						if (name != null && teamColor != null) {
							TabAPI.setTabString(MCWarfare.getPlugin(), this.player, posy, startX, teamColor + name);
						}
					}
				}
			} else {
				int posy = startY + i / 3;
				int posx = i % 3;
				if (posy < maxY) {
					if (players1.get(i).getPlayer() != null) {
						ChatColor teamColor = players1.get(i).arena.getTeamColor(players1.get(i).team);
						String name = players1.get(i).getPlayer().getName();
						if (teamColor != null && name != null) {
							TabAPI.setTabString(MCWarfare.getPlugin(), this.player, posy, posx, teamColor + name);
						}
					}
				}
			}
	}

	private String drawXp()
	{
		return this.profile.getXp() + "/" + this.xpTo;
	}

	private void doKillStreak() {
		if ((this.arena.getArenaType().equals(ArenaType.FREE_FOR_ALL)) || (this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)))
			return;
		executeKillstreak(this.profile.killstreak1);
		executeKillstreak(this.profile.killstreak2);
		executeKillstreak(this.profile.killstreak3);
	}

	private void executeKillstreak(String name) {
		if ((name == null) || (name.length() <= 1))
			return;
		ArrayList<GameKillstreak> killstreaks = MCWarfare.getPlugin().serverKillstreaks;
		for (int i = 0; i < killstreaks.size(); i++) {
			GameKillstreak killstreak = (GameKillstreak)killstreaks.get(i);
			if (killstreak.getName().equalsIgnoreCase(name)) {
				int amt = killstreak.getKillsNeeded();
				if (hasPerk("hardline"))
					amt--;
				if (this.killstreak == amt)
					killstreak.execute(this);
			}
		}
	}

	public void setInvincibleTicks(int x)
	{
		this.invincibleTicks = x;
	}

	public boolean hasPerk(String string) {
		if (this.profile.getCurrentClass() != null)
			return this.profile.getCurrentClass().getPerk().equalsIgnoreCase(string);
		return false;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Team getTeam() {
		return this.team;
	}

	public void setArena(Arena arena) {
		this.player.getInventory().clear();
		this.kills = 0;
		this.deaths = 0;
		this.killstreak = 0;
		this.arena = arena;
		this.gungameLevel = 0;

		for (PotionEffect effect : this.player.getActivePotionEffects())
			this.player.removePotionEffect(effect.getType());
		this.player.getInventory().clear();

		spawn();
	}

	public GameProfile getProfile() {
		return this.profile;
	}

	public Player getPlayer() {
		return this.player;
	}

	public boolean sucessfullyLoaded() {
		return this.profile.loaded;
	}

	public void disconnect() {
		this.arena.onLeave(this);
		this.player = null;
		//MCWarfare.getPlugin().getSQLController().doSave = true;
	}

	public int getXpTo(int level) {
		return (int)Math.floor(Math.pow(46 * level, 1.38D) / 10.0D) * 10;
	}

	private int getAmountXpToLevel(int level) {
		int count = 0;
		for (int i = 0; i < level; i++) {
			count += getXpTo(i);
		}
		return count;
	}

	public int getKills() {
		return this.kills;
	}

	public Arena getArena() {
		return this.arena;
	}

	public GameRank getRank() {
		if (getClan() != null) {
			return new GameRank("Clan", getClan().getName(), 0);
		}
		return this.profile.getRank();
	}

	public String getTag() {
		ChatColor color = arena.getTeamColor(this.team);
		String prefix = getRank().getTag();
		if (getClan() != null && getClan().getOwner().equals(getPlayer().getName()))
			prefix = ChatColor.UNDERLINE + prefix;
		prefix = getRankColor() + prefix;
		String lol = this.player.getName();
		String n = prefix + color + lol;
		return n;
	}

	private String getRankColor() {
		if (this.player.isOp())
			return "" + ChatColor.GOLD;
		if (this.profile.hasPermission("smod"))
			return "" + ChatColor.DARK_PURPLE;
		if (this.profile.hasPermission("mod"))
			return "" + ChatColor.LIGHT_PURPLE;
		if (this.profile.hasPermission("mvp"))
			return "" + ChatColor.AQUA;
		if (this.profile.hasPermission("vip"))
			return "" + ChatColor.GREEN;
		return "";
	}

	private void decideHat() {
		int hexColor = 255;
		if (this.team.equals(Team.RED)) {
			hexColor = 16711680;
			if (this.arena.getArenaModifier().equals(ArenaModifier.INFECTION)) {
				hexColor = 3381504;
			}
		}

		if (this.arena.getArenaType().equals(ArenaType.FREE_FOR_ALL)) {
			hexColor = 16711935;
		}
		Color color = Color.fromRGB(hexColor);

		ItemStack c0 = setColor(new ItemStack(Material.LEATHER_HELMET, 1), color);
		ItemStack c1 = setColor(new ItemStack(Material.LEATHER_CHESTPLATE, 1), color);
		ItemStack c2 = setColor(new ItemStack(Material.LEATHER_LEGGINGS, 1), color);
		ItemStack c3 = setColor(new ItemStack(Material.LEATHER_BOOTS, 1), color);
		
		if (this.arena.getArenaModifier().equals(ArenaModifier.INFECTION) && this.team.equals(Team.RED)) {
			MaterialData data = new MaterialData(397);
			data.setData((byte)2);
			c0 = data.toItemStack(1);
		}
		
		if (this.player.getInventory().getHelmet() == null)
			this.player.getInventory().setHelmet(c0);
		if (this.player.getInventory().getChestplate() == null)
			this.player.getInventory().setChestplate(c1);
		if (this.player.getInventory().getLeggings() == null)
			this.player.getInventory().setLeggings(c2);
		if (this.player.getInventory().getBoots() == null)
			this.player.getInventory().setBoots(c3);
	}

	private ItemStack setColor(ItemStack is, Color color) {
		LeatherArmorMeta lam = (LeatherArmorMeta)is.getItemMeta();
		lam.setColor(color);
		is.setItemMeta(lam);
		return is;
	}
	
	public Clan getClan() {
		return this.profile.getClan();
	}

	public void setClan(Clan temp) {
		this.profile.setClan(temp);
	}

	public void inviteToClan(Clan clan) {
		this.clanInviteTimer = 60;
		this.clanInviteTo = clan.getName();
		this.player.sendMessage(ChatColor.GREEN + "You have been invited to the clan: " + ChatColor.BLUE + clan.getName());
	}

	public boolean hasTag(String string) {
		for (int i = 0; i < tags.size(); i++) {
			if (tags.get(i).equalsIgnoreCase(string)) {
				return true;
			}
		}
		return false;
	}

	public void save() {
		this.profile.save(false, this.arena.getSQLController());
		this.arena.saveStats();
	}
}