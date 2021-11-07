package com.solemanseb.uhc;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PluginMain extends JavaPlugin {
    // Fields
    private Logger logger;
    public PluginCommands commands;

    private boolean debugMode = false;
    private World world;

    private ArrayList<Player> activePlayers = new ArrayList<>();


    @Override
    public void onDisable() {
        logger.info("BattleDome plugin disabled");
    }


    @Override
    public void onEnable() {
        logger = Logger.getLogger("com.solemanseb.uhc.PluginMain");
        logger = getLogger();
        logger.info("UHC Plugin Enabled!");
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("debugMode", false);
        getServer().getPluginManager().registerEvents(new PluginListener(this), this);

        commands = new PluginCommands(this);
        for (String command : PluginCommands.registeredCommands) {
            this.getCommand(command).setExecutor(commands);
        }

        List<World> worlds = Bukkit.getWorlds();
        if (worlds.size() < 1) {
            logger.warning("Could not detect main world! Plugin will not work.");
        }
        world = worlds.get(0);

    }

    public World getWorld() {
        return this.world;
    }

    public ArrayList<Player> getActivePlayers() {
        return activePlayers;
    }

    public void addActivePlayer(Player player) {
        this.activePlayers.add(player);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public void setWorld(World world1) {
        this.world = world1;
    }
}
