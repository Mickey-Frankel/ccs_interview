package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {

    private final GameLogic gameLogic = new GameLogic();
    private GameLogic.GameState currentGame = null;
    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private synchronized void handleNewConnection(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String playerId = "Player" + UUID.randomUUID().toString().substring(0, 4); // assigning ID to players
            GameLogic.Player newPlayer = new GameLogic.Player(playerId, out);

            if (currentGame == null || currentGame.isGameEnded()) {
                // Start new game
                currentGame = new GameLogic.GameState(gameLogic.getSecretNumber(), MIN_PLAYERS, MAX_PLAYERS);
                currentGame.addPlayer(newPlayer);
                newPlayer.sendMessage("Welcome! Waiting for more players to join...");
                System.out.println("New game created. Waiting for players...");
            } else if (currentGame.hasRoomForMorePlayers()) {
                // Add to existing game
                currentGame.addPlayer(newPlayer);
                newPlayer.sendMessage("Welcome! Joined existing game.");
                currentGame.broadcastToOthers(playerId + " has joined the game!", newPlayer);
                System.out.println(playerId + " joined the game");

                if (currentGame.isReadyToStart() && currentGame.getPlayerCount() == MIN_PLAYERS) {
                    startGame();
                } else {
                    currentGame.broadcastMessage(currentGame.getGameStatus());
                }
            } else {
                // Game is full
                newPlayer.sendMessage("Sorry, the current game is full. Please try again later.");
                System.out.println("Rejected connection - game is full");
                clientSocket.close();
                return;
            }

            // Start a new handler thread for this client
            new ClientHandler(clientSocket, currentGame).start();

        } catch (IOException e) {
            System.err.println("Error handling new connection: " + e.getMessage());
            e.printStackTrace();
            try {
                clientSocket.close();
            } catch (IOException closeError) {
                closeError.printStackTrace();
            }
        }
    }

    private void startGame() {
        currentGame.broadcastMessage("Game is starting! " + currentGame.getGameStatus());
        currentGame.broadcastMessage("Try to guess the number between 1 and 100");
        currentGame.getCurrentPlayer().sendMessage("It's your turn!");
        currentGame.broadcastToOthers("Waiting for " + currentGame.getCurrentPlayer().getId() + " to make a move...",
                currentGame.getCurrentPlayer());
        System.out.println("Game started with " + currentGame.getPlayerCount() + " players");
    }

}
