package com.solemanseb.uhc;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class PluginCommands implements CommandExecutor {

    public static final String[] registeredCommands = {
            "start",
            "end",
    };

    public boolean gameIsRunning = false;
    public boolean worldBorderModified;
    private final PluginMain main;

    public PluginCommands(PluginMain main) {
        this.main = main;
    }

    public List<String> getCompletions(String[] args, List<String> existingCompletions){
        switch (args[0]){
            case "/start":
            case "/end":
            default:
                return existingCompletions;
        }
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if ("start".equals(label)){
            if (gameIsRunning){
                commandSender.sendMessage("Game is in progress, use /end before starting another game");
                return true;
            }
            Bukkit.broadcastMessage("Starting game...");

            if (worldBorderModified) {
                WorldBorder wb = main.getWorld().getWorldBorder();
                wb.setCenter(new Location(main.getWorld(),0,64,0));
                wb.setSize(1500);
            }

            List<World> worlds = Bukkit.getWorlds();
            World world1 = worlds.get(0);
            if (main.getConfig().getBoolean("setTimeToZero", true)) {
                main.setWorld(world1);
                main.getWorld().setTime(0);
            }

            main.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, false);

            for (Player player : main.getActivePlayers()){
                playerStateOnStart(player);
            }

            startTimers();

            gameIsRunning = true;
        }

        return false;
    }

    private void startTimers() {
        // 10 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "10 MINUTES!");
                var xBorder = main.getWorld().getWorldBorder().getSize();
                Bukkit.broadcastMessage("Be inside x: " + xBorder + ", z: " + xBorder + ", or you will DIE");
            }
        }, 6000, 18000L);

        // 5 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "5 MINUTES!");
                var xBorder = main.getWorld().getWorldBorder().getSize();
                Bukkit.broadcastMessage("Be inside x: " + xBorder + ", z: " + xBorder + ", or you will DIE");
            }
        }, 12000, 18000L);

        // 3 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "3 MINUTES!");
                var xBorder = main.getWorld().getWorldBorder().getSize();
                Bukkit.broadcastMessage("Be inside x: " + xBorder + ", z: " + xBorder + ", or you will DIE");
            }
        }, 14400, 18000L);

        // 1 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "1 MINUTE!");
                var xBorder = main.getWorld().getWorldBorder().getSize();
                Bukkit.broadcastMessage("Be inside x: " + xBorder + ", z: " + xBorder + ", or you will DIE");
            }
        }, 16800, 18000L);

        // Shrink border
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Border shrinking...");
                WorldBorder wb = main.getWorld().getWorldBorder();
                if (wb.getSize() < 200) {
                    wb.setSize(wb.getSize() - 200.00);
                }
                Bukkit.broadcastMessage("Next shrink: 15 Minutes");
            }
        }, 18000, 18000L);
    }

    private void playerStateOnStart(Player player) {
        if (player == null)
            return;
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealthScale(20.0);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 30, 5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 30, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 30, 10));
    }

}
