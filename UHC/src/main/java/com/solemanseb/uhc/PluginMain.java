package com.solemanseb.uhc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PluginMain extends JavaPlugin {
    // Fields
    private Logger logger = Bukkit.getServer().getLogger();
    public PluginCommands commands;

    private boolean debugMode = false;
    private World world;

    private ArrayList<UHCPlayer> activePlayers = new ArrayList<>();
    private String gamestyle;


    @Override
    public void onDisable() {
        logger.info("UHC plugin disabled");
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

        goldenAppleRecipe();

    }

    private void goldenAppleRecipe() {
        ItemStack goldenHead = new ItemStack(Material.GOLDEN_APPLE,1);
        ItemMeta meta = goldenHead.getItemMeta();
        meta.setDisplayName("GOLDEN HEAD");
        goldenHead.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this,"golden_head");
        ShapedRecipe recipe = new ShapedRecipe(key, goldenHead);

        recipe.shape(
                "GGG",
                "GHG",
                "GGG");

        recipe.setIngredient('G',Material.GOLD_INGOT);
        recipe.setIngredient('H',Material.matchMaterial("PLAYER_HEAD"));
        Bukkit.addRecipe(recipe);
    }

    public World getWorld() {
        return this.world;
    }

    public ArrayList<UHCPlayer> getActivePlayers() {
        return activePlayers;
    }

    public void addActivePlayer(UHCPlayer player) {
        this.activePlayers.add(player);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public void setWorld(World world1) {
        this.world = world1;
    }

    public void setGamestyle(String gamestyle) {
        this.gamestyle = gamestyle;
    }

    public String getGamestyle() {
        return gamestyle;
    }
}
