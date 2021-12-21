package com.orange451.mcwarfare.listener;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.Arena;
import com.orange451.mcwarfare.arena.ArenaCreator;
import com.orange451.mcwarfare.arena.ArenaItem;
import com.orange451.mcwarfare.arena.ArenaItemFlagStand;
import com.orange451.mcwarfare.arena.ArenaModifier;
import com.orange451.mcwarfare.arena.ArenaSpawn;
import com.orange451.mcwarfare.arena.LaunchPad;
import com.orange451.mcwarfare.arena.VoteMap;
import com.orange451.mcwarfare.arena.killstreaks.AttackDog;
import com.orange451.mcwarfare.arena.killstreaks.C4;
import com.orange451.mcwarfare.arena.killstreaks.CarePackage;
import com.orange451.mcwarfare.arena.killstreaks.TacticalInsertion;
import com.orange451.mcwarfare.player.ChestExplorer;
import com.orange451.mcwarfare.player.Clan;
import com.orange451.mcwarfare.player.DamageType;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.util.InventoryHelper;
import com.orange451.mcwarfare.util.ReflectionHelper;
import com.orange451.mcwarfare.util.Util;
import com.orange451.pvpgunplus.events.PVPGunPlusFireGunEvent;
import com.orange451.pvpgunplus.events.PVPGunPlusReloadGunEvent;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R1.EnumDifficulty;
import net.minecraft.server.v1_7_R1.EnumGamemode;
import net.minecraft.server.v1_7_R1.PacketPlayOutRespawn;
import net.minecraft.server.v1_7_R1.WorldType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class PlayerListener implements Listener {
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPVPGunPlusGunReload(PVPGunPlusReloadGunEvent event)
	{
		GamePlayer kp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(event.getPlayer().getName());
		if ((kp != null) && (kp.hasPerk("sleightofhand")))
			event.setReloadTime(event.getReloadTime() / 2);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPVPGunPlusFireGun(PVPGunPlusFireGunEvent event) {
		GamePlayer kp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(event.getShooterAsPlayer().getName());
		if ((kp != null) && (kp.hasPerk("steadyaim")) && (!event.getShooter().isAimedIn()))
			event.setGunAccuracy(event.getGunAccuracy() / 2.0D);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerReceiveNameTag(PlayerReceiveNameTagEvent event) {
		GamePlayer pl = MCWarfare.getPlugin().getArenaManager().getGamePlayer(event.getNamedPlayer().getName());
		if (pl != null)
			event.setTag(pl.getArena().getTeamColor(pl.getTeam()) + pl.getPlayer().getName());
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event)
	{
		MCWarfare.getPlugin().getArenaManager().join(event.getPlayer());
		event.setJoinMessage(null);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onDisconnect(PlayerQuitEvent event) {
		MCWarfare.getPlugin().getArenaManager().leave(event.getPlayer());
		event.setQuitMessage(null);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onRespawn(final PlayerRespawnEvent event) {
		final GamePlayer player = MCWarfare.getPlugin().getArenaManager().getGamePlayer(event.getPlayer().getName());
		if (player != null) {
			player.dead = false;
			ArenaSpawn spawn = player.getArena().getArenaStats().getSpawn(player);
			if (spawn != null) {
				event.setRespawnLocation(spawn.getLocation());
				spawn.spawn(player.getPlayer());

				Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
					public void run() {
						player.hasSpawnProtection = true;
						player.spawnLocation = event.getRespawnLocation();
						player.setInvincibleTicks(5);
						player.forceRespawn();
					}
				}
				, 4L);
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.setDroppedExp(0);

		GamePlayer killed = MCWarfare.getPlugin().getArenaManager().getGamePlayer(event.getEntity().getName());
		if (killed != null) {
			List<?> mapItems = MCWarfare.getPlugin().getMapItems();
			for (int i = mapItems.size() - 1; i >= 0; i--) {
				if ((mapItems.get(i) instanceof C4)) {
					C4 c4 = (C4)mapItems.get(i);
					if (c4.getOwner().getPlayer().getName().equals(killed.getPlayer().getName()))
						c4.detonate();
				}
			}
			
			killed.getArena().onDeath(killed);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event)
	{
		if (event.getReason().equals("You moved too quickly :( (Hacking?)"))
				event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerChat(PlayerChatEvent event)
	{
		if (!event.isCancelled()) {
			Player p = event.getPlayer();
			GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(p.getName());
			if (gplayer != null) {
				String msg = "<" + gplayer.getTag() + ChatColor.WHITE + "> " + event.getMessage();
				gplayer.getArena().broadcastMessage(msg);
			}

			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player pl = event.getPlayer();
		if ((pl != null) && (event.getTo() != null) && (event.getFrom() != null)) {
			double dist = event.getFrom().distanceSquared(event.getTo());
			if (dist > 0.0D) {
				try {
					Block b = pl.getLocation().add(0.0D, -1.0D, 0.0D).getBlock();
					Material mat = b.getType();
					if (mat.equals(Material.SPONGE)) {
						LaunchPad lp = LaunchPad.getLaunchPad(b);
						if (lp != null)
							lp.launch(pl);
					}
				} catch (Exception localException) {
					//
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack iteminhand = player.getItemInHand();
		
		//Block the ability to throw ender pearls
		if ((iteminhand != null) && (iteminhand.getType().equals(Material.ENDER_PEARL))) {
			event.setCancelled(true);
		}

		Material mat = iteminhand.getType();
		
		//If you right clicked
		if (iteminhand != null && event.getAction().toString().toLowerCase().contains("right")) {
			//If you right clicked the eye of ender
			if (mat.equals(Material.EYE_OF_ENDER)) {
				GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
				ChestExplorer.openChestMenu(gplayer);
				event.setCancelled(true);
			}
			
			//If you right clicked the eye of ender
			if (mat.equals(Material.BOOK)) {
				String itemName = iteminhand.getItemMeta().getDisplayName();
				
				//You right clicked a link
				if (itemName.toLowerCase().contains("link")) {
					if (itemName.toLowerCase().contains("donation"))
						player.sendMessage(ChatColor.GRAY + "The link to donate is " + ChatColor.RED + "http://mcwar.buycraft.net/");
					if (itemName.toLowerCase().contains("voting"))
						player.sendMessage(ChatColor.GRAY + "The link to vote is " + ChatColor.RED + "http://tinyurl.com/ljx2fq5");
					if (itemName.toLowerCase().contains("website"))
						player.sendMessage(ChatColor.GRAY + "The link to our website is " + ChatColor.RED + "http://multiplayerservers.com/");
				}
				event.setCancelled(true);
			}

			//If you right clicked the ghast tear
			if ((mat.equals(Material.GHAST_TEAR))) {
				event.setCancelled(true);
				GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
				if (gplayer != null) {
					InventoryHelper.removeItem(player.getInventory(), Material.GHAST_TEAR.getId(), 1);
					if (iteminhand.getItemMeta().getDisplayName().toLowerCase().contains("package")) {
						MCWarfare.getPlugin().mapItems.add(new CarePackage(gplayer, gplayer.getPlayer().getEyeLocation()));
					}
				}
			}
		}
		
		if (mat.equals(Material.BOW)) {
			System.out.println("SHOOTING BOW " + event.getAction().toString());
		}

		if (mat.equals(Material.SHEARS)) {
			GamePlayer kp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
			if (kp != null) {
				List<?> mapItems = MCWarfare.getPlugin().getMapItems();
				for (int i = mapItems.size() - 1; i >= 0; i--) {
					if ((mapItems.get(i) instanceof C4)) {
						C4 c4 = (C4)mapItems.get(i);
						if (c4.getOwner().getPlayer().getName().equals(kp.getPlayer().getName())) {
							c4.detonate();
						}
					}
				}
			}
			player.setItemInHand(null);
		}

		if (mat.equals(Material.STRING)) {
			event.setCancelled(true);
			return;
		}

		if (event.hasBlock()) {
			if ((event.getClickedBlock().getType().equals(Material.FIRE)) || (event.getClickedBlock().getType().equals(Material.LEVER))) {
				event.setCancelled(true);
				return;
			}
			if (iteminhand != null) {
				String holding = iteminhand.getType().toString().toLowerCase();

				if (iteminhand.getTypeId() == 397) {
					event.setCancelled(true);
				}
				if (((event.getClickedBlock().getType().equals(Material.DIRT)) || (event.getClickedBlock().getType().equals(Material.GRASS)) || (event.getClickedBlock().getType().equals(Material.SOIL))) && (holding.contains("hoe"))) {
					event.setCancelled(true);
				}

				if (event.getClickedBlock().getType().equals(Material.CHEST)) {
					List<?> items = MCWarfare.getPlugin().mapItems;
					for (int i = 0; i < items.size(); i++) {
						if (((items.get(i) instanceof CarePackage)) && 
								(((CarePackage)items.get(i)).getOwner().getPlayer().equals(player))) {
							((CarePackage)items.get(i)).givePlayerItems();
						}
					}

				}

				if ((event.getClickedBlock().getType().equals(Material.CHEST)) || 
						(event.getClickedBlock().getType().equals(Material.DISPENSER)) || 
						(event.getClickedBlock().getType().equals(Material.DROPPER)) || 
						(event.getClickedBlock().getType().equals(Material.HOPPER)) || 
						(event.getClickedBlock().getType().equals(Material.BED)) || 
						(event.getClickedBlock().getType().equals(Material.ANVIL)) || 
						(event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE)) || 
						(event.getClickedBlock().getType().equals(Material.LEVER)) || (
								(event.getClickedBlock().getType().equals(Material.FURNACE)) && (!player.isOp()))) {
					event.setCancelled(true);
				}

				if (iteminhand.getTypeId() == Material.LEVER.getId()) {
					GamePlayer kp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
					if ((kp != null) && 
							(iteminhand.getTypeId() == Material.LEVER.getId()) && (event.getAction().toString().toLowerCase().contains("right"))) {
						if (Util.isBlockSolid(event.getClickedBlock())) {
							Location loc = event.getClickedBlock().getLocation().add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());
							if (loc.getBlock().getType().equals(Material.AIR)) {
								MCWarfare.getPlugin().getMapItems().add(new C4(kp, loc));
								int amtShears = InventoryHelper.amtItem(event.getPlayer().getInventory(), Material.SHEARS.getId());
								if (amtShears == 0)
									event.getPlayer().getInventory().addItem(new ItemStack[] { Util.namedItemStack(Material.SHEARS, 1, ChatColor.BLUE + "C4 Detonator", null, null) });
								event.getPlayer().updateInventory();
								return;
							}
							event.setCancelled(true);
							return;
						}

						event.setCancelled(true);
						return;
					}

				}

				if ((iteminhand.getTypeId() == Material.REDSTONE_TORCH_ON.getId()) && (event.getAction().toString().toLowerCase().contains("right"))) {
					GamePlayer kp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
					if (kp != null) {
						if (Util.isBlockSolid(event.getClickedBlock())) {
							Location loc = event.getClickedBlock().getLocation().add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());
							if ((loc.getBlock().getType().equals(Material.AIR)) && (loc.getBlock().getLocation().add(0.0D, -1.0D, 0.0D).getBlock().getTypeId() > 0) && (loc.getBlock().getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getTypeId() == 0)) {
								TacticalInsertion tacinsert = new TacticalInsertion(kp, loc);
								MCWarfare.getPlugin().getMapItems().add(tacinsert);
								event.getPlayer().updateInventory();
								return;
							}
							event.setCancelled(true);
							return;
						}

						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onCraft(CraftItemEvent event)
	{
		HumanEntity clicker = event.getWhoClicked();
		if (!clicker.isOp())
			event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		event.setCancelled(true);
		Player pl = event.getPlayer();
		if (pl != null) {
			GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(pl.getName());
			if (gplayer != null) {
				Arena arena = gplayer.getArena();
				if (arena.getArenaModifier().equals(ArenaModifier.CAPTURE_THE_FLAG))
					for (int i = 0; i < arena.getArenaItems().size(); i++) {
						ArenaItem item = (ArenaItem)arena.getArenaItems().get(i);
						if ((item instanceof ArenaItemFlagStand))
							try {
								if (((ArenaItemFlagStand)item).getFlag().getItem().getEntityId() == event.getItem().getEntityId())
									((ArenaItemFlagStand)item).getFlag().onPickUp(gplayer);
							}
						catch (Exception localException)
						{
						}
					}
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDeath(final EntityDeathEvent event) {
		event.getDrops().clear();
		event.setDroppedExp(0);

		if ((event.getEntity() instanceof Player))
			MCWarfare.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
				public void run() {
					EnumDifficulty difficulty = EnumDifficulty.valueOf(event.getEntity().getWorld().getDifficulty().name().toUpperCase());
					WorldType worldType = WorldType.getType(event.getEntity().getWorld().getWorldType().getName().toUpperCase());
					EnumGamemode gameMode = EnumGamemode.SURVIVAL;
					
					//PacketPlayOutRespawn packet = new PacketPlayOutRespawn(1, difficulty, worldType, gameMode);
					
					//((CraftPlayer)event.getEntity()).getHandle().playerConnection.sendPacket(packet);
				}
			});
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
	{
		try {
			if (!(event.getEntity() instanceof Player)) {
				return;
			}
			Player defender = (Player)event.getEntity();
			Entity att = event.getDamager();

			if ((att instanceof Wolf)) {
				ArrayList<?> items = (ArrayList<?>)MCWarfare.getPlugin().mapItems;
				for (int i = items.size() - 1; i >= 0; i--) {
					if ((items.get(i) instanceof AttackDog)) {
						AttackDog dog = (AttackDog)items.get(i);
						if (dog.dog.getEntityId() == att.getEntityId()) {
							GamePlayer attacked = MCWarfare.getPlugin().getArenaManager().getGamePlayer(defender.getName());
							if ((attacked != null) && (!attacked.getTeam().equals(dog.getOwner().getTeam()))) {
								MCWarfare.getPlugin().damagePlayer(defender, 16, DamageType.PLAYER, dog.getOwner().getPlayer());
							}
						}
					}
				}
			}
			if (((defender instanceof Wolf)) && ((att instanceof Player))) {
				ArrayList<?> items = (ArrayList<?>)MCWarfare.getPlugin().mapItems;
				for (int i = items.size() - 1; i >= 0; i--) {
					if ((items.get(i) instanceof AttackDog)) {
						AttackDog dog = (AttackDog)items.get(i);
						if (dog.dog.getEntityId() == defender.getEntityId()) {
							GamePlayer attacker = MCWarfare.getPlugin().getArenaManager().getGamePlayer(((Player)att).getName());
							if ((attacker == null) || (attacker.getTeam().equals(dog.getOwner().getTeam()))) {
								event.setCancelled(true);
								return;
							}
						}
					}
				}
			}
			if (((att instanceof Player)) || ((att instanceof Projectile))) {
				Player attacker = null;
				if ((event.getDamager() instanceof Projectile)) {
					Projectile arrow = (Projectile)att;
					attacker = (Player)arrow.getShooter();
				} else {
					attacker = (Player)att;
				}

				if (attacker != null) {
					boolean canDamage = MCWarfare.getPlugin().canDamagePlayer(attacker, defender);
					event.setCancelled(!canDamage);

					GamePlayer shootKP = MCWarfare.getPlugin().getArenaManager().getGamePlayer(attacker.getName());
					GamePlayer defendKP = MCWarfare.getPlugin().getArenaManager().getGamePlayer(defender.getName());
					if ((!event.isCancelled()) && (event.getDamage() < 1000.0D))
						shootKP.getArena().onDamage(event, shootKP, defendKP);
				}
			}
		}
		catch (Exception localException)
		{
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onTabComplete(PlayerChatTabCompleteEvent event) {
		if (event.getChatMessage().equals("/") || event.getChatMessage().equals("/?")) {
			event.getTabCompletions().clear();
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		String[] split = event.getMessage().split(" ");
		split[0] = split[0].substring(1);
		String label = split[0];
		String[] args = new String[split.length - 1];
		for (int i = 1; i < split.length; i++) {
			args[(i - 1)] = split[i];
		}

		if ((label.equalsIgnoreCase("?")) || (label.equalsIgnoreCase("me"))) {
			event.setCancelled(true);
			return;
		}

		if ((label.equalsIgnoreCase("plugins")) || (label.equalsIgnoreCase("pl"))) {
			event.setCancelled(true);
			ArrayList<String> plugins = new ArrayList<String>();
			plugins.add("MCWarfare");
			plugins.add("PVPGunPlus");
			plugins.add("NoCheatPlus");
			plugins.add("WorldEdit");
			String str = "Plugins (" + plugins.size() + ")";
			for (int i = 0; i < plugins.size(); i++) {
				str = str + " " + ChatColor.GREEN + (String)plugins.get(i);
				if (i < plugins.size() - 1) {
					str = str + ChatColor.WHITE + ",";
				}
			}
			player.sendMessage(str);
		}

		if (label.equalsIgnoreCase("war")) {
			event.setCancelled(true);

			if (args[0].equals("leave")) {
				if (player.isOp()) {
					MCWarfare.getPlugin().getArenaManager().leave(player);
					player.sendMessage("You have left mcwar");
				} else {
					player.sendMessage("You can no longer do this");
				}
			}

			if (args[0].equals("join")) {
				if (player.isOp()) {
					if (MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName()) == null) {
						player.sendMessage("You have joined mcwar");
						MCWarfare.getPlugin().getArenaManager().join(player);
					} else {
						player.sendMessage("You cannot join war, when you're already in war");
					}
				}
				else player.sendMessage("You can no longer do this");

			}

			if (args[0].equals("buy")) {
				GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
				ChestExplorer.openWarBuy(gplayer);
			}
			if (args[0].equals("killfeed")) {
				GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
				if (gplayer != null) {
					gplayer.hasKillfeedEnabled = !gplayer.hasKillfeedEnabled;
					if (gplayer.hasKillfeedEnabled)
						player.sendMessage("Killfeed is now enabled");
					else
						player.sendMessage("Killfeed has been disabled");
				}
			}
			if (args[0].equals("map")) {
				event.setCancelled(true);
				if ((args[1].equals("create")) && (player.isOp())) {
					try {
						String arenaName = args[2];
						ArenaCreator creator = new ArenaCreator(player, arenaName);
						MCWarfare.getPlugin().getArenaManager().startMakingArena(creator);
					} catch (Exception localException1) {
					}
				}
				else {
					ArenaCreator creator = MCWarfare.getPlugin().getArenaManager().getArenaMaker(player);
					creator.doCommand(args);
				}
			}
		}

		if (label.equalsIgnoreCase("kill")) {
			event.setCancelled(true);
			GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
			if (gplayer != null) {
				gplayer.lastDamager = null;
				gplayer.suicideTicks = 7;
				gplayer.getPlayer().setHealth(0.0D);
				//gplayer.getArena().onDeath(gplayer);
			} else {
				player.setHealth(0.0D);
			}
		}

		if (label.equalsIgnoreCase("help")) {
			event.setCancelled(true);
			player.sendMessage("MCWar help:");
			player.sendMessage(ChatColor.GRAY + "/war buy " + ChatColor.WHITE + "opens up a 1-round buyable items menu");
			player.sendMessage(ChatColor.GRAY + "/war killfeed " + ChatColor.WHITE + "to enable/disable killfeeds");
			player.sendMessage(ChatColor.GRAY + "/spawn " + ChatColor.WHITE + "teleports you to the spawn");
			player.sendMessage(ChatColor.GRAY + "/texture " + ChatColor.WHITE + "gives you a link to our texturepack");
			player.sendMessage(ChatColor.GRAY + "/server " + ChatColor.WHITE + "lets you know which server you're in");
			player.sendMessage(ChatColor.GRAY + "/profile {name} " + ChatColor.WHITE + "gives you information on a specific MCWar user");
			player.sendMessage(ChatColor.GRAY + "/clan " + ChatColor.WHITE + "clan help");
		}

		if (label.equalsIgnoreCase("rules")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "1) NO CHEATING, GLITCHING, SPAMMING");
			player.sendMessage(ChatColor.RED + "2) NO EXPLOITING");
			player.sendMessage(ChatColor.RED + "3) NO ARGUING WITH MODS OR ADMINS");
		}

		if (label.equalsIgnoreCase("kick")) {
			event.setCancelled(true);
			GamePlayer kicker = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
			if ((kicker != null) && ((kicker.getProfile().hasPermission("mod")) || (kicker.getProfile().hasPermission("smod")))) {
				int arglen = args.length;
				String reason = "";
				for (int i = 1; i < arglen; i++) {
					reason = reason + args[i] + " ";
				}
				Player pl = Util.MatchPlayer(args[0]);
				if (pl != null) {
					pl.kickPlayer("Kicked by: " + ChatColor.RED + player.getName() + ChatColor.WHITE + "\n '" + reason + "'");
				}
			}
		}

		if (label.equalsIgnoreCase("vote")) {
			event.setCancelled(true);
			GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getPlayer().getName());
			if (gplayer != null) {
				if (gplayer.hasVoted) {
					gplayer.getPlayer().sendMessage(ChatColor.RED + "You have already voted");
					return;
				}
				try {
					int vote = Integer.parseInt(args[0]) - 1;
					gplayer.getArena().doVote(vote);
					gplayer.getPlayer().sendMessage(ChatColor.GREEN + "You have voted for " + ChatColor.AQUA + ((VoteMap)gplayer.getArena().voteMaps.get(vote)).getArenaName());
					gplayer.hasVoted = true;
				} catch (Exception e) {
					gplayer.getPlayer().sendMessage(ChatColor.RED + "ERROR VOTING");
				}
			}
		}

		if (label.equalsIgnoreCase("spawn")) {
			event.setCancelled(true);
			MCWarfare.getPlugin().getArenaManager().leave(player);
			Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable()
			{
				public void run() {
					MCWarfare.getPlugin().getArenaManager().join(player);
				}
			}
			, 10L);
		}

		if (label.equalsIgnoreCase("texture")) {
			event.setCancelled(true);

			player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "SINCE MINECRAFT 1.6 DOES NOT ALLOW FOR AUTO DOWNLOADING RESOURCEPACKS, YOU CAN FIND OUR TEXTUREPACK AT: " + ChatColor.RESET + ChatColor.RED + "http://texture.mc-war.com/");
		}

		if (label.equalsIgnoreCase("server")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.GREEN + "You are in server #" + ChatColor.WHITE + MCWarfare.getPlugin().getServerNumber());
		}
		
		if (label.equalsIgnoreCase("save")) {
			event.setCancelled(true);
			if (player.isOp()) {
				GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
				if (gplayer != null) {
					gplayer.save();
				}
			}
		}

		if (label.equalsIgnoreCase("dump")) {
			event.setCancelled(true);
			GamePlayer gp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
			if (gp != null) {
				gp.getProfile().dumpClass();
			}

		}

		if (label.equalsIgnoreCase("clan")) {
			event.setCancelled(true);
			try {
				if (args.length == 0) {
					player.sendMessage(ChatColor.GREEN + "Clan help");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan create {name} " + ChatColor.WHITE + " to create a clan " + ChatColor.YELLOW + " [2000] credits!");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan invite {name} " + ChatColor.WHITE + " to invite a player");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan join {name} " + ChatColor.WHITE + " to join a clan");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan info {name} " + ChatColor.WHITE + " to view your clan info");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan leave " + ChatColor.WHITE + " to leave your clan");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan mod {name} " + ChatColor.WHITE + " to mod/demote a user in the clan");
				} else {
					GamePlayer kp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
					if (kp != null && kp.sucessfullyLoaded()) {
						if (args[0].equals("create")) {
							if (kp.getClan() != null) {
								kp.getPlayer().sendMessage(ChatColor.RED + "Leave your clan first!");
								return;
							}
							if (kp.getProfile().getLevel() > 30) {
								if (kp.getProfile().getCredits() >= 2000) {
									String map = args[1].replaceAll("[^\\p{Alpha}\\p{Digit}]+", "");
									if (map.equals(args[1])) {
										if (map.length() > 4) {
											map = map.substring(0, 4);
										}
										map.replace(" ", "");

										Clan temp = MCWarfare.getPlugin().getClanByTag(map);
										if ((temp == null) || ((temp != null) && (temp.getMembers().size() == 0))) {
											if (temp == null)
												temp = MCWarfare.getPlugin().loadClan(map);
											temp.join(kp.getPlayer().getName());
											temp.setOwner(kp.getPlayer().getName());
											kp.setClan(temp);
											kp.getProfile().addCredits(-1000);
											temp.save();
										} else {
											kp.getPlayer().sendMessage(ChatColor.RED + "THIS CLAN ALREAY EXISTS");
										}
									} else {
										kp.getPlayer().sendMessage(ChatColor.RED + "THIS NAME CANNOT BE USED");
									}
								} else {
									kp.getPlayer().sendMessage(ChatColor.RED + "You need at least 1000 credits!");
								}
							}
							else kp.getPlayer().sendMessage(ChatColor.RED + "You need to be level 31+ to make a clan!");
						} else if (args[0].equals("invite")) {
							if (kp.getClan() != null)
								if ((kp.getClan().getOwner().equals(kp.getPlayer().getName())) || (kp.getClan().hasModerator(kp.getPlayer().getName()))) {
									Player pln = Util.MatchPlayer(args[1]);
									if (pln != null) {
										GamePlayer np = MCWarfare.getPlugin().getArenaManager().getGamePlayer(pln.getName());
										if (np != null) {
											np.inviteToClan(kp.getClan());
											kp.getPlayer().sendMessage("You have invited " + np.getPlayer().getName() + " to your clan");
										}
									}
								} else {
									kp.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to invite players");
								}
						} else if (args[0].equals("kick")) {
							if (kp.getClan() != null)
								if ((kp.getClan().getOwner().equals(kp.getPlayer().getName())) || (kp.getClan().hasModerator(kp.getPlayer().getName()))) {
									Player pln = Util.MatchPlayer(args[1]);
									if (pln != null) {
										GamePlayer np = MCWarfare.getPlugin().getArenaManager().getGamePlayer(pln.getName());
										if ((np != null) && (np.getClan().equals(kp.getClan())) && (!kp.getClan().getOwner().equals(pln.getName()))) {
											np.getPlayer().sendMessage(ChatColor.RED + "You have been kicked from your clan");
											np.getClan().leave(np.getPlayer().getName());
											np.setClan(null);
										}
									}
								} else {
									kp.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to invite players");
								}
						} else if (args[0].equals("mod")) {
							if (kp.getClan() != null)
								if (kp.getClan().getOwner().equals(kp.getPlayer().getName())) {
									if (args.length >= 2) {
										String check = args[1];
										Player pln = Util.MatchPlayer(args[1]);
										if (pln != null)
											check = pln.getName();
										
										if (kp.getClan().hasMember(check)) {
											if (kp.getClan().hasModerator(check)) {
												kp.getClan().demote(check);
												kp.getPlayer().sendMessage("Player: " + ChatColor.GREEN + check + ChatColor.WHITE + " is no longer a moderator");
												if (pln != null)
													pln.sendMessage(ChatColor.LIGHT_PURPLE + "You are no longer a clan moderator :(");
											}else{
												kp.getClan().promote(check);
												kp.getPlayer().sendMessage("Player: " + ChatColor.GREEN + check + ChatColor.WHITE + " is now a moderator");
												if (pln != null)
													pln.sendMessage(ChatColor.LIGHT_PURPLE + "You are now a clan moderator!");
											}
										}
									}
								}
								else
								{
									kp.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to invite players");
								}
						} else if (args[0].equals("setowner")) {
							if (kp.getClan() != null)
								if (kp.getClan().getOwner().equals(kp.getPlayer().getName())) {
									Player pln = Util.MatchPlayer(args[1]);
									if (pln != null) {
										GamePlayer np = MCWarfare.getPlugin().getArenaManager().getGamePlayer(pln.getName());
										if ((np != null) && (np.getClan().equals(kp.getClan()))) {
											kp.getClan().setOwner(np.getPlayer().getName());
											np.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You are now the owner of " + ChatColor.YELLOW + kp.getClan().getName());
											kp.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You are no longer the owner of " + ChatColor.YELLOW + kp.getClan().getName());
										}
									}
								}
								else {
									kp.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to invite players");
								}
						} else if (args[0].equals("info")) {
							if (args.length == 1) {
								Clan c = kp.getClan();
								if (c != null)
									kp.getPlayer().sendMessage("You are in clan " + c.getName());
								else
									kp.getPlayer().sendMessage("You are not in a clan!");
							} else {
								Clan c = MCWarfare.getPlugin().getClanByTag(args[1]);
								if (c != null) {
									kp.getPlayer().sendMessage("Clan " + c.getName());
									kp.getPlayer().sendMessage("Owned by " + c.getOwner());
									kp.getPlayer().sendMessage("Members: " + c.getMembers().size());
								} else {
									kp.getPlayer().sendMessage("Clan does not exist");
								}
							}
						}
						else if (args[0].equals("join")) {
							if (kp.getClan() == null) {
								if (kp.clanInviteTo.length() > 0 && kp.sucessfullyLoaded()) {
									Clan clan = MCWarfare.getPlugin().getClanByTag(kp.clanInviteTo);
									if (clan != null) {
										clan.join(kp.getPlayer().getName());
										kp.setClan(clan);
									}
								} else {
									kp.getPlayer().sendMessage(ChatColor.RED + "You are not invited to this clan!");
								}
							}
							else kp.getPlayer().sendMessage(ChatColor.RED + "Leave your current clan first!");
						}
						else if (args[0].equals("leave")) {
							Clan clan = kp.getClan();
							if (clan != null) {
								clan.leave(kp.getPlayer().getName());
								kp.getPlayer().sendMessage(ChatColor.RED + "LEFT CLAN!");
								kp.setClan(null);
							}
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}