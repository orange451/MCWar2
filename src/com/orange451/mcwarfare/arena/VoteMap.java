package com.orange451.mcwarfare.arena;

public class VoteMap {
	private String arenaName;
	private int votes;
	
	public VoteMap(String name) {
		this.arenaName = name;
	}
	
	public int getVotes() {
		return this.votes;
	}
	
	public String getArenaName() {
		return this.arenaName;
	}

	public void vote() {
		this.votes++;
	}
}
