package server;

import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final GameLogic.GameState gameState;
    private final GameLogic.Player player;
    private final GameLogic gameLogic;

    public ClientHandler(Socket socket, GameLogic.GameState gameState, GameLogic.Player player) {
        this.clientSocket = socket;
        this.gameState = gameState;
        this.player = player;
        this.gameLogic = new GameLogic();
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             gameState.broadcastMessage("Welcome to the Guessing Game! Enter a number between 1 and 100.");
             String inputLine;

            while ((inputLine = in.readLine()) != null) {
                try {
                    int guess = gameLogic.validateGuess(inputLine);
                    boolean isCorrect = gameLogic.checkGuessCorrectness(guess);
                    String prefix = gameLogic.generatePrefix(guess);
                    if (guess == gameState.getSecretNumber()) {
                        gameState.broadcastMessage(prefix + " " + player.getId() + " won! The number was " + guess);
                        gameState.setGameEnded(true);
                        break;
                    } else {
                        gameState.broadcastMessage(prefix + " " + player.getId() + " guessed wrong.");
                        gameState.switchTurn();
                        gameState.getCurrentPlayer().sendMessage("It's your turn!");
                    }
                } catch (IllegalArgumentException e) {
                    out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gameState.removePlayer(player);
    }
}
