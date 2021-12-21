package com.orange451.mcwarfare.player;

import com.orange451.mcwarfare.MCWarfare;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Clan
{
	private String name = "";
	private String owner = "";
	private String owner_new = "";
	private ArrayList<String> moderators = new ArrayList<String>();
	private ArrayList<String> moderators_new = new ArrayList<String>();
	private ArrayList<String> moderators_leave = new ArrayList<String>();
	private ArrayList<String> members = new ArrayList<String>();
	private ArrayList<String> members_new = new ArrayList<String>();
	private ArrayList<String> members_leave = new ArrayList<String>();
	private boolean changed;

	public Clan(ResultSet set) {
		try {
			this.name = set.getString("clan_name");
			this.owner = set.getString("clan_owner");
			PARSESTRINGLIST(set.getString("clan_moderators"), this.moderators, "\\|");
			PARSESTRINGLIST(set.getString("clan_users"), this.members, "\\|");
		} catch (Exception localException) {
			
		}
		this.removeMultipleMembers();
	}

	public Clan(String name) {
		this.name = name;
		this.removeMultipleMembers();
	}

	private void PARSESTRINGLIST(String parse, ArrayList<String> toArray, String seperator) {
		if (parse == null)
			return;
		String[] entry = parse.split(seperator);
		for (int i = 0; i < entry.length; i++)
			if ((entry[i] != null) && (entry[i].length() > 1))
				toArray.add(entry[i]);
	}

	public String getName()
	{
		return this.name;
	}

	public void join(String name) {
		this.members.add(name);
		this.members_new.add(name);
		this.changed = true;
		
		this.sendClanMessage(name + " has joined your clan");
	}

	public void leave(String name) {
		for (int i = this.members.size() - 1; i <= 0; i--) {
			if (((String)this.members.get(i)).equals(name)) {
				this.members.remove(i);
			}
		}
		
		sendClanMessage(name + " has left your clan");
		
		this.members_leave.add(name);
		this.changed = true;

		if (hasModerator(name)) {
			demote(name);
		}

		if (this.owner.equals(name))
			if (this.members.size() > 0)
				setOwner((String)this.members.get(0));
			else
				setOwner("");
	}

	public void sendClanMessage(String string) {
		for (int i = members.size()- 1; i >= 0; i--) {
			String temp = members.get(i);
			List<Player> players = Bukkit.matchPlayer(temp);
			if (players != null && players.size() > 0) {
				Player player = players.get(0);
				player.sendMessage(string);
			}
		}
	}

	public void promote(String name) {
		this.moderators.add(name);
		this.moderators_new.add(name);
		this.changed = true;
	}

	public void demote(String name) {
		for (int i = this.moderators.size() - 1; i >= 0; i--) {
			if (this.moderators.get(i) != null) {
				if (this.moderators.get(i).equals(name)) {
					this.moderators.remove(i);
				}
			}
		}

		this.moderators_leave.add(name);
		this.changed = true;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String name) {
		this.owner = name;
		this.owner_new = name;
		this.changed = true;
	}

	public boolean hasModerator(String name) {
		for (int i = this.moderators.size() - 1; i >= 0; i--) {
			if (i >= 0)
			{
				if (((String)this.moderators.get(i)).equalsIgnoreCase(name))
					return true;
			}
		}
		return false;
	}

	public boolean hasMember(String name) {
		for (int i = this.members.size() - 1; i >= 0; i--) {
			if (((String)this.members.get(i)).equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	private void removeMultipleMembers() {
		int q;
		for(q = members.size()-1; q >= 0; q--) {  
			if (amtMembers(members.get(q)) > 1)
				members.remove(q);
		}
	}

	private int amtMembers(String string) {
		int amt = 0;
		for (int i = 0; i < members.size(); i++) {
			if (members.get(i).toLowerCase().equals(string.toLowerCase()))
				amt++;
		}
		return amt;
	}
	
	public ArrayList<String> getMembers() {
		return this.members;
	}

	public ArrayList<String> getModerators() {
		return this.moderators;
	}
	
	public boolean hasBeenChanged() {
		return this.changed;
	}

	public void save() {
		if (!this.changed)
			return;
		this.changed = false;

		Clan temp = MCWarfare.getPlugin().loadClan(this.name);
		if (temp != null) {
			this.members = temp.getMembers();
			this.moderators = temp.getModerators();
			this.owner = temp.getOwner();

			if (this.owner_new != null && this.owner_new.length() > 2) {
				this.owner = this.owner_new;
			}

			for (int i = 0; i < this.members_new.size(); i++)
				this.members.add((String)this.members_new.get(i));
			for (int i = 0; i < this.members_leave.size(); i++) {
				for (int ii = this.members.size() - 1; ii >= 0; ii--) {
					if (((String)this.members.get(ii)).equals(this.members_leave.get(i))) {
						this.members.remove(ii);
					}
				}
			}

			for (int i = 0; i < this.moderators_new.size(); i++)
				this.members.add((String)this.moderators_new.get(i));
			for (int i = 0; i < this.moderators_leave.size(); i++) {
				for (int ii = this.moderators.size() - 1; ii >= 0; ii--) {
					if (((String)this.moderators.get(ii)).equals(this.moderators_leave.get(i))) {
						this.moderators.remove(ii);
					}
				}

			}

			String mods = "";
			for (int i = 0; i < this.moderators.size(); i++) {
				mods = mods + (String)this.moderators.get(i);
				if (i < this.moderators.size() - 1) {
					mods = mods + "|";
				}
			}

			String mems = "";
			for (int i = 0; i < this.members.size(); i++) {
				mems = mems + (String)this.members.get(i);
				if (i < this.members.size() - 1) {
					mems = mems + "|";
				}
			}

			String values = "`clan_owner`='" + this.owner + "'," + 
					" `clan_moderators`='" + mods + "'," + 
					" `clan_users`='" + mems + "'";
			try
			{
				Statement st = MCWarfare.getPlugin().getSQLDatabaseConnection().createStatement();
				String query = "UPDATE `MCWarClans` SET " + values + "  WHERE `clan_name` = '" + this.name + "'";
				st.executeUpdate(query);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			this.moderators_leave.clear();
			this.moderators_new.clear();
			this.members_leave.clear();
			this.members_new.clear();
			this.owner_new = null;
		}
	}
}