package com.solemanseb.uhc;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class PluginCommands implements CommandExecutor {

    public static final String[] registeredCommands = {
            "start",
            "end",
            "team"
    };

    public boolean gameIsRunning = false;
    public boolean worldBorderModified;
    private final PluginMain main;
    private ArrayList<Team> teams = new ArrayList<>();

    private Map<Integer, String> chatColors = Map.ofEntries(
            Map.entry(0, ChatColor.RED.toString()),
            Map.entry(1, ChatColor.BLUE.toString()),
            Map.entry(2, ChatColor.YELLOW.toString()),
            Map.entry(3, ChatColor.GREEN.toString()),
            Map.entry(4,ChatColor.LIGHT_PURPLE.toString()),
            Map.entry(5, ChatColor.GOLD.toString()),
            Map.entry(6, ChatColor.DARK_BLUE.toString()),
            Map.entry(7,ChatColor.DARK_GRAY.toString()),
            Map.entry(8,ChatColor.DARK_PURPLE.toString()),
            Map.entry(9, ChatColor.AQUA.toString()),
            Map.entry(10, ChatColor.DARK_GREEN.toString()),
            Map.entry(11, ChatColor.BLACK.toString()),
            Map.entry(12,ChatColor.DARK_RED.toString()),
            Map.entry(13, ChatColor.DARK_AQUA.toString()),
            Map.entry(14, ChatColor.GRAY.toString()),
            Map.entry(15, ChatColor.WHITE.toString())
    );
    private int borderStartSize;
    private long sizeDecrease;
    private int speedFactor = 0;

    public PluginCommands(PluginMain main) {
        this.main = main;
    }

    public List<String> getCompletions(String[] args, List<String> existingCompletions){
        switch (args[0]){
            case "/start":
            case "/end":
            case "/team":
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
            //todo; Add check that there is more than one team;
            Bukkit.broadcastMessage("Starting game...");

            teams.removeIf(team -> team.size() == 0); // Remove empty teams

            for (UHCPlayer soloPlayer : main.getActivePlayers()){ // Create solo teams
                if (soloPlayer.getTeam() == null) {
                    String abbreviation = soloPlayer.getPlayer().getName().substring(0,3);
                    Team soloTeam = new Team(abbreviation, soloPlayer, chatColors.get(teams.size()));
                    teams.add(soloTeam);
                }
            }

            if (worldBorderModified) {
                WorldBorder wb = main.getWorld().getWorldBorder();
                wb.setCenter(new Location(main.getWorld(),0,64,0));
                int teamAmount = teams.size();
                if (teamAmount <= 3){
                    wb.setSize(700*2);
                    this.borderStartSize = 700;
                }
                else if (teamAmount <= 6){
                    wb.setSize(800*2);
                    this.borderStartSize = 800;
                }
                else if (teamAmount <= 10){
                    wb.setSize(900*2);
                    this.borderStartSize = 900;
                }
                else if (teamAmount <= 15){
                    wb.setSize(1000*2);
                    this.borderStartSize = 1000;
                }
                else{
                    wb.setSize(1100*2);
                    this.borderStartSize = 1100;
                }
            }

            List<World> worlds = Bukkit.getWorlds();
            World world1 = worlds.get(0);
            if (main.getConfig().getBoolean("setTimeToZero", true)) {
                main.setWorld(world1);
                main.getWorld().setTime(0);
            }

            main.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, false);

            for (UHCPlayer player : main.getActivePlayers()){
                playerStateOnStart(player);
            }

            scatter();

            startTimers();

            gameIsRunning = true;
        }
        else if ("end".equals(label)){
            if(!gameIsRunning){
                commandSender.sendMessage("There is no game in progress. Use /start to start a new game.");
                return true;
            }
            worldBorderModified = false;
            Bukkit.broadcastMessage("Manhunt stopped!");
            gameIsRunning = false;
            return true;
        }
        else if ("team".equals(label)){
            if (gameIsRunning){
                commandSender.sendMessage("Cannot use team commands when game is running");
                return true;
            }
            UHCPlayer uhcPlayer = getUHCPlayerFromPlayer((Player) commandSender);
            if (args.length == 0 || args.length > 2){
                commandSender.sendMessage("Illegal command. Format: /team create <teamname>");
                return true;
            }
            else{
                // Creating a team
                if (args[0].equals("create")){
                    if (args.length != 2){
                        commandSender.sendMessage("Illegal command. Format: /team create <teamname>");
                        return true;
                    }
                    for (Team team : teams){
                        if (team.getMembers().contains(uhcPlayer)){
                            commandSender.sendMessage("You are already on a team. Please use /team leave to leave before creating a new team");
                            return true;
                        }
                        if (team.getTeamName().equals(args[1])){
                            commandSender.sendMessage("Team name is taken, please choose a different name");
                            return true;
                        }
                    }
                    Team newTeam = new Team(args[1],uhcPlayer, chatColors.get(teams.size())); //todo; add color
                    teams.add(newTeam);
                }
                // Leaving a team
                else if (args[0].equals("leave")){
                    var onTeam = teams.stream().anyMatch(team -> team.getMembers().contains(uhcPlayer));
                    if (onTeam) {
                        uhcPlayer.getPlayer().sendMessage("You have left " + uhcPlayer.getTeam().getTeamName());
                        uhcPlayer.leaveTeam();
                        return true;
                    }
                    commandSender.sendMessage("You are not on a team, illegal command");
                    return true;
                }
                // Inviting players to a team
                else if (args[0].equals("invite")){
                    var senderTeam = uhcPlayer.getTeam(); // Get players team
                    if (senderTeam == null){ // If the player is not on a team, they can't invite
                        uhcPlayer.getPlayer().sendMessage("You are not on a team. Use /team create <teamname> to create a team before inviting");
                        return true;
                    }
                    if (args.length != 2){ // If no target player is provided
                        uhcPlayer.getPlayer().sendMessage("Illegal format. Use /team invite <playername> to invite a player");
                        return true;
                    }
                    if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))){
                        uhcPlayer.getPlayer().sendMessage("Target is not online. Please invite online player");
                        return true;
                    }
                    else{
                        Player target = Bukkit.getPlayer(args[1]);
                        UHCPlayer targetPlayer = main.getActivePlayers().stream().filter(uhcPlayer1 -> uhcPlayer1.getPlayer().equals(target)).collect(Collectors.toList()).get(0);
                        targetPlayer.getPlayer().sendMessage("You have been invited to join " + senderTeam.getTeamName());
                        targetPlayer.getPlayer().sendMessage("Type /team <teamname> join, to join, or /team <teamname> ignore to ignore the invitation");
                        senderTeam.sendInvite(targetPlayer);
                        return true;
                    }
                }
                // Disbanding a team
                else if (args[0].equals("disband")){
                    var senderTeam = uhcPlayer.getTeam(); // Get players team
                    senderTeam.removeAllMembers();
                    teams.remove(senderTeam);
                }
                // Requesting to join a team
                else if (args[0].equals("request")){
                    if (args[1] == null){
                        uhcPlayer.getPlayer().sendMessage("Invalid command format. Please use /team request <teamname>");
                    }
                    String teamNameParam = args[1];
                    var teamNameMatch = teams.stream().filter(team -> team.getTeamName().equals(teamNameParam)).collect(Collectors.toList()).get(0);
                    if (teamNameMatch == null) { // If arg doesn't provide a legitimate team.
                        uhcPlayer.getPlayer().sendMessage("Team doesn't exist, have you spelled it correctly?");
                        return true;
                    }
                    // If team exists
                    uhcPlayer.sendRequest(teamNameMatch);
                    for (UHCPlayer member : teamNameMatch.getMembers()){
                        member.getPlayer().sendMessage(uhcPlayer.getPlayer().getName() + " has requested to join your team.");
                        member.getPlayer().sendMessage("Use /team accept <playername> to accept, or /team decline <playername> to decline");
                    }
                    return true;

                }
                // Joining (accepting) an invite from a TEAM. Format: /team join <teamname>
                else if (args[0].equals("join")){
                    String teamNameParam = args[1];
                    var teamNameMatch = teams.stream().filter(team -> team.getTeamName().equals(teamNameParam)).collect(Collectors.toList()).get(0);
                    if (teamNameMatch == null) { // If arg doesn't provided a legitimate team.
                        uhcPlayer.getPlayer().sendMessage("Team doesn't exist, have you spelled it correctly?");
                        return true;
                    }
                    // If player wants to join a team that exists, do the following
                    if (teamNameMatch.getInviteTargets().contains(uhcPlayer)
                            && uhcPlayer.getTeamInviteSourceList().contains(teamNameMatch)){ // If sender has been invited by team
                        Team currentTeam = uhcPlayer.getTeam();
                        if (currentTeam != null){ // If player is already on a team
                            currentTeam.removeMember(uhcPlayer); // Remove player from that team
                        }
                        Bukkit.broadcastMessage(uhcPlayer.getPlayer().getName() + " has joined " + teamNameMatch.getTeamName());
                        teamNameMatch.addMember(uhcPlayer); // Add player to their new team
                        teamNameMatch.removeInvitedPlayer(uhcPlayer);
                        uhcPlayer.removeInviteFrom(teamNameMatch);
                        uhcPlayer.clearRequests(); // So the player doesn't move teams unintentionally
                        return true;
                    }
                    else { // If player hasn't been invited
                        uhcPlayer.getPlayer().sendMessage("You cannot join a team you haven't been invited to");
                        uhcPlayer.getPlayer().sendMessage("Use /team request <teamname> to request to join");
                        return true;
                    }

                }
                // Team accepting request from player
                else if (args[0].equals("accept")){
                    String playerName = args[1];
                    if (playerName == null){
                        commandSender.sendMessage("Please provide a name");
                        return true;
                    }

                    UHCPlayer targetPlayer = main.getActivePlayers().stream().filter(player -> player.getPlayer().equals(Bukkit.getPlayer(playerName))).collect(Collectors.toList()).get(0);
                    Team invitingTeam = uhcPlayer.getTeam();
                    if (targetPlayer.getRequestedTeamsList().contains(invitingTeam)
                            && invitingTeam.getPlayerRequestsList().contains(targetPlayer)){ // If player has requested
                        Team currentTeam = targetPlayer.getTeam();
                        if (currentTeam !=null){
                            currentTeam.removeMember(targetPlayer);
                        }
                        Bukkit.broadcastMessage(uhcPlayer.getPlayer().getName() + " has joined " + invitingTeam.getTeamName());
                        invitingTeam.addMember(targetPlayer);
                        invitingTeam.removePlayerRequest(targetPlayer);
                        targetPlayer.clearRequests(); // So the player doesn't move teams unintentionally
                        return true;
                    }
                    else { // If player has not requested
                        uhcPlayer.getPlayer().sendMessage("Player has not requested to join your team");
                        uhcPlayer.getPlayer().sendMessage("Use /team invite <playername> to invite a player to your team");
                        return true;
                    }
                }
            }
        }
        else if ("speed".equals(label)){
            if (args.length > 1){
                commandSender.sendMessage("Illegal command format. Please use /speed <number>");
                return true;
            }
            try {
                int speed = Integer.parseInt(args[0]);
                if (speed < 0){
                    commandSender.sendMessage("Please provide a positive speed number");
                    return true;
                }
                else{
                    this.speedFactor =  speed;
                }
            } catch (NumberFormatException e){
                commandSender.sendMessage("Illegal command format. Please use /speed <number>");
                return true;
            }
        }


        return false;
    }

    private UHCPlayer getUHCPlayerFromPlayer(Player player1){
        return main.getActivePlayers().stream().filter(uhcPlayer -> uhcPlayer.getPlayer().equals(player1)).collect(Collectors.toList()).get(0);
    }

    private Team getTeamFromPlayer(Player player) {
        for (Team team : teams){
            boolean onTeam = team.getMembers().contains(player);
            if (onTeam){
                return team;
            }
        }
        return null;
    }

    private void startTimers() {
        switch (this.borderStartSize){
            case 600 -> sizeDecrease = 100 + this.speedFactor;
            case 700 -> sizeDecrease = 125 + this.speedFactor;
            case 800 -> sizeDecrease = 150 + this.speedFactor;
            case 900 -> sizeDecrease = 175 + this.speedFactor;
        }

        // 10 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, () -> {
            if (main.getWorld().getWorldBorder().getSize()/2 <= 300.0){
                sizeDecrease = 100;
            }
            if (main.getWorld().getWorldBorder().getSize()/2 < 100.0){
                sizeDecrease = 10;
            }
            Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "10 MINUTES!");
            var nextBorder = (main.getWorld().getWorldBorder().getSize() / 2) - sizeDecrease;
            Bukkit.broadcastMessage("Be inside +-x: " + ChatColor.GREEN  + nextBorder + ", +-z: " + ChatColor.GREEN.toString()  + nextBorder + ", or you will DIE");
        }, 200, 1200L); // 6000, 18000L

        // 5 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                if (main.getWorld().getWorldBorder().getSize()/2 <= 300.0){
                    sizeDecrease = 100;
                }
                else if (main.getWorld().getWorldBorder().getSize()/2 < 100.0){
                    sizeDecrease = 10;
                }
                else if (main.getWorld().getWorldBorder().getSize()/2 <= 40){
                    sizeDecrease = 0;
                }
                Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "5 MINUTES!");
                var nextBorder = (main.getWorld().getWorldBorder().getSize() / 2) - sizeDecrease;
                Bukkit.broadcastMessage("Be inside +-x: " + ChatColor.GREEN  + nextBorder + ", +-z: " + ChatColor.GREEN.toString()  + nextBorder + ", or you will DIE");
            }
        }, 12000, 18000L); // 12000, 18000L

        // 3 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                if (main.getWorld().getWorldBorder().getSize()/2 <= 300.0){
                    sizeDecrease = 100;
                }
                if (main.getWorld().getWorldBorder().getSize()/2 < 100.0){
                    sizeDecrease = 10;
                }
                Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "3 MINUTES!");
                var nextBorder = (main.getWorld().getWorldBorder().getSize() / 2) - sizeDecrease;
                Bukkit.broadcastMessage("Be inside +-x: " + ChatColor.GREEN  + nextBorder + ", +-z: " + ChatColor.GREEN.toString()  + nextBorder + ", or you will DIE");
            }
        }, 14400, 18000L); // 14400, 18000L

        // 1 MINUTE WARNING
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, () -> {
            if (main.getWorld().getWorldBorder().getSize()/2 <= 300.0){
                sizeDecrease = 100;
            }
            if (main.getWorld().getWorldBorder().getSize()/2 < 100.0){
                sizeDecrease = 10;
            }
            Bukkit.broadcastMessage("Border shrinking in: " + ChatColor.BOLD + ChatColor.DARK_RED + "1 MINUTE!");
            var nextBorder = (main.getWorld().getWorldBorder().getSize() / 2) - sizeDecrease;
            Bukkit.broadcastMessage("Be inside +-x: " + ChatColor.GREEN  + nextBorder + ", +-z: " + ChatColor.GREEN.toString()  + nextBorder + ", or you will DIE");
        }, 16800, 18000L); // 16800, 18000L

        // Shrink border
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.main, new Runnable() {
            @Override
            public void run() {
                if (main.getWorld().getWorldBorder().getSize()/2 <= 300.0){
                    sizeDecrease = 100;
                }
                if (main.getWorld().getWorldBorder().getSize()/2 < 100.0){
                    sizeDecrease = 10;
                }
                Bukkit.broadcastMessage("Border shrinking...");
                WorldBorder wb = main.getWorld().getWorldBorder();
                double currentSize = wb.getSize()/2;
                wb.setSize((currentSize-sizeDecrease)*2);
                wb.setDamageAmount(4);
                Bukkit.broadcastMessage("Next shrink: 15 Minutes");
            }
        }, 18000, 18000L); // 18000, 18000L
    }

    private void scatter(){
        // Bad random scatter. Should be more equal in the future
        final ArrayList<Location> locs = new ArrayList<>();
        for (int i = 0; i < teams.size(); ++i) {
            double worldRadius = main.getWorld().getWorldBorder().getSize() / 2;
            Location loc = new Location(
                    main.getWorld(), Numbers.random((int) (-1 * worldRadius),(int) worldRadius), 0, Numbers.random((int) (-1 * worldRadius), (int) worldRadius));
            loc.setY(main.getWorld().getHighestBlockYAt(loc));
            locs.add(loc);
        }

        // Teleport players over an interval.
        int iteration = 0;
        for (Team team : teams){
            Location teleportLocation = locs.get(iteration);
            for (UHCPlayer player : team.getMembers()){
                player.getPlayer().teleport(teleportLocation);
            }
            iteration++;
        }

    }

    private void playerStateOnStart(UHCPlayer player) {
        if (player == null)
            return;
        player.getPlayer().setGameMode(GameMode.SURVIVAL);
        player.getPlayer().setHealthScale(20.0);
        player.getPlayer().setMaxHealth(20.0);
        player.getPlayer().setHealth(20.0);
        player.getPlayer().setFoodLevel(20);
        player.getPlayer().getInventory().clear();
        player.getPlayer().setExp(0);
        player.getPlayer().setLevel(0);
        player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 30, 5));
        player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 30, 3));
        player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 30, 10));
    }

}
