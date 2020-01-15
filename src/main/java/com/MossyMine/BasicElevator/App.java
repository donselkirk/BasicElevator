package com.MossyMine.BasicElevator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin implements Listener {

    FileConfiguration config;
    File cfile;
    List<Player> cd;
    List<Material> matches;
	
    public App() {
        this.config = null;
        this.cfile = null;
        this.cd = new ArrayList<Player>();
        this.matches = new ArrayList<Material>();
        this.matches.add(Material.AIR);
        this.matches.add(Material.TORCH);
        /*
        this.matches.add(Material.ACACIA_SIGN);
        this.matches.add(Material.ACACIA_WALL_SIGN);
        this.matches.add(Material.BIRCH_SIGN);
        this.matches.add(Material.BIRCH_WALL_SIGN);
        this.matches.add(Material.DARK_OAK_SIGN);
        this.matches.add(Material.DARK_OAK_WALL_SIGN);
        this.matches.add(Material.JUNGLE_SIGN);
        this.matches.add(Material.JUNGLE_WALL_SIGN);
        this.matches.add(Material.OAK_SIGN);
        this.matches.add(Material.OAK_WALL_SIGN);
        this.matches.add(Material.SPRUCE_SIGN);
        this.matches.add(Material.SPRUCE_WALL_SIGN);
        this.matches.add(Material.LEVER);
        this.matches.add(Material.REDSTONE_TORCH);
        this.matches.add(Material.REDSTONE_WALL_TORCH);
        */
        
        //this.matches.add(Material.ACACIA_PRESSURE_PLATE);
        //75, 76
        //this.matches.add(Material.AIR);
    }
	
	@Override
	public void onEnable() {
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.saveDefaultConfig();
        this.config = this.getConfig();
        this.config.options().copyDefaults(true);
        this.saveConfig();
        this.cfile = new File(this.getDataFolder(), "config.yml");
        this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(this.cfile);
	}
	
    @EventHandler
    public void onMove(final PlayerMoveEvent e) {
        final Player p = e.getPlayer();
        if (!p.hasPermission("elevator.use")) {
            return;
        }
        if (this.cd.contains(p)) {
            return;
        }
        if (e.getFrom().getY() < e.getTo().getY()) {
            final Block b1 = p.getLocation().getBlock();
            final Block b2 = b1.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
            if (this.isNext(b1, b2)) {
                for (int loop = 1; loop <= this.getConfig().getInt("distance"); ++loop) {
                    final Block b3 = b1.getLocation().add(0.0, (double)loop, 0.0).getBlock();
                    final Block b4 = b3.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
                    if (this.isNext(b3, b4)) {
                        final Block b5 = b3.getLocation().add(0.0, 1.0, 0.0).getBlock();
                        if (this.matches.contains(b5.getType())) {
                            final Location loc = p.getLocation();
                            loc.setY(b3.getLocation().getY());
                            p.teleport(loc);
                            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
                            this.cd.add(p);
                            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this, (Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    App.this.cd.remove(p);
                                }
                            }, (long)this.getConfig().getInt("cooldown"));
                            break;
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void isPlayerSneaking(final PlayerToggleSneakEvent e) {
        final Player p = e.getPlayer();
        if (!p.hasPermission("elevator.use")) {
            return;
        }
        if (p.isSneaking()) {
            return;
        }
        if (this.cd.contains(p)) {
            return;
        }
        final Block b1 = p.getLocation().getBlock();
        final Block b2 = b1.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
        if (this.isNext(b1, b2)) {
            for (int loop = -1; loop >= -this.getConfig().getInt("distance"); --loop) {
                final Block b3 = b1.getLocation().add(0.0, (double)loop, 0.0).getBlock();
                final Block b4 = b3.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
                if (this.isNext(b3, b4)) {
                    final Block b5 = b3.getLocation().add(0.0, 1.0, 0.0).getBlock();
                    if (this.matches.contains(b5.getType())) {
                        final Location loc = p.getLocation();
                        loc.setY(b3.getLocation().getY());
                        p.teleport(loc);
                        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
                        this.cd.add(p);
                        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this, (Runnable)new Runnable() {
                            @Override
                            public void run() {
                                App.this.cd.remove(p);
                            }
                        }, (long)this.getConfig().getInt("cooldown"));
                        break;
                    }
                }
            }
        }
    }
	
    public boolean isNext(final Block b1, final Block b2) {
        //final String plate = this.getConfig().getString("plate");
        //final String block = this.getConfig().getString("block");
        //final boolean test = b1.getType() == Material.getMaterial(plate);
        //getLogger().info(Boolean.toString(test));
        return b1.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE && b2.getType() == Material.IRON_BLOCK;
    }
	
    public boolean onCommand(final CommandSender sender, final Command cmd, final String a, final String[] args) {
        if (a.equalsIgnoreCase("reload") && (sender.isOp() || !(sender instanceof Player))) {
            this.reloadConfig();
            this.saveConfig();
            sender.sendMessage("[§bSimpleElevator§f] §aReloaded Configurations!");
        }
        return true;
    }
}

