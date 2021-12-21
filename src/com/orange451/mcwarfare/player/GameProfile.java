package com.orange451.mcwarfare.player;

import com.mysql.jdbc.PreparedStatement;
import com.orange451.mcwarfare.GunManager;
import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.player.sql.SQLController;
import com.orange451.mcwarfare.player.sql.StatMCWar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitScheduler;

public class GameProfile
{
	private int credits;
	private int xp;
	private int level;
	private int kills;
	private int deaths;
	private int creditsGained;
	private int classPointer;
	private int startXp;
	private int chests;
	private int chests_start;
	private Clan clan;
	private GameRank rank = new GameRank("Please wait...", "(Loading)", 0);
	private String playerName;
	private ArrayList<String> guns;
	private ArrayList<GameClass> classes;
	private ArrayList<String> permissions;
	private ArrayList<String> killstreaks;
	private ArrayList<GamePerk> perks;
	private ArrayList<String> gunsRemoved;
	private ArrayList<String> gunsAdded;
	private ArrayList<String> permissionsRemoved;
	private ArrayList<String> permissionsAdded;
	private int maxClasses = 1;
	private int addedClasses;
	public int xp_max;
	public boolean loaded = false;
	public boolean corrupted = false;
	
	private boolean changedClassLoadout = false;
	private boolean changedGuns = false;
	private boolean changedPerks = false;
	private boolean changedKillstreaks = false;
	private boolean changedLevel;
	private boolean changedCredits;
	private boolean changedXp;

	public String killstreak1 = "";
	public String killstreak2 = "";
	public String killstreak3 = "";

	public GameProfile(String playerName, boolean autoLoad) {
		this.playerName = playerName;
		
		if (autoLoad) {
			this.loadProfile(playerName);
		}else{
			this.reload();
		}
	}

	public void dumpClass() {
		MCWarfare.getPlugin().print("LOADED PLAYER PROFILE FOR: " + this.playerName);
		MCWarfare.getPlugin().print("kills: " + this.kills);
		MCWarfare.getPlugin().print("deaths: " + this.deaths);
		MCWarfare.getPlugin().print("credits: " + this.credits);
		MCWarfare.getPlugin().print("level: " + this.level);
		MCWarfare.getPlugin().print("xp: " + this.xp);
		for (int i = 0; i < this.classes.size(); i++) {
			MCWarfare.getPlugin().print("Reading class: #" + i);
			MCWarfare.getPlugin().print("  -Primary: " + ((GameClass)this.classes.get(i)).getPrimary().getType().toString());
			MCWarfare.getPlugin().print("  -Secondary: " + ((GameClass)this.classes.get(i)).getSecondary().getType().toString());
			MCWarfare.getPlugin().print("  -Lethal: " + ((GameClass)this.classes.get(i)).getLethal().getType().toString());
			MCWarfare.getPlugin().print("  -Tactical: " + ((GameClass)this.classes.get(i)).getTactical().getType().toString());
			MCWarfare.getPlugin().print("  -Knife: " + ((GameClass)this.classes.get(i)).getKnife().getType().toString());
			MCWarfare.getPlugin().print("  -Perk: " + ((GameClass)this.classes.get(i)).getPerk());
		}
		MCWarfare.getPlugin().print("Purchased Guns: (" + this.guns.size() + ")");
		for (int i = 0; i < this.guns.size(); i++) {
			MCWarfare.getPlugin().print("  -" + (String)this.guns.get(i));
		}
		MCWarfare.getPlugin().print("Reading killstreaks: (" + this.killstreaks.size() + ")");
		for (int i = 0; i < this.killstreaks.size(); i++) {
			MCWarfare.getPlugin().print("  -" + (String)this.killstreaks.get(i));
		}
		MCWarfare.getPlugin().print("Equipped killstreaks: ");
		MCWarfare.getPlugin().print("  -Killstreak 1: " + this.killstreak1);
		MCWarfare.getPlugin().print("  -Killstreak 2: " + this.killstreak2);
		MCWarfare.getPlugin().print("  -Killstreak 3: " + this.killstreak3);

		MCWarfare.getPlugin().print("Reading permissions: (" + this.permissions.size() + ")");
		for (int i = 0; i < this.permissions.size(); i++)
			MCWarfare.getPlugin().print("  -" + (String)this.permissions.get(i));
	}

	public Clan getClan() {
		return this.clan;
	}

	public void setClan(Clan temp) {
		this.clan = temp;
	}

	public boolean hasPermission(String perm) {
		if (!this.loaded || permissions == null || permissions.size() <= 0)
			return false;
		
		for (int i = 0; i < this.permissions.size(); i++) {
			if (((String)this.permissions.get(i)).toLowerCase().equals(perm.toLowerCase()))
				return true;
		}
		List<Player> players = Bukkit.matchPlayer(this.playerName);
		if (players.size() > 0) {
			Player player = (Player)players.get(0);
			if ((player != null) && (player.isOp()))
				return true;
		}
		return false;
	}

	public boolean hasGun(String s) {
		for (int i = 0; i < this.guns.size(); i++) {
			if (((String)this.guns.get(i)).toLowerCase().equals(s.toLowerCase()))
				return true;
		}
		return false;
	}

	public void givePermission(String perm) {
		this.permissions.add(perm);
		this.permissionsAdded.add(perm);
	}

	public void removePermission(String perm) {
		for (int i = this.permissions.size() - 1; i >= 0; i--)
			if (((String)this.permissions.get(i)).toLowerCase().equals(perm.toLowerCase()))
				this.permissions.remove(i);
		this.permissionsRemoved.add(perm);
	}

	public GameClass getCurrentClass() {
		if (!this.loaded || this.classes.size() <= 0)
			return null;
		return (GameClass)this.classes.get(this.classPointer);
	}

	public void setCurrentClassPointer(int i) {
		this.classPointer = i;
	}
	
	public int getCurrentClassPointer() {
		return this.classPointer;
	}
	
	public void loadProfile(Connection c, String name) {
		this.guns = new ArrayList<String>();
		this.permissions = new ArrayList<String>();
		this.classes = new ArrayList<GameClass>();
		this.gunsRemoved = new ArrayList<String>();
		this.gunsAdded = new ArrayList<String>();
		this.permissionsRemoved = new ArrayList<String>();
		this.permissionsAdded = new ArrayList<String>();
		this.killstreaks = new ArrayList<String>();
		this.perks = new ArrayList<GamePerk>();
		this.loaded = false;

		this.clan = MCWarfare.getPlugin().getUserClan(this.playerName);
		boolean loadSuccessful = false;
		try {
			String query = "SELECT * FROM `MCWarProfile` WHERE `user_name` = '" + name + "'";
			if (c != null) {
				Statement st = c.createStatement();
				ResultSet result = st.executeQuery(query);
				if (!result.first()) {
					blankProfile();
					this.loaded = true;
				} else {
					this.kills = Integer.parseInt(result.getString("user_kills"));
					this.deaths = Integer.parseInt(result.getString("user_deaths"));
					this.level = Integer.parseInt(result.getString("user_level"));
					this.xp = Integer.parseInt(result.getString("user_xp"));
					this.credits = Integer.parseInt(result.getString("user_credits"));
					
					
					try{
						this.chests = Integer.parseInt(result.getString("user_chests"));
					}catch(Exception e) {
						//
					}
					this.chests = 6;
					this.chests_start = this.chests;
					
					
					this.maxClasses = 1;
					try {
						this.maxClasses = Integer.parseInt(result.getString("max_classes"));
					}
					catch (Exception localException1) {
					}
					String parseGuns = result.getString("owned_guns");
					String parseClasses = result.getString("defined_classes");
					String parsePerms = result.getString("permissions");
					String parsePerks = result.getString("user_perks");
					String parseKillstreaks = result.getString("user_killstreaks");
					String parseKillstreaks_owned = result.getString("equipped_killstreaks");

					PARSESTRINGLIST(parseKillstreaks, this.killstreaks, "\\|");
					PARSESTRINGLIST(parseGuns, this.guns, "\\|");
					PARSESTRINGLIST(parsePerms, this.permissions, "\\|");

					parseKILLSTREAKS(parseKillstreaks_owned);
					PARSEPERKS(parsePerks);
					PARSECLASSES(parseClasses);

					this.startXp = this.xp;
					st.close();
					loadSuccessful = true;
					System.out.println("LOADED THE PROFILE!!!!!");
				}
			} else {
				MCWarfare.getPlugin().print("CANNOT LOAD PROFILE FOR " + name + " (No database connection)");
			}
		} catch (Exception e) {
			//e.printStackTrace();
			this.loaded = false;
		}

		this.rank = MCWarfare.getPlugin().matchRank(getLevel());

		if (MCWarfare.getPlugin().hasVoted(this.playerName)) {
			if (!hasGun("vote_grenade"))
				giveGun("vote_grenade");
		} else {
			removeGun("vote_grenade");
			for (int i = 0; i < this.classes.size(); i++) {
				GameClass pclass = (GameClass)this.classes.get(i);
				if (pclass.getLethal().getType().equals(Material.MAGMA_CREAM))
					pclass.setLethal(Material.SNOW_BALL.getId(), 1, (byte)0);
			}
		}
		
		for (int i = 0; i < this.classes.size(); i++) {
			classes.get(i).setLoaded();
		}
		
		
		if (loadSuccessful)
			this.loaded = true;
		if (!this.loaded)
			this.corrupted = true;
	}

	public void loadProfile(String name) {
		this.loadProfile(MCWarfare.getPlugin().getSQLDatabaseConnection(), name);
	}

	private void parseKILLSTREAKS(String parse) {
		if ((parse == null) || (parse.length() <= 4))
			return;
		String[] split = parse.split("\\|");
		if (split.length >= 1)
			this.killstreak1 = split[0];
		if (split.length >= 2)
			this.killstreak2 = split[1];
		if (split.length >= 3)
			this.killstreak3 = split[2];
	}

	private void PARSESTRINGLIST(String parse, ArrayList<String> toArray, String seperator) {
		if (parse == null)
			return;
		String[] entry = parse.split(seperator);
		for (int i = 0; i < entry.length; i++)
			toArray.add(entry[i]);
	}

	private void PARSEPERKS(String parsePerks) {
		if (parsePerks == null)
			return;
		String[] perm = parsePerks.split("\\|");
		for (int i = 0; i < perm.length; i++) {
			String perk = perm[i];
			ArrayList<?> loadedPerks = MCWarfare.getPlugin().serverPerks;
			for (int ii = 0; ii < loadedPerks.size(); ii++) {
				GamePerk temp = (GamePerk)loadedPerks.get(ii);
				if (temp.getName().equalsIgnoreCase(perk))
					this.perks.add(temp.clone());
			}
		}
	}

	private void PARSECLASSES(String parseClasses) {
		String[] temp = parseClasses.split(":");
		if (temp.length == 2) {
			this.classPointer = Integer.parseInt(temp[0]);
			String[] sclass = temp[1].split("\\|");
			for (int i = 0; i < sclass.length; i++) {
				String current = sclass[i];
				if (current != null) {
					String[] slots = current.split("\\/");
					if (slots.length == 6) {
						GameClass mclass = new GameClass();
			
						int[] primary = getWeaponDataFromString(slots[0]);
						int[] secondary = getWeaponDataFromString(slots[1]);
						int[] lethal = getWeaponDataFromString(slots[2]);
						int[] tactical = getWeaponDataFromString(slots[3]);
						int[] knife = getWeaponDataFromString(slots[4]);
			
						mclass.setPrimary(primary[0], primary[1], (byte)primary[2]);
						mclass.setSecondary(secondary[0], secondary[1], (byte)secondary[2]);
						mclass.setLethal(lethal[0], lethal[1], (byte)lethal[2]);
						mclass.setTactical(tactical[0], tactical[1], (byte)tactical[2]);
						mclass.setKnife(knife[0], knife[1], (byte)knife[2]);
						mclass.setPerk(slots[5].replace(" ", ""));
			
						this.classes.add(mclass);
					}
				}
			}
		}
	}

	private void blankProfile() {
		this.credits = 0;
		this.xp = 0;
		this.level = 1;
		this.kills = 0;
		this.deaths = 0;
		this.classPointer = 0;
		this.maxClasses = 1;
		this.classes.clear();
		this.guns.clear();

		MCWarfare.getPlugin().print("CREATING BLANK PROFILE FOR " + this.playerName);

		ArrayList<KitGun> tguns = MCWarfare.getPlugin().loadedGuns;
		for (int i = 0; i < tguns.size(); i++) {
			if ((((KitGun)tguns.get(i)).isUnlocked(this)) || (((KitGun)tguns.get(i)).cost == 0)) {
				giveGun(((KitGun)tguns.get(i)).name);
			}
		}

		this.killstreaks.add("Ammo");
		this.killstreaks.add("E.M.P");
		this.killstreaks.add("TacticalNuke");

		GameClass temp = new GameClass();
		temp.setPrimary(MCWarfare.getPlugin().getGunManager().getFirstGunByType("primary"), 1, (byte)0);
		temp.setSecondary(MCWarfare.getPlugin().getGunManager().getFirstGunByType("secondary"), 1, (byte)0);
		temp.setLethal(0, 0, (byte)0);
		temp.setTactical(0, 0, (byte)0);
		temp.setKnife(Material.IRON_SWORD.getId(), 1, (byte)0);
		temp.setPerk("");
		this.classes.add(temp);

		//Set all your classes to a loaded state
		for (int i = 0; i < this.classes.size(); i++) {
			classes.get(i).setLoaded();
		}
		
		changedClassLoadout = true;
		changedGuns = true;
		changedPerks = true;
		changedKillstreaks = true;

		if (MCWarfare.getPlugin().autoSave)
			save(true, MCWarfare.getPlugin().getSQLController());
	}

	private String writeClassItemToString(ItemStack current) {
		return current.getType().getId() + "," + current.getAmount() + "," + current.getData().getData();
	}

	private int[] getWeaponDataFromString(String string) {
		String[] sval = string.split(",");
		int[] ret = new int[sval.length];
		for (int i = 0; i < sval.length; i++) {
			ret[i] = Integer.parseInt(sval[i]);
		}
		return ret;
	}

	public ArrayList<String> getPermissions() {
		return this.permissions;
	}

	public ArrayList<String> getOwnedGuns() {
		return this.guns;
	}

	public boolean ownsKillstreak(String name) {
		for (int i = 0; i < this.killstreaks.size(); i++) {
			if (((String)this.killstreaks.get(i)).toLowerCase().equals(name.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public boolean ownsPerk(String name) {
		for (int i = 0; i < this.perks.size(); i++) {
			if (((GamePerk)this.perks.get(i)).getName().toLowerCase().equals(name.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void removeGun(String gun) {
		for (int i = this.guns.size() - 1; i >= 0; i--) {
			if (((String)this.guns.get(i)).toLowerCase().equals(gun.toLowerCase())) {
				this.guns.remove(i);
			}
		}
		this.gunsRemoved.add(gun);
		if (this.loaded)
			this.changedGuns = true;
	}

	public void giveGun(String gun) {
		this.guns.add(gun);
		this.gunsAdded.add(gun);
		if (this.loaded)
			this.changedGuns = true;
	}

	public void givePerk(String string) {
		GamePerk perk = MCWarfare.getPlugin().matchPerk(string);
		if (perk != null)
			this.perks.add(perk);
		if (this.loaded)
			this.changedPerks = true;
	}

	public int getCredits() {
		return this.credits;
	}

	public void addCredits(int i) {
		this.credits += i;
		this.creditsGained += i;
		
		if (loaded)
			this.changedCredits = true;
	}

	public int getXp() {
		return this.xp;
	}

	public void setXp(int x) {
		this.xp = x;
		
		if (loaded)
			this.changedXp = true;
	}

	public int getLevel() {
		return this.level;
	}

	public void setLevel(int x) {
		this.level = x;
		this.rank = MCWarfare.getPlugin().matchRank(getLevel());
		
		if (loaded)
			this.changedLevel = true;
	}

	public void addKill() {
		this.kills += 1;
	}

	public void addDeath() {
		this.deaths += 1;
	}

	public void giveXp(int xp) {
		this.xp += xp;
	}

	public ArrayList<GameClass> getLoadedClasses() {
		return this.classes;
	}

	public ArrayList<GamePerk> getOwnedPerks() {
		return this.perks;
	}

	public ArrayList<String> getOwnedKillstreaks() {
		return this.killstreaks;
	}

	public void giveKillstreak(String string) {
		this.killstreaks.add(string);
	}

	public void addClass() {
		this.addedClasses += 1;
	}

	public void createNewClass() {
		GameClass temp = new GameClass();
		temp.setPrimary(MCWarfare.getPlugin().getGunManager().getFirstGunByType("primary"), 1, (byte)0);
		temp.setSecondary(MCWarfare.getPlugin().getGunManager().getFirstGunByType("secondary"), 1, (byte)0);
		temp.setLethal(0, 0, (byte)0);
		temp.setTactical(0, 0, (byte)0);
		temp.setKnife(Material.IRON_SWORD.getId(), 1, (byte)0);
		temp.setPerk("");
		this.classes.add(temp);
		
		if (this.loaded)
			this.changedClassLoadout = true;
	}

	public int getMaxClasses() {
		return this.maxClasses;
	}

	public String getPlayername() {
		return this.playerName;
	}

	public GameRank getRank() {
		if (getLevel() > 30) {
			return new GameRank("Level 31+", "31+", 31);
		}
		return this.rank;
	}

	public int getKills() {
		return this.kills;
	}

	public int getDeaths() {
		return this.deaths;
	}

	public double getKDR() {
		double big = (double)this.getKills();
		if(this.getDeaths() > 0) {
			big = (double)this.getKills() / (double)this.getDeaths() * 100.0D;
		} else {
			big *= 100.0D;
		}

		double round = (double)Math.round(big);
		return round / 100.0D;
	}
	
	public int getChests() {
		return this.chests;
	}
	
	public void setChests(int i) {
		this.chests = i;
	}

	public void reload() {
		/*Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable()
		{
			public void run() {
				GameProfile.this.loadProfile(GameProfile.this.playerName);
			}
		}
		, 20L);*/
		//MCWarfare.getPlugin().getSQLController().saveProfile(this);
	}

	public int getAmountClasses() {
		return this.classes.size();
	}
	
	private void removeMultipleGuns() {
		int q;
		for(q = guns.size()-1; q >= 0; q--) {  
			if (amtGun(guns.get(q)) > 1)
				guns.remove(q);
		}
	}
	
	private void removeMultiplePerks() {
		int q;
		for(q = perks.size()-1; q >= 0; q--) {  
			if (amtPerk(perks.get(q)) > 1)
				perks.remove(q);
		}
	}
	
	private int amtPerk(GamePerk gamePerk) {
		int amt = 0;
		for (int i = 0; i < perks.size(); i++) {
			GamePerk perk = perks.get(i);
			if (perk.getName().equalsIgnoreCase(gamePerk.getName()))
				amt++;
		}
		return amt;
	}

	private int amtGun(String string) {
		int amt = 0;
		for (int i = 0; i < guns.size(); i++) {
			if (guns.get(i).toLowerCase().equals(string.toLowerCase()))
				amt++;
		}
		return amt;
	}
	
	public StatMCWar getMCWarStat(boolean firstTimePlaying) {
		StatMCWar playerStat = new StatMCWar(playerName, firstTimePlaying);
		
		//Generate the string for your guns
		String mgun = "";
		if (this.changedGuns) {
			for (int i = 0; i < this.guns.size(); i++) {
				mgun = mgun + (String)this.guns.get(i);
				if (i < this.guns.size() - 1) {
					mgun = mgun + "|";
				}
			}
		}

		//Generate the string for your permissions
		String mperm = "";
		for (int i = 0; i < this.permissions.size(); i++) {
			mperm = mperm + (String)this.permissions.get(i);
			if (i < this.permissions.size() - 1) {
				mperm = mperm + "|";
			}
		}

		//Generate the string for your perks
		String mperk = "";
		if (this.changedPerks) {
			for (int i = 0; i < this.perks.size(); i++) {
				mperk = mperk + ((GamePerk)this.perks.get(i)).getName();
				if (i < this.perks.size() - 1) {
					mperk = mperk + "|";
				}
			}
		}

		//Generate the string for your killstreaks
		String mkills = "";
		if (this.changedKillstreaks) {
			for (int i = 0; i < this.killstreaks.size(); i++) {
				mkills = mkills + (String)this.killstreaks.get(i);
				if (i < this.killstreaks.size() - 1) {
					mkills = mkills + "|";
				}
			}
		}

		String ekills = this.killstreak1 + "|" + this.killstreak2 + "|" + this.killstreak3;

		//Generate the string for your killstreaks;
		String mclas = Integer.toString(this.classPointer) + ":";
		if (this.changedClassLoadout) {
			for (int i = 0; i < this.classes.size(); i++) {
				GameClass cls = (GameClass)this.classes.get(i);
				mclas = mclas + writeClassItemToString(cls.getPrimary()) + "/";
				mclas = mclas + writeClassItemToString(cls.getSecondary()) + "/";
				mclas = mclas + writeClassItemToString(cls.getLethal()) + "/";
				mclas = mclas + writeClassItemToString(cls.getTactical()) + "/";
				mclas = mclas + writeClassItemToString(cls.getKnife()) + "/";
				mclas = mclas + cls.getPerk() + " ";
				if (i < this.classes.size() - 1) {
					mclas = mclas + "|";
				}
			}
		}
		
		playerStat.user_kills = this.kills;
		playerStat.user_deaths = this.deaths;
		playerStat.user_level = this.level;
		playerStat.user_xp = this.xp;
		playerStat.user_credits = this.credits;
		playerStat.owned_guns = mgun;
		playerStat.defined_classes = mclas;
		playerStat.permissions = mperm;
		playerStat.user_perks = mperk;
		playerStat.user_killstreaks = mkills;
		playerStat.equipped_killstreaks = ekills;
		playerStat.max_classes = this.maxClasses;
		
		
		return playerStat;
	}
	
	@Deprecated
	public String getRawSQLValues(boolean insert) {
		//System.out.println("ASDFGASDASFAGFASG " + this.changedGuns);
		//System.out.println("ASDFGASDASFAGFASG " + this.changedKillstreaks);
		//System.out.println("ASDFGASDASFAGFASG " + this.changedClassLoadout);
		//System.out.println("ASDFGASDASFAGFASG " + this.changedPerks);

		//Generate the string for your guns
		String mgun = "";
		if (this.changedGuns) {
			for (int i = 0; i < this.guns.size(); i++) {
				mgun = mgun + (String)this.guns.get(i);
				if (i < this.guns.size() - 1) {
					mgun = mgun + "|";
				}
			}
		}

		//Generate the string for your permissions
		String mperm = "";
		for (int i = 0; i < this.permissions.size(); i++) {
			mperm = mperm + (String)this.permissions.get(i);
			if (i < this.permissions.size() - 1) {
				mperm = mperm + "|";
			}
		}

		//Generate the string for your perks
		String mperk = "";
		if (this.changedPerks) {
			for (int i = 0; i < this.perks.size(); i++) {
				mperk = mperk + ((GamePerk)this.perks.get(i)).getName();
				if (i < this.perks.size() - 1) {
					mperk = mperk + "|";
				}
			}
		}

		//Generate the string for your killstreaks
		String mkills = "";
		if (this.changedKillstreaks) {
			for (int i = 0; i < this.killstreaks.size(); i++) {
				mkills = mkills + (String)this.killstreaks.get(i);
				if (i < this.killstreaks.size() - 1) {
					mkills = mkills + "|";
				}
			}
		}

		String ekills = this.killstreak1 + "|" + this.killstreak2 + "|" + this.killstreak3;

		//Generate the string for your killstreaks;
		String mclas = Integer.toString(this.classPointer) + ":";
		if (this.changedClassLoadout) {
			for (int i = 0; i < this.classes.size(); i++) {
				GameClass cls = (GameClass)this.classes.get(i);
				mclas = mclas + writeClassItemToString(cls.getPrimary()) + "/";
				mclas = mclas + writeClassItemToString(cls.getSecondary()) + "/";
				mclas = mclas + writeClassItemToString(cls.getLethal()) + "/";
				mclas = mclas + writeClassItemToString(cls.getTactical()) + "/";
				mclas = mclas + writeClassItemToString(cls.getKnife()) + "/";
				mclas = mclas + cls.getPerk() + " ";
				if (i < this.classes.size() - 1) {
					mclas = mclas + "|";
				}
			}
		}

		if (!insert) {//for update
			String values = " `user_kills`='" + this.kills + "'," + " `user_deaths`='" + this.deaths;
			if (this.changedLevel)
				values += "'," + " `user_level`='" + this.level;
			
			if (this.changedXp)
				values += "'," + " `user_xp`='" + this.xp;
			
			if (this.changedCredits)
				values += "'," + " `user_credits`='" + this.credits;
			
			if (this.changedGuns)
				values += "'," + " `owned_guns`='" + mgun;
			
			if (this.changedClassLoadout)
				values += "'," + " `defined_classes`='" + mclas;
			
			values += "'," + " `permissions`='" + mperm;
			
			if (this.changedPerks)
				values += "'," + " `user_perks`='" + mperk;
			
			if (this.changedKillstreaks) {
				values += "'," + " `user_killstreaks`='" + mkills;
				values += "'," + " `equipped_killstreaks`='" + ekills;
			}
			
			values += "'," + " `max_classes`='" + this.maxClasses + "'";

			return values;
		}
		String values = "'" + this.playerName + "'," + 
				"'" + this.kills + "'," + 
				" '" + this.deaths + "'," + 
				" '" + this.level + "'," + 
				" '" + this.xp + "'," + 
				" '" + this.credits + "',";
			if (this.changedGuns)
				values += " '" + mgun + "',";
			if (this.changedClassLoadout)
				values += " '" + mclas + "',";
			
			values += " '" + mperm + "',";
			
			if (this.changedPerks)
				values += " '" + mperk + "',";
			if (this.changedKillstreaks)
				values += " '" + mkills + "',";
			
			values += " '" + ekills + "'," + " '" + this.maxClasses + "'";

		return values;
	}

	public void save(boolean firstTimePlaying, SQLController sqlController) {
		if (!this.loaded) {
			System.out.println("Profile not loaded for " + this.playerName + ". Will not save!");
			return;
		}
		
		for (int i = 0; i < this.classes.size(); i++) {
			if (classes.get(i).isChanged()) {
				this.changedClassLoadout = true;
			}
		}
		
		MCWarfare.getPlugin().print("SAVING PROFILE: " + this.playerName);

		if (!firstTimePlaying) {
			GameProfile temp = new GameProfile(this.playerName, true);
			int addedxp = temp.startXp - this.startXp;
			if (addedxp > 0) {
				this.xp += addedxp;
			}
			this.permissions = temp.getPermissions();
			this.guns = temp.getOwnedGuns();
			for (int i = 0; i < this.permissionsRemoved.size(); i++)
				this.permissions.remove(this.permissionsRemoved.get(i));
			for (int i = 0; i < this.gunsRemoved.size(); i++)
				this.guns.remove(this.gunsRemoved.get(i));
			for (int i = 0; i < this.permissionsAdded.size(); i++)
				this.permissions.add((String)this.permissionsAdded.get(i));
			for (int i = 0; i < this.gunsAdded.size(); i++) {
				this.guns.add((String)this.gunsAdded.get(i));
			}
			this.maxClasses = temp.maxClasses;
			this.maxClasses += this.addedClasses;
			if (this.maxClasses > 5)
				this.maxClasses = 5;
			if (this.maxClasses < this.classes.size()) {
				this.maxClasses = this.classes.size();
			}

			ArrayList<GamePerk> a = temp.perks;
			for (int i = 0; i < a.size(); i++) {
				if (!ownsPerk(((GamePerk)a.get(i)).getName())) {
					this.perks.add((GamePerk)a.get(i));
				}

			}

			ArrayList<String> ks = temp.killstreaks;
			for (int i = 0; i < ks.size(); i++) {
				if (!ownsKillstreak((String)ks.get(i))) {
					this.killstreaks.add((String)ks.get(i));
				}
			}
		}
		
		this.removeMultipleGuns();
		this.removeMultiplePerks();
		
		//String values = getSQLValues(false);

		StatMCWar stat = getMCWarStat(firstTimePlaying);
		sqlController.saveStats(this.playerName, stat, firstTimePlaying);

		changedClassLoadout = false;
		changedGuns = false;
		changedPerks = false;
		changedKillstreaks = false;
		changedLevel = false;
		changedCredits = false;
		changedXp = false;
		
		this.gunsRemoved.clear();
		this.permissionsRemoved.clear();
		this.gunsAdded.clear();
		this.permissionsAdded.clear();
		this.creditsGained = 0;
		this.startXp = this.xp;
		this.addedClasses = 0;
	}
}