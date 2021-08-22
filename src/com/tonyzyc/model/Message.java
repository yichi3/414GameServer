package com.tonyzyc.model;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    // message type
    private int typeId;
    private int playerId;
    private String playerUname;
    private String contentString;
    private List<Poker> pokers;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getContentString() {
        return contentString;
    }

    public void setContentString(String contentString) {
        this.contentString = contentString;
    }

    public List<Poker> getPokers() {
        return pokers;
    }

    public void setPokers(List<Poker> pokers) {
        this.pokers = pokers;
    }

    public String getPlayerUname() {
        return playerUname;
    }

    public void setPlayerUname(String playerUname) {
        this.playerUname = playerUname;
    }

    public Message() {}

    public Message(int typeId, int playerId, String playerUname, String contentString, List<Poker> pokers) {
        this.typeId = typeId;
        this.playerId = playerId;
        this.playerUname = playerUname;
        this.contentString = contentString;
        this.pokers = pokers;
    }
}
