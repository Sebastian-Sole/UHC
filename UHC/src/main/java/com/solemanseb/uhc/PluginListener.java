package com.solemanseb.uhc;


import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PluginListener implements Listener {

    PluginMain main;

    public PluginListener(PluginMain main) { this.main = main; }

    //todo; playerInteractEvent where if goldenhead, set absorption, regeneration and remove item.
    @EventHandler
    public void onEat(PlayerItemConsumeEvent event){
        //todo; check if game is running
        Player player = event.getPlayer();
        var eatenItem = event.getItem();
        if (eatenItem.getItemMeta().getDisplayName().equals("GOLDEN HEAD")){
            player.removePotionEffect(PotionEffectType.ABSORPTION);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200,1)); // 4 Hearts
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        main.getLogger().info("Player Joined event");
        Location joinLoc = event.getPlayer().getLocation();
        WorldBorder wb = main.getWorld().getWorldBorder();

        wb.setDamageAmount(0);
        wb.setWarningDistance(0);
        wb.setCenter(joinLoc);
        wb.setSize(main.getConfig().getInt("preGameBorderSize", 300));

        Player player = event.getPlayer();
        player.setFoodLevel(20);
        player.setHealthScale(20.0);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);

        main.commands.worldBorderModified = true;

        UHCPlayer uhcPlayer = new UHCPlayer(player);
        main.addActivePlayer(uhcPlayer);

    }

    @EventHandler
    public void onAutocomplete(TabCompleteEvent event){
        String buffer = event.getBuffer();
        if(!buffer.startsWith("/")) return;
        String[] args = buffer.split(" ");

        List<String> completions = main.commands.getCompletions(args, event.getCompletions());

        event.setCompletions(completions);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        // Drop player head
        //todo; Add if game is running check
        Player killed = event.getEntity().getPlayer();
        event.getDrops().add(getPlayerHead(killed));

    }

    private ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.matchMaterial("PLAYER_HEAD"), 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.DARK_RED.toString() + player.getName()+ ChatColor.DARK_RED + "'s Head");
        return head;
    }


}
