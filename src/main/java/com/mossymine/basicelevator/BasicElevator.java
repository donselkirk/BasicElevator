package com.mossymine.basicelevator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import org.bukkit.plugin.java.JavaPlugin;

public final class BasicElevator extends JavaPlugin implements Listener {

    FileConfiguration config;
    File cfile;
    List<Player> cd;
    List<Material> matches;

    public BasicElevator() {
        this.config = null;
        this.cfile = null;
        this.cd = new ArrayList<>(); //Player
        this.matches = new ArrayList<>(); //Material
        this.matches.add(Material.AIR);
        this.matches.add(Material.TORCH);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.config = this.getConfig();
        this.config.options().copyDefaults(true);
        this.saveConfig();
        this.cfile = new File(this.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.cfile);
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
        if (e.getFrom().getY() < Objects.requireNonNull(e.getTo()).getY()) {
            final Block b1 = p.getLocation().getBlock();
            final Block b2 = b1.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
            if (this.isNext(b1, b2)) {
                for (int loop = 1; loop <= this.getConfig().getInt("distance"); ++loop) {
                    final Block b3 = b1.getLocation().add(0.0, loop, 0.0).getBlock();
                    final Block b4 = b3.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
                    if (this.isNext(b3, b4)) {
                        final Block b5 = b3.getLocation().add(0.0, 1.0, 0.0).getBlock();
                        if (this.matches.contains(b5.getType())) {
                            final Location loc = p.getLocation();
                            loc.setY(b3.getLocation().getY());
                            p.teleport(loc);
                            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
                            this.cd.add(p);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> BasicElevator.this.cd.remove(p), this.getConfig().getInt("cooldown"));
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
                final Block b3 = b1.getLocation().add(0.0, loop, 0.0).getBlock();
                final Block b4 = b3.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
                if (this.isNext(b3, b4)) {
                    final Block b5 = b3.getLocation().add(0.0, 1.0, 0.0).getBlock();
                    if (this.matches.contains(b5.getType())) {
                        final Location loc = p.getLocation();
                        loc.setY(b3.getLocation().getY());
                        p.teleport(loc);
                        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
                        this.cd.add(p);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> BasicElevator.this.cd.remove(p), this.getConfig().getInt("cooldown"));
                        break;
                    }
                }
            }
        }
    }

    public boolean isNext(final Block b1, final Block b2) {
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
