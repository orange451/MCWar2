package com.orange451.mcwarfare.arena;

import java.util.ArrayList;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftFirework;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkExplosion
{
	private Location location;

	public FireworkExplosion(Location location)
	{
		this.location = location;
	}

	public void explode(Color c) {
		net.minecraft.server.v1_7_R1.World world = ((CraftWorld)this.location.getWorld()).getHandle();
		Firework bfirework = (Firework)this.location.getWorld().spawn(this.location, Firework.class);
		bfirework.setFireworkMeta((FireworkMeta)getFirework(c).getItemMeta());
		CraftFirework a = (CraftFirework)bfirework;
		world.broadcastEntityEffect(a.getHandle(), (byte)17);
		bfirework.remove();
	}

	public ItemStack getFirework(Color color) {
		FireworkEffect.Type type = FireworkEffect.Type.BALL_LARGE;
		ItemStack i = new ItemStack(Material.FIREWORK, 1);
		FireworkMeta fm = (FireworkMeta)i.getItemMeta();
		ArrayList c = new ArrayList();
		c.add(color);
		FireworkEffect e = FireworkEffect.builder()
				.flicker(true)
				.withColor(c)
				.withFade(c)
				.with(type)
				.trail(true)
				.build();
		fm.addEffect(e);
		fm.setPower(3);
		i.setItemMeta(fm);

		return i;
	}
}