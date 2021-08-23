package com.tonyzyc.view;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tonyzyc.model.Player;
import com.tonyzyc.model.Poker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainFrame {
    private final int numOfPlayers;
    private final int numOfDecks;
    public List<Player> players = new ArrayList<>();
    private int index = 0;
    public List<Poker> allPokers = new ArrayList<>();
    // FSM for the game
    // step = 0: player ready info
    public int step = 0;

    public MainFrame(int numOfPlayers) {
        // create all pokers
        this.numOfPlayers = numOfPlayers;
        this.numOfDecks = numOfPlayers / 2;
        createPokers();
        try {
            // create server side socket
            ServerSocket serverSocket = new ServerSocket(8888);
            // accept client socket
            while (true) {
                Socket socket = serverSocket.accept();
                // create thread to handle client socket
                AcceptThread acceptThread = new AcceptThread(socket);
                acceptThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPokers() {
        String[] names = new String[] {"3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2"};
        String[] suits = new String[] {"Spades", "Hearts", "Clubs", "Diamonds"};
        // red and black poker
        for (int i = 0; i < numOfDecks; i++) {
            Poker redPoker = new Poker(1, "Red Joker", 17);
            Poker blackPoker = new Poker(2, "Black Joker", 16);
            allPokers.add(redPoker);
            allPokers.add(blackPoker);
            int id = 3;
            for (String suit : suits) {
                int num = 3;
                for (String name : names) {
                    Poker poker = new Poker(id++, suit + " " + name, num++);
                    allPokers.add(poker);
                }
            }
        }
        Collections.shuffle(allPokers);
    }

    public void dealPoker() {
        for (int i = 0; i < allPokers.size(); i++) {
            for (int j = 0; j < numOfPlayers; j++) {
                if (i % numOfPlayers == j) {
                    players.get(j).getPokers().add(allPokers.get(i));
                }
            }
        }
        // send each player's poker to the client side
        for (int i = 0; i < numOfPlayers; i++) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(players.get(i).getSocket().getOutputStream());
                String jsonString = JSON.toJSONString(players);
                dataOutputStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // send message to all clients
    public void sendMessageToClient(String msg) {
        for (Player player : players) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(player.getSocket().getOutputStream());
                dataOutputStream.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class AcceptThread extends Thread {
        private final Socket socket;
        public AcceptThread(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                while (true) {
                    String msg = dataInputStream.readUTF();
                    System.out.println(msg);
                    if (step == 0) {
                        // get player ready info
                        JSONObject msgJSONObject = JSON.parseObject(msg);
                        String playerUname = msgJSONObject.getString("playerUname");
                        Player player = new Player(index, playerUname);
                        player.setFirst(index == 0);
                        index++;
                        player.setSocket(socket);
                        players.add(player);
                        System.out.println(player);
                        System.out.println("Message from client, " + msg + " is online");
                        System.out.println("Current number of players is " + players.size());
                        if (players.size() == numOfPlayers) {
                            System.out.println("All players are ready, begin game");
                            dealPoker();
                            step = 1;
                        }
                    } else if (step == 1) {
                        sendMessageToClient(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
