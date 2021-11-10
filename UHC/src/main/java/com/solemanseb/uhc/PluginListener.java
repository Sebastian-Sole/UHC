package com.solemanseb.uhc;


import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.IllegalFormatCodePointException;
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
        if(main.commands.gameIsRunning){
            return;
        }
        Player killed = event.getEntity().getPlayer();
        event.getDrops().add(getPlayerHead(killed));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if (!main.getGamestyle().equalsIgnoreCase("cutclean")){
            return;
        }
        Block blockBroken = event.getBlock();
        Material type = event.getBlock().getType();
        if (type.equals(Material.IRON_ORE)) {
            blockBroken.setType(Material.IRON_INGOT);
            event.setExpToDrop(4);
        }
        else if (type.equals(Material.GOLD_ORE)){
            blockBroken.setType(Material.GOLD_ORE);
            event.setExpToDrop(8);
        }
        else if (type.equals(Material.POTATO)){
            blockBroken.setType(Material.BAKED_POTATO);
            event.setExpToDrop(2);
        }
        // todo; find out why I can't get copper
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        if (!main.getGamestyle().equalsIgnoreCase("cutclean")){
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Chicken) {
            for (ItemStack item : event.getDrops()) {
                if (item.getType().equals(Material.CHICKEN)) {
                    item.setType(Material.COOKED_CHICKEN);
                }
            }
        } else if (entity instanceof Cow) {
            for (ItemStack drop : event.getDrops()) {
                if (drop.getType().equals(Material.BEEF)) {
                    drop.setType(Material.COOKED_BEEF);
                }
            }
        } else if (entity instanceof Pig) {
            for (ItemStack item : event.getDrops()) {
                if (item.getType().equals(Material.PORKCHOP)) {
                    item.setType(Material.COOKED_PORKCHOP);
                }
            }
        } else if (entity instanceof Rabbit) {
            for (ItemStack item : event.getDrops()) {
                if (item.getType().equals(Material.RABBIT)) {
                    item.setType(Material.COOKED_RABBIT);
                }
            }
        } else if (entity instanceof Sheep) {
            for (ItemStack item : event.getDrops()) {
                if (item.getType().equals(Material.MUTTON)) {
                    item.setType(Material.COOKED_MUTTON);
                }
            }
        }

    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!main.getGamestyle().equalsIgnoreCase("cutclean")){
            return;
        }
        Entity caught = event.getCaught();
        if (caught instanceof ItemStack fish) {
            if (fish.getType().equals(Material.SALMON)) {
                fish.setType(Material.COOKED_SALMON);
            }
            else if (fish.getType().equals(Material.COD)){
                fish.setType(Material.COOKED_COD);
            }
        }
    }

    private ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.matchMaterial("PLAYER_HEAD"), 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.DARK_RED.toString() + player.getName()+ ChatColor.DARK_RED + "'s Head");
        return head;
    }


}
