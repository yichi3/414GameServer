package com.tonyzyc.model;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {
    private int id;
    private String playerUname;
    private Socket socket;
    private boolean isFirst;
    private List<Poker> pokers = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerUname() {
        return playerUname;
    }

    public void setPlayerUname(String playerUname) {
        this.playerUname = playerUname;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public List<Poker> getPokers() {
        return pokers;
    }

    public void setPokers(List<Poker> pokers) {
        this.pokers = pokers;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", playerUname='" + playerUname + '\'' +
                ", isFirst=" + isFirst +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id && Objects.equals(playerUname, player.playerUname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, playerUname);
    }

    public Player() {}

    public Player(int id, String playerUname, Socket socket, List<Poker> pokers) {
        this.id = id;
        this.playerUname = playerUname;
        this.socket = socket;
        this.pokers = pokers;
    }

    public Player(int id, String playerUname) {
        this.id = id;
        this.playerUname = playerUname;
    }
}
