package com.orange451.mcwarfare.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class LaunchPad
{
  public Vector direction;
  public Block me;
  public double speed = 3.5D;

  public LaunchPad(Block b, Vector direction) {
    this.me = b;
    this.direction = direction;
  }

  public static LaunchPad getLaunchPad(Block b)
  {
    Block b1 = checkBlock(b, Material.SPONGE, 1, -1, 0);
    Block b2 = checkBlock(b, Material.SPONGE, -1, -1, 0);
    Block b3 = checkBlock(b, Material.SPONGE, 0, -1, 1);
    Block b4 = checkBlock(b, Material.SPONGE, 0, -1, -1);
    if (b1 != null)
      return new LaunchPad(b, getDirection(b, b1));
    if (b2 != null)
      return new LaunchPad(b, getDirection(b, b2));
    if (b3 != null)
      return new LaunchPad(b, getDirection(b, b3));
    if (b4 != null)
      return new LaunchPad(b, getDirection(b, b4));
    return null;
  }

  private static Vector getDirection(Block b, Block b1)
  {
    double xdn = b.getLocation().getBlockX() - b1.getLocation().getBlockX();
    double ydn = b.getLocation().getBlockY() - b1.getLocation().getBlockY();
    double zdn = b.getLocation().getBlockZ() - b1.getLocation().getBlockZ();

    double a = Math.sqrt(xdn * xdn + ydn * ydn + zdn * zdn);
    if (a > 0.0D) {
      xdn /= a;
      ydn /= a;
      zdn /= a;
    }
    return new Vector(xdn, ydn, zdn);
  }

  private static Block checkBlock(Block b, Material mat, int i, int j, int k) {
    Block bb = b.getLocation().add(i, j, k).getBlock();
    if (bb.getType().equals(mat)) {
      return bb;
    }
    return null;
  }

  public void launch(Player pl) {
    pl.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 4));
    Vector nv = new Vector(this.direction.getX() * this.speed, this.direction.getY() * (this.speed / 2.0D), this.direction.getZ() * this.speed);
    pl.setVelocity(nv);
    pl = null;
  }
}