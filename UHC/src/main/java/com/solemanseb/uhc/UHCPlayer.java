package com.solemanseb.uhc;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class UHCPlayer {
    private Player player;
    private Team team;
    private ArrayList<Team> teamInviteSourceList = new ArrayList<>();
    private ArrayList<Team> requestedTeamsList;

    public UHCPlayer(Player player, Team team) {
        this.player = player;
        this.team = team;
    }

    public UHCPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void leaveTeam() {
        team.removeMember(this);
        this.team = null;
    }

    public ArrayList<Team> getRequestedTeamsList() {
        return requestedTeamsList;
    }

    public void sendRequest(Team team) {
        this.requestedTeamsList.add(team);
        team.addPlayerRequest(this);
    }


    public ArrayList<Team> getTeamInviteSourceList() {
        return teamInviteSourceList;
    }

    public void addTeamInviteSource(Team teamInviteSource) {
        this.teamInviteSourceList.add(teamInviteSource);
    }

    public void removeRequest(Team team){
        this.requestedTeamsList.remove(team);
    }

    public void removeInviteFrom(Team team){
        requestedTeamsList.remove(team);
    }

    public void clearRequests(){
        requestedTeamsList.clear();
    }


}

