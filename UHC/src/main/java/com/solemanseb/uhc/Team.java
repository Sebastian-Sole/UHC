package com.solemanseb.uhc;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Team {
    // Fields
    private String teamName;
    private ArrayList<UHCPlayer> members = new ArrayList<>();
    private ArrayList<UHCPlayer> inviteTargets = new ArrayList<>();
    private ArrayList<UHCPlayer> playerRequestsList = new ArrayList<>();
    private String teamColor;

    // Constructor

    public Team(String teamName, UHCPlayer leader, String color) {
        this.teamName =  color + "[" + color + teamName + color + "]";
        members.add(leader);
        this.teamColor = color;
        leader.setTeam(this);
    }

    public void addMember(UHCPlayer player){
        members.add(player);
        player.setTeam(this);
        player.getPlayer().setPlayerListName(teamColor + teamName + teamColor + player.getPlayer().getName());
        player.getPlayer().setDisplayName(teamColor + teamName + teamColor + player.getPlayer().getName());
        player.getPlayer().setCustomName(teamColor + teamName + teamColor + player.getPlayer().getName());
        player.getPlayer().setCustomNameVisible(true);
    }

    public void removeMember(UHCPlayer player){
        player.setTeam(null);
        player.getPlayer().setPlayerListName(player.getOriginalName());
        player.getPlayer().setDisplayName(player.getOriginalName());
        player.getPlayer().setCustomName(player.getOriginalName());
        player.getPlayer().setCustomNameVisible(true);
        this.members.remove(player);
    }

    public void removeAllMembers(){
        for (UHCPlayer player : members)
            removeMember(player);
    }

    public String getTeamName() {
        return teamName;
    }

    public ArrayList<UHCPlayer> getMembers() {
        return members;
    }
    public int size(){
       return members.size();
    }

    public void sendInvite(UHCPlayer player){
        this.inviteTargets.add(player);
        player.addTeamInviteSource(this);
    }


    public ArrayList<UHCPlayer> getInviteTargets() {
        return inviteTargets;
    }


    public ArrayList<UHCPlayer> getPlayerRequestsList() {
        return playerRequestsList;
    }

    public void addPlayerRequest(UHCPlayer player) {
        this.playerRequestsList.add(player);
    }

    public void removePlayerRequest(UHCPlayer player){
        this.playerRequestsList.remove(player);
    }

    public void removeInvitedPlayer(UHCPlayer player){
        this.inviteTargets.remove(player);
    }

}
