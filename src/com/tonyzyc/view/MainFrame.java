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
import java.util.*;

public class MainFrame {
    private final int numOfPlayers;
    private final int numOfDecks;
    private final int numOfTeams;
    public Map<Integer, Player> players = new HashMap<>();
    public Map<Integer, Set<Player>> team = new HashMap<>();
    public List<Integer> numOfPlayerWithNoPoker = new ArrayList<>();
    public List<Player> ranking = new ArrayList<>();
    private int index = 0;
    public List<Poker> allPokers = new ArrayList<>();
    // FSM for the game
    // step = 0: player ready info
    public int step = 0;
    private List<Integer> hunNum = new ArrayList<>();

    public MainFrame(int numOfPlayers, int numOfTeams) {
        // create all pokers
        this.numOfPlayers = numOfPlayers;
        this.numOfDecks = numOfPlayers / 2;
        this.numOfTeams = numOfTeams;
        for (int i = 0; i < numOfTeams; i++) {
            this.numOfPlayerWithNoPoker.add(0);
            this.hunNum.add(3);
        }
        // start game with first team's hun number
        createPokers(hunNum.get(0));
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

    public void createPokers(int hunNum) {
        String[] names = new String[] {"3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2"};
        String[] suits = new String[] {"Spades", "Hearts", "Clubs", "Diamonds"};
        // red and black poker
        for (int i = 0; i < numOfDecks; i++) {
            Poker redPoker = new Poker(1, "Red Joker", "Red", 17, false);
            Poker blackPoker = new Poker(2, "Black Joker", "Black", 16, false);
            allPokers.add(redPoker);
            allPokers.add(blackPoker);
            int id = 3;
            for (String suit : suits) {
                int num = 3;
                for (String name : names) {
                    String color = (suit.equals("Hearts") || suit.equals("Diamonds")) ? "Red" : "Black";
                    Poker poker = new Poker(id++, suit + " " + name, color, num, num == hunNum);
                    num++;
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
        for (Map.Entry<Integer, Player> entry : players.entrySet()) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(entry.getValue().getSocket().getOutputStream());
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
                        player.setSocket(socket);
                        players.put(index, player);
                        // set teams
                        int teamIndex = index % numOfTeams;
                        Set<Player> teamSet = team.getOrDefault(teamIndex, new HashSet<>());
                        teamSet.add(player);
                        team.put(teamIndex, teamSet);
                        System.out.println(player);
                        System.out.println("Message from client, " + msg + " is online");
                        System.out.println("Current number of players is " + players.size());
                        if (players.size() == numOfPlayers) {
                            System.out.println("All players are ready, begin game");
                            dealPoker();
                            step = 1;
                        }
                        index++;
                    } else if (step == 1) {
                        sendMessageToClient(msg);
                        JSONObject msgJSONObject = JSONObject.parseObject(msg);
                        int typeId = msgJSONObject.getInteger("typeId");
                        int playerId = msgJSONObject.getInteger("playerId");
                        if (typeId == 10) {
                            //this means playerId has no poker, record the message
                            ranking.add(players.get(playerId));
                            int teamIndex = playerId % numOfTeams;
                            int num = numOfPlayerWithNoPoker.get(teamIndex) + 1;
                            numOfPlayerWithNoPoker.set(teamIndex, num);
                            if (num == numOfPlayers / numOfTeams) {
                                // this means all players in team teamIndex has no poker, they win

                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
