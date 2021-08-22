package com.tonyzyc.view;

import com.tonyzyc.util.GameUtil;

public class Main {
    public static void main(String[] args) {
        int numOfPlayers = GameUtil.numOfPlayers;
        new MainFrame(numOfPlayers);
    }
}
