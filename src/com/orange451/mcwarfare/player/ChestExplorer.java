package com.orange451.mcwarfare.player;

import com.orange451.chestAPI.ChestAPI;
import com.orange451.chestAPI.ChestItemListener;
import com.orange451.chestAPI.ChestMenu;
import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.Arena;
import com.orange451.mcwarfare.arena.ArenaType;
import com.orange451.mcwarfare.arena.BuyableItem;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.util.Util;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestExplorer {
	public static void openChestMenu(final GamePlayer player) {
		ChestAPI.Initialise();

		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);

		if (!player.getArena().getArenaType().equals(ArenaType.SPAWN)) {
			openClassSelection(player);
			return;
		}

		//Class loadout
		ChestMenu menu = new ChestMenu("MCWar -Main", 2);
		addListenableItem(menu, Util.namedItemStack(Material.ANVIL, 1, ChatColor.GREEN + "Class Loadout", "" + ChatColor.GRAY + ChatColor.ITALIC, "Manage your /class loadout"), 0, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openClassLoadout(player);
			}
		});
		
		//Weapon shop
		addListenableItem(menu, Util.namedItemStack(Material.CHEST, 1, ChatColor.GREEN + "Weapon shop", "" + ChatColor.GRAY + ChatColor.ITALIC, "Open up a list/of buyable guns"), 1, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openWeaponShop(player);
			}
		});
		
		//Killstreak menu
		addListenableItem(menu, Util.namedItemStack(Material.getMaterial(2256), 1, ChatColor.GREEN + "Killstreak menu", "" + ChatColor.GREEN + ChatColor.GRAY + ChatColor.ITALIC, "Manage your/killstreaks"), 2, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openKillstreakMenu(player);
			}
		});
		
		//war buy items
		addListenableItem(menu, Util.namedItemStack(Material.DIAMOND_CHESTPLATE, 1, ChatColor.GREEN + "Ingame store", "" + ChatColor.GREEN + ChatColor.GRAY + ChatColor.ITALIC, "Purchase 1-round/items"), 3, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openWarBuy(player);
			}
		});
		
		//war buy items
		addListenableItem(menu, Util.namedItemStack(Material.STORAGE_MINECART, 1, ChatColor.GREEN + "MCWar Crate", "" + ChatColor.GREEN + ChatColor.GRAY + ChatColor.ITALIC, "Crate inventory"), 3, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openCrateInventory(player);
			}
		});
		
		//Join round button
		final Arena spawn = MCWarfare.getPlugin().getArenaManager().getFirstArenaByType(ArenaType.SPAWN);
		if (spawn != null) {
			if (spawn.getToArena() != null && spawn.getToArena().canPlayerJoin(player) && spawn.getToArena().active) {
				addListenableItem(menu, Util.namedItemStack(Material.BOAT, 1, ChatColor.GREEN + "Join game!", "" + ChatColor.GREEN + ChatColor.GRAY + ChatColor.ITALIC, "Click here to/join the current/round"), 8, 1, new ChestItemListener(null) {
					public void ItemClicked() {
						//Check again, as the player could have left the menu open for awhile
						if (spawn.getToArena() != null && spawn.getToArena().canPlayerJoin(player)) {
							spawn.onLeave(player);
							spawn.getToArena().onJoin(player);
						}else{
							player.getPlayer().sendMessage("Sorry, you cannot join at this time");
						}
					}
				});
			}
		}
		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openCrateInventory(final GamePlayer player) {
		ChestMenu menu = new ChestMenu("Crate Inventory", 6);
		addBackButton(menu, player);
		
		for (int i = 0; i < player.getProfile().getChests(); i++) {
			ItemStack itm = new ItemStack(Material.STORAGE_MINECART, 1);
			ItemMeta imeta = itm.getItemMeta();
			imeta.setDisplayName(ChatColor.BLUE + "MCWar Crate");
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Mystery Box!");
			lore.add(Integer.toString(i));
			imeta.setLore(lore);
			itm.setItemMeta(imeta);

			addListenableItem(menu, itm, 9 + i, new ChestItemListener(null) {
				public void ItemClicked() {
					OpenCrate opener = new OpenCrate(player);
					final int taskId = MCWarfare.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(MCWarfare.getPlugin(), opener, 2L, 2L);
					opener.taskId = taskId;
				}
			});
		}
		
		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openWeaponShop(final GamePlayer player) {
		ChestMenu menu = new ChestMenu("Weapon Shop (money: " + player.getProfile().getCredits() + ")", 6);
		addBackButton(menu, player);

		int count = 0;
		for (int i = 0; i < MCWarfare.getPlugin().loadedGuns.size(); i++) {
			KitGun gun = (KitGun)MCWarfare.getPlugin().loadedGuns.get(i);
			String g = gun.name;
			boolean unlocked = ((KitGun)MCWarfare.getPlugin().loadedGuns.get(i)).isUnlocked(player.getProfile());
			if ((unlocked) && (!player.getProfile().hasGun(g))) {
				ItemStack itm = new ItemStack(((KitGun)MCWarfare.getPlugin().loadedGuns.get(i)).type, 1);
				ItemMeta imeta = itm.getItemMeta();
				imeta.setDisplayName(ChatColor.BLUE + g + ChatColor.GREEN + " $" + Integer.toString(((KitGun)MCWarfare.getPlugin().loadedGuns.get(i)).cost));
				ArrayList<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GRAY + gun.slot);
				lore.add(ChatColor.GRAY + gun.desc);
				imeta.setLore(lore);
				itm.setItemMeta(imeta);
				menu.setItem(9 + count, itm);

				addListenableItem(menu, itm, 9 + count, new ChestItemListener(null) {
					public void ItemClicked() {
						ItemStack itm = getAttachedItem();
						KitGun gun = MCWarfare.getPlugin().getGunManager().getGun(itm);
						if (gun != null) {
							int cost = gun.cost;
							if (player.getProfile().getCredits() >= cost) {
								player.getProfile().addCredits(-cost);
								player.getProfile().giveGun(gun.name);
								player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1.0F, 4.0F);
								ChestExplorer.openWeaponShop(player);
							}
						}
					}
				});
				count++;
			}
		}

		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openClassSelection(final GamePlayer player) {
		ChestMenu menu = new ChestMenu("Class Selection", 6);
		int classStart = 2;
		menu.setItem(classStart, Util.namedItemStack(Material.MAP, 1, ChatColor.RED + "Class name", null, null));
		menu.setItem(classStart + 1, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Primary", "" + ChatColor.GRAY, "Main Weapon of/this class"));
		menu.setItem(classStart + 2, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Secondary", "" + ChatColor.GRAY, "Secondary Weapon of/this class"));
		menu.setItem(classStart + 3, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Sword", "" + ChatColor.GRAY, "Sword of/this class"));
		menu.setItem(classStart + 4, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Lethal", "" + ChatColor.GRAY, "Lethal grenade/of this class"));
		menu.setItem(classStart + 5, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Tactical", "" + ChatColor.GRAY, "Tactical grenade/of this class"));
		menu.setItem(classStart + 6, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Perk", "" + ChatColor.GRAY, "Perk for/this class"));
		menu.setItem(45, Util.namedItemStack(Material.MAP, 1, "" + ChatColor.RED + "INFORMATION", "" + ChatColor.GRAY, "Click the class you/want to play as"));

		ArrayList<GameClass> pclasses = player.getProfile().getLoadedClasses();
		for (int i = 0; i < pclasses.size(); i++) {
			int start = classStart + 9 * (i + 1);
			final int classPointer = i;
			String strClass = "CLASS " + (classPointer + 1);
			
			if (classPointer == player.getProfile().getCurrentClassPointer())
				menu.setItem(start - 1, Util.namedItemStack(Material.BOAT, 1, "Current Class", null, null));
			
			GameClass pclass = (GameClass)pclasses.get(i);
			KitGun primary = MCWarfare.getPlugin().getGunManager().getGun(pclass.getPrimary());
			KitGun secondary = MCWarfare.getPlugin().getGunManager().getGun(pclass.getSecondary());
			KitGun lethal = MCWarfare.getPlugin().getGunManager().getGun(pclass.getLethal());
			KitGun tactical = MCWarfare.getPlugin().getGunManager().getGun(pclass.getTactical());

			//Setup Perk item
			String perk = pclass.getPerk();
			ItemStack perkItem = Util.namedItemStack(Material.SNOW_BALL, 1, "" + ChatColor.WHITE + ChatColor.ITALIC + "No perk", null, null);
			if ((perk != null) && (!perk.equals("")))
				perkItem = MCWarfare.getPlugin().matchPerk(perk).getPerkAsItem();
			perkItem = Util.appendItemStackLore(perkItem, strClass);

			//Setup class book (label)
			ItemStack book = Util.namedItemStack(Material.BOOK, 1, "" + ChatColor.ITALIC + ChatColor.GRAY + strClass, null, null);
			addListenableItem(menu, book, start, new ChestItemListener(null) {
				public void ItemClicked() {
					player.getProfile().setCurrentClassPointer(classPointer);
					player.getPlayer().closeInventory();
					if (player.getArena().getTicksSinceStart() <= 9)
						player.forceRespawn();
					else
						player.getPlayer().sendMessage(ChatColor.ITALIC + "You will spawn as this class next life");
				}
			});
			menu.setItem(start + 1, primary.getItemStack());
			menu.setItem(start + 2, secondary.getItemStack());
			menu.setItem(start + 3, pclass.getKnife());

			ItemStack lth = pclass.getLethal();
			if (lethal != null)
				lth = lethal.getItemStack();
			menu.setItem(start + 4, lth);

			ItemStack tac = pclass.getTactical();
			if (tactical != null)
				tac = tactical.getItemStack();
			menu.setItem(start + 5, tac);

			menu.setItem(start + 6, perkItem);
		}

		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openClassLoadout(final GamePlayer player) {
		try{
			ChestMenu menu = new ChestMenu("Class Loadout", 6);
			addBackButton(menu, player);
	
			int classStart = 2;
			menu.setItem(classStart, Util.namedItemStack(Material.MAP, 1, ChatColor.RED + "Class name", null, null));
			menu.setItem(classStart + 1, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Primary", "" + ChatColor.GRAY, "Main Weapon of/this class"));
			menu.setItem(classStart + 2, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Secondary", "" + ChatColor.GRAY, "Secondary Weapon of/this class"));
			menu.setItem(classStart + 3, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Sword", "" + ChatColor.GRAY, "Sword of/this class"));
			menu.setItem(classStart + 4, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Lethal", "" + ChatColor.GRAY, "Lethal grenade/of this class"));
			menu.setItem(classStart + 5, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Tactical", "" + ChatColor.GRAY, "Tactical grenade/of this class"));
			menu.setItem(classStart + 6, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Perk", "" + ChatColor.GRAY, "Perk for/this class"));
			menu.setItem(45, Util.namedItemStack(Material.MAP, 1, "" + ChatColor.RED + "INFORMATION", "" + ChatColor.GRAY, "Click the items under/the arrows to change/them in your class/ /You get a free class/after level 10"));
	
			int maxClass = player.getProfile().getMaxClasses();
			int amtClasses = player.getProfile().getLoadedClasses().size();
			if (maxClass > amtClasses) {
				addListenableItem(menu, Util.namedItemStack(Material.BUCKET, maxClass - amtClasses, "" + ChatColor.GREEN + "Create new class", null, null), 36, new ChestItemListener(null) {
					public void ItemClicked() {
						player.getProfile().createNewClass();
						ChestExplorer.openClassLoadout(player);
					}
				});
			}
	
			ArrayList<GameClass> pclasses = player.getProfile().getLoadedClasses();
			for (int i = 0; i < pclasses.size(); i++) {
				int start = classStart + 9 * (i + 1);
				final int classPointer = i;
				String strClass = "CLASS " + (classPointer + 1);
				GameClass pclass = (GameClass)pclasses.get(i);
				KitGun primary = MCWarfare.getPlugin().getGunManager().getGun(pclass.getPrimary());
				KitGun secondary = MCWarfare.getPlugin().getGunManager().getGun(pclass.getSecondary());
				KitGun lethal = MCWarfare.getPlugin().getGunManager().getGun(pclass.getLethal());
				KitGun tactical = MCWarfare.getPlugin().getGunManager().getGun(pclass.getTactical());
				
				if (classPointer == player.getProfile().getCurrentClassPointer())
					menu.setItem(start - 1, Util.namedItemStack(Material.BOAT, 1, "Current Class", null, null));
				
				if (primary == null || primary.getItemStack() == null) {
					pclass.reset();
					
					primary = MCWarfare.getPlugin().getGunManager().getGun(pclass.getPrimary());
					secondary = MCWarfare.getPlugin().getGunManager().getGun(pclass.getSecondary());
					lethal = MCWarfare.getPlugin().getGunManager().getGun(pclass.getLethal());
					tactical = MCWarfare.getPlugin().getGunManager().getGun(pclass.getTactical());
				}
	
				//Setup class book (label)
				String bookDescription = "Loadout for class " + (classPointer + 1);
				if (player.getProfile().getAmountClasses() - 1 < classPointer)
					bookDescription = "Buy this class at/www.multiplayerservers.com/";
				ItemStack book = Util.namedItemStack(Material.BOOK, 1, "" + ChatColor.ITALIC + ChatColor.GRAY + strClass, "" + ChatColor.DARK_GRAY, bookDescription);
				addListenableItem(menu, book, start, new ChestItemListener(null) {
					public void ItemClicked() {
						player.getProfile().setCurrentClassPointer(classPointer);
						player.getPlayer().sendMessage(ChatColor.ITALIC + "You will spawn as this class next game");
						openClassLoadout(player);
					}
				});
	
				strClass = "" + ChatColor.ITALIC + ChatColor.DARK_GRAY + strClass;
				
				addListenableItem(menu, Util.appendItemStackLore(primary.getItemStack(), strClass), start + 1, new ChestItemListener(null) {
					public void ItemClicked() {
						ChestExplorer.openWeaponChooser(player, "primary", classPointer);
					}
				});
				addListenableItem(menu, Util.appendItemStackLore(secondary.getItemStack(), strClass), start + 2, new ChestItemListener(null) {
					public void ItemClicked() {
						ChestExplorer.openWeaponChooser(player, "secondary", classPointer);
					}
				});
				addListenableItem(menu, Util.appendItemStackLore(pclass.getKnife(), strClass), start + 3, new ChestItemListener(null) {
					public void ItemClicked() {
						ChestExplorer.openKnifeChooser(player, classPointer);
					}
				});
				ItemStack lth = pclass.getLethal();
				if (lethal != null)
					lth = lethal.getItemStack();
				lth = Util.appendItemStackLore(lth, strClass);
				addListenableItem(menu, lth, start + 4, new ChestItemListener(null) {
					public void ItemClicked() {
						ChestExplorer.openWeaponChooser(player, "grenade_lethal", classPointer);
					}
				});
				
				ItemStack tac = pclass.getTactical();
				if (tactical != null)
					tac = tactical.getItemStack();
				tac = Util.appendItemStackLore(tac, strClass);
				addListenableItem(menu, tac, start + 5, new ChestItemListener(null) {
					public void ItemClicked() {
						ChestExplorer.openWeaponChooser(player, "grenade_tactical", classPointer);
					}
				});
				
				String perk = pclass.getPerk();
				ItemStack perkItem = Util.namedItemStack(Material.SNOW_BALL, 1, "" + ChatColor.WHITE + ChatColor.ITALIC + "No perk", null, null);
				if ((perk != null) && (!perk.equals("")))
					perkItem = MCWarfare.getPlugin().matchPerk(perk).getPerkAsItem();
				perkItem = Util.appendItemStackLore(perkItem, strClass);
				addListenableItem(menu, perkItem, start + 6, new ChestItemListener(null) {
					public void ItemClicked() {
						ChestExplorer.openPerkChooser(player, classPointer);
					}
				});
			}
	
			ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void openPerkChooser(final GamePlayer player, final int mclass) {
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CLICK, 1.0F, 4.0F);
		ChestMenu menu = new ChestMenu("Class Loadout -Perk", 4);
		addListenableItem(menu, Util.namedItemStack(Material.LAVA_BUCKET, 1, ChatColor.ITALIC + "Go back!", null, null), 0, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openClassLoadout(player);
			}
		});
		int start = 9;
		final ArrayList<GamePerk> perks = player.getProfile().getOwnedPerks();
		for (int i = 0; i < perks.size(); i++) {
			final int perkPointer = i;
			ItemStack itm = ((GamePerk)perks.get(i)).getPerkAsItem();
			addListenableItem(menu, itm, start + i, new ChestItemListener(null) {
				public void ItemClicked() {
					((GameClass)player.getProfile().getLoadedClasses().get(mclass)).setPerk(((GamePerk)perks.get(perkPointer)).getName());
					player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1.0F, 4.0F);
					ChestExplorer.openClassLoadout(player);
				}
			});
		}

		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openKnifeChooser(final GamePlayer player, final int mclass) {
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CLICK, 1.0F, 4.0F);
		ChestMenu menu = new ChestMenu("Class Loadout -Knife", 4);
		addListenableItem(menu, Util.namedItemStack(Material.LAVA_BUCKET, 1, ChatColor.ITALIC + "Go back!", null, null), 0, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openClassLoadout(player);
			}
		});
		ItemStack itm = new ItemStack(Material.IRON_SWORD);
		addListenableItem(menu, itm, 9, new ChestItemListener(null) {
			public void ItemClicked() {
				((GameClass)player.getProfile().getLoadedClasses().get(mclass)).setKnife(getAttachedItem().getTypeId(), 1, (byte)0);
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1.0F, 4.0F);
				ChestExplorer.openClassLoadout(player);
			}
		});
		if (player.getProfile().hasPermission("superknife")) {
			itm = new ItemStack(Material.DIAMOND_SWORD);
			addListenableItem(menu, itm, 10, new ChestItemListener(null) {
				public void ItemClicked() {
					((GameClass)player.getProfile().getLoadedClasses().get(mclass)).setKnife(getAttachedItem().getTypeId(), 1, (byte)0);
					player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1.0F, 4.0F);
					ChestExplorer.openClassLoadout(player);
				}
			});
		}

		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openWeaponChooser(final GamePlayer player, final String gunType, final int mclass) {
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CLICK, 1.0F, 4.0F);
		ChestMenu menu = new ChestMenu("Class Loadout -" + gunType, 6);
		addListenableItem(menu, Util.namedItemStack(Material.LAVA_BUCKET, 1, ChatColor.ITALIC + "Go back!", null, null), 0, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openClassLoadout(player);
			}
		});
		int start = 9;
		int pointer = 0;
		ArrayList<String> guns = player.getProfile().getOwnedGuns();
		for (int i = 0; i < guns.size(); i++) {
			String temp = (String)guns.get(i);
			final KitGun gun = MCWarfare.getPlugin().getGunManager().getGun(temp);
			if ((gun != null) && (gun.slot.equalsIgnoreCase(gunType))) {
				addListenableItem(menu, gun.getItemStack(), start + pointer, new ChestItemListener(null) {
					public void ItemClicked() {
						if  (player.sucessfullyLoaded()) {
							GameClass tclass = (GameClass)player.getProfile().getLoadedClasses().get(mclass);
							if (gunType.equalsIgnoreCase("primary"))
								tclass.setPrimary(gun.type, 1, (byte)0);
							else if (gunType.equalsIgnoreCase("secondary"))
								tclass.setSecondary(gun.type, 1, (byte)0);
							else if (gunType.equalsIgnoreCase("grenade_tactical"))
								tclass.setTactical(gun.type, 1, (byte)0);
							else if (gunType.equalsIgnoreCase("grenade_lethal")) {
								tclass.setLethal(gun.type, 1, (byte)0);
							}
							player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1.0F, 4.0F);
							ChestExplorer.openClassLoadout(player);
						}else{
							player.getPlayer().sendMessage("Your profile is loading, please wait!");
						}
					}
				});
				pointer++;
			}
		}

		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openKillstreakMenu(final GamePlayer player) {
		ChestAPI.Initialise();
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CLICK, 1.0F, 4.0F);

		ChestMenu menu = new ChestMenu("Killstreak Menu", 2);
		addBackButton(menu, player);

		menu.setItem(6, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Killstreak", "" + ChatColor.GRAY, "Tier I killstreak"));
		menu.setItem(7, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Killstreak", "" + ChatColor.GRAY, "Tier II killstreak"));
		menu.setItem(8, Util.namedItemStack(Material.WATER_BUCKET, 1, "" + ChatColor.RED + "Killstreak", "" + ChatColor.GRAY, "Tier III killstreak"));

		String ks1 = player.getProfile().killstreak1;
		String ks2 = player.getProfile().killstreak2;
		String ks3 = player.getProfile().killstreak3;
		GameKillstreak killstreak1 = MCWarfare.getPlugin().matchKillstreak(ks1);
		GameKillstreak killstreak2 = MCWarfare.getPlugin().matchKillstreak(ks2);
		GameKillstreak killstreak3 = MCWarfare.getPlugin().matchKillstreak(ks3);

		ItemStack item_killstreak1 = Util.namedItemStack(Material.SNOW_BALL, 1, "No Killstreak Tier I", null, null);
		ItemStack item_killstreak2 = Util.namedItemStack(Material.SNOW_BALL, 1, "No Killstreak Tier II", null, null);
		ItemStack item_killstreak3 = Util.namedItemStack(Material.SNOW_BALL, 1, "No Killstreak Tier III", null, null);

		if (killstreak1 != null)
			item_killstreak1 = killstreak1.getKillstreakAsItem();
		if (killstreak2 != null)
			item_killstreak2 = killstreak2.getKillstreakAsItem();
		if (killstreak3 != null) {
			item_killstreak3 = killstreak3.getKillstreakAsItem();
		}
		menu.setItem(15, item_killstreak1);
		menu.setItem(16, item_killstreak2);
		menu.setItem(17, item_killstreak3);

		addListenableItem(menu, item_killstreak1, 15, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openKillstreakModifier(player, 1);
			}
		});
		addListenableItem(menu, item_killstreak2, 16, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openKillstreakModifier(player, 2);
			}
		});
		addListenableItem(menu, item_killstreak3, 17, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openKillstreakModifier(player, 3);
			}
		});
		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openKillstreakModifier(final GamePlayer player, final int tier) {
		ChestAPI.Initialise();
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CLICK, 1.0F, 4.0F);

		ChestMenu menu = new ChestMenu("Killstreak Selection (Tier " + tier + ")", 2);
		addListenableItem(menu, Util.namedItemStack(Material.LAVA_BUCKET, 1, ChatColor.ITALIC + "Go back!", null, null), 0, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openKillstreakMenu(player);
			}
		});
		int pointer = 0;
		ArrayList<String> killstreaks = player.getProfile().getOwnedKillstreaks();
		for (int i = 0; i < killstreaks.size(); i++) {
			final GameKillstreak killstreak = MCWarfare.getPlugin().matchKillstreak((String)killstreaks.get(i));
			if ((killstreak != null) && 
					(killstreak.getTier() == tier)) {
				ItemStack itm = killstreak.getKillstreakAsItem();
				addListenableItem(menu, itm, pointer + 9, new ChestItemListener(null) {
					public void ItemClicked() {
						if (tier == 1)
							player.getProfile().killstreak1 = killstreak.getName();
						else if (tier == 2)
							player.getProfile().killstreak2 = killstreak.getName();
						else {
							player.getProfile().killstreak3 = killstreak.getName();
						}
						ChestExplorer.openKillstreakMenu(player);
					}
				});
				pointer++;
			}

		}

		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}

	public static void openWarBuy(final GamePlayer player) {
		try {
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CLICK, 1.0F, 4.0F);
			ChestAPI.Initialise();

			ChestMenu menu = new ChestMenu("Per-Round Items (money: " + player.getProfile().getCredits() + ")", 2);
			addBackButton(menu, player);

			ArrayList<BuyableItem> items = MCWarfare.getPlugin().buyableItems;
			int pointer = 0;
			for (int i = 0; i < items.size(); i++) {
				ItemStack itm = ((BuyableItem)items.get(i)).getAsItem();

				boolean found = false;
				for (int ii = 0; ii < player.boughtItems.size(); ii++) {
					BuyableItem temp = (BuyableItem)player.boughtItems.get(ii);
					if (temp.mat.equals(itm.getType())) {
						found = true;
					}
				}
				if (!found) {
					ItemMeta meta = itm.getItemMeta();
					ArrayList<String> str = new ArrayList<String>();
					str.add("" + ChatColor.GRAY + ChatColor.ITALIC + "Lasts 1 round");
					meta.setLore(str);
					itm.setItemMeta(meta);
					menu.setItem(pointer, 1, itm);
					menu.addItemListener(new ChestItemListener(itm) {
						public void ItemClicked() {
							ItemStack clicked = getAttachedItem();
							String name = clicked.getItemMeta().getDisplayName();
							String[] temp = name.split("\\$");
							if (temp.length == 2)
								try {
									int amount = Integer.parseInt(temp[1]);
									if (player.getProfile().getCredits() >= amount) {
										BuyableItem buyable = MCWarfare.getPlugin().matchBuyableItem(clicked.getType());
										if (buyable != null) {
											player.boughtItems.add(buyable);
											player.getPlayer().closeInventory();
											player.getProfile().addCredits(-amount);
											player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.CLICK, 1.0F, 4.0F);
											ChestExplorer.openWarBuy(player);
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
						}
					});
					pointer++;
				}
			}

			ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addBackButton(ChestMenu menu, final GamePlayer player) {
		addListenableItem(menu, Util.namedItemStack(Material.LAVA_BUCKET, 1, ChatColor.ITALIC + "Go back!", null, null), 0, 0, new ChestItemListener(null) {
			public void ItemClicked() {
				ChestExplorer.openChestMenu(player);
			}
		});
	}

	private static void addListenableItem(ChestMenu menu, ItemStack item, int x, int y, ChestItemListener listener) {
		menu.setItem(x, y, item);
		listener.setAttachedTo(item);
		menu.addItemListener(listener);
	}

	private static void addListenableItem(ChestMenu menu, ItemStack item, int x, ChestItemListener listener) {
		menu.setItem(x, item);
		listener.setAttachedTo(item);
		menu.addItemListener(listener);
	}
}