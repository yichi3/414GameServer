package com.tonyzyc.view;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tonyzyc.model.Message;
import com.tonyzyc.model.Player;
import com.tonyzyc.model.Poker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class MainFrame {
    private int numOfPlayers = -1;
    private int numOfDecks = 1;
    private final int numOfTeams;
    public Map<Integer, Player> players = new HashMap<>();
    public Map<Integer, Set<Player>> team = new HashMap<>();
    public List<Integer> numOfPlayerWithNoPoker = new ArrayList<>();
    private int index = 0;
    public List<Poker> allPokers = new ArrayList<>();
    public List<Poker> shangGongPokers = new ArrayList<>();
    public List<Poker> huiGongPokers = new ArrayList<>();
    // FSM for the game
    // step = 0: player ready info
    public int step = 0;
    private int readyPlayerCount = 0;

    public MainFrame(int numOfTeams) {
        this.numOfTeams = numOfTeams;
        for (int i = 0; i < numOfTeams; i++) {
            this.numOfPlayerWithNoPoker.add(0);
        }
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
        allPokers.clear();
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
                    Poker poker = new Poker(id++, suit + " " + name, color, num, false);
                    num++;
                    allPokers.add(poker);
                }
            }
        }
        Collections.shuffle(allPokers);
    }

    public void dealPoker() {
        for (int j = 0; j < numOfPlayers; j++) {
            // clear all players card before the game
            players.get(j).getPokers().clear();
        }
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

    private void setHun(String hun) {
        int hunNum = 0;
        switch (hun) {
            case "A":
                hunNum = 14;
                break;
            case "K":
                hunNum = 13;
                break;
            case "Q":
                hunNum = 12;
                break;
            case "J":
                hunNum = 11;
                break;
            case "2":
                hunNum = 15;
                break;
            default:
                hunNum = Integer.parseInt(hun);
                break;
        }
        for (Poker p: allPokers) {
            p.setHun(p.getNum() == hunNum);
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

    private void askPlayerSetHun() {
        sendMessageToClient(players.get(0).getPlayerUname());
    }

    private List<Poker> getPokerListFromJSON(JSONObject obj) {
        List<Poker> list = new ArrayList<>();
        JSONArray pokerListJSONArray = obj.getJSONArray("pokers");
        for (Object o : pokerListJSONArray) {
            JSONObject pokerJSONObject = (JSONObject) o;
            int pokerId = pokerJSONObject.getInteger("id");
            String pokerName = pokerJSONObject.getString("name");
            String pokerColor = pokerJSONObject.getString("color");
            int pokerNum = pokerJSONObject.getInteger("num");
            boolean isHun = pokerJSONObject.getBoolean("hun");
            Poker poker = new Poker(pokerId, pokerName, pokerColor, pokerNum, isHun);
            list.add(poker);
        }
        return list;
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
                        int playerId = msgJSONObject.getInteger("playerId");
                        if (numOfPlayers == -1) {
                            numOfPlayers = Integer.parseInt(msgJSONObject.getString("contentString"));
                            numOfDecks = numOfPlayers / 2;
                        }
                        if (playerId == -1) {
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
                            index++;
                        }
                        readyPlayerCount++;
                        System.out.println("Message from client, " + msg + " is online");
                        System.out.println("Current number of players is " + players.size());
                        if (readyPlayerCount == numOfPlayers) {
                            System.out.println("All players are ready, begin game");
                            askPlayerSetHun();
                            step = 1;
                        }
                    } else if (step == 1) {
                        // get hun number
                        JSONObject msgJSONObject = JSONObject.parseObject(msg);
                        String content = msgJSONObject.getString("contentString");
                        createPokers();
                        setHun(content);
                        dealPoker();
                        // sendMessageToClient("server go to step 2");
                        step = 3;
                    } else if (step == 2) {
                        for (int i = 0; i < numOfPlayers; i++) {
                            sendMessageToClient(msg);
                            JSONObject msgJSONObject = JSONObject.parseObject(msg);
                            List<Poker> gongPokers = getPokerListFromJSON(msgJSONObject);
                            shangGongPokers.addAll(gongPokers);
                        }
                        System.out.println("Get all Gong");
                        for (Poker p: shangGongPokers) {
                            System.out.println(p);
                        }
                        step = 3;
                    } else if (step == 3) {
                        sendMessageToClient(msg);
                        JSONObject msgJSONObject = JSONObject.parseObject(msg);
                        int typeId = msgJSONObject.getInteger("typeId");
                        int playerId = msgJSONObject.getInteger("playerId");
                        if (typeId == 10) {
                            // this means playerId has no poker, record the message
                            int teamIndex = playerId % numOfTeams;
                            int num = numOfPlayerWithNoPoker.get(teamIndex) + 1;
                            numOfPlayerWithNoPoker.set(teamIndex, num);
                            System.out.println(num + " " + numOfPlayers + " " + numOfTeams);
                            if (num == numOfPlayers / numOfTeams) {
                                // this means all players in team teamIndex has no poker, they win
                                System.out.println("Send done message");
                                Message message = new Message(100, 0, "", "", null);
                                String msgJSONString = JSON.toJSONString(message);
                                sendMessageToClient(msgJSONString);
                                readyPlayerCount = 0;
                                step = 0;
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
