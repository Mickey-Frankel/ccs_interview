package server;

import java.util.Random;

public class GameLogic {
//    private static final int SECRET_NUMBER = 42;
    private static int[] primeArr = {2,3,5,7,11,13};
    private final Random random;

    public GameLogic(Random random){
        this.random = random;
    }
    public GameLogic(){
        this(new Random());
    }
    public int validateGuess(String input) throws IllegalArgumentException {
        // I've extended the validations by checking the input isn't null or merely spaces
        try {
            if (input == null || input.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            int guess = Integer.parseInt(input);
            if (guess < 1 || guess > 100) {
                throw new IllegalArgumentException("Number out of range, please guess between 1 and 100.");
            }
            return guess;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input, please enter a number.");
        }
    }

    private int reverse(int num){
        int reversed = 0;
        while(num > 0){
            reversed = reversed * 10 + num % 10;
            num = num/10;
        }
        return reversed;
    }
    private int getSecretNumber(){
        int secret = random.nextInt(1,101);
        if(secret % 2 == 0) {
            secret = reverse(secret);
        }
        else{
            secret += primeArr[random.nextInt(primeArr.length)];
        }
        if(secret >= 100) {
            return secret / 2;
        }
        else if (secret <50) {
            return secret * 2;
        }
        return secret;
    }
    public boolean checkGuessCorrectness(int guess) {
        return guess == getSecretNumber();
    }

    public String generatePrefix(int guess) {
        int formatChoice = random.nextInt(3);
        String prefix;

        switch (formatChoice) {
            case 0:
                prefix = (guess % 2 == 0)
                        ? "The number you selected is " + guess + " and it is even!"
                        : "The number you selected is " + guess + " and it is odd!";
                break;
            case 1:
                prefix = (guess > 100)
                        ? "You selected " + guess + ", a number greater than 100! Great choice!"
                        : "You selected " + guess + ", which is a small number!";
                break;
            case 2:
                int randomFact = random.nextInt(100);
                prefix = "The number " + guess + " has a special fact: " + randomFact + " is a random number generated.";
                break;
            default:
                prefix = "You selected " + guess + ".";
        }

        if (guess >= 0 && guess <= 50) {
            prefix += " Your guess is within the safe zone!";
        } else if (guess > 50 && guess <= 150) {
            prefix += " Be careful! Your guess is in the uncertain range.";
        } else {
            prefix += " Your guess is in the high-risk zone!";
        }

        return prefix;
    }

    // Part 2 - New classes for allowing multiplayer
    public static class Player {
        private final String id;
        private final PrintWriter writer;

        public Player(String id, PrintWriter writer) {
            this.id = id;
            this.writer = writer;
        }

        public String getId() {
            return id;
        }

        public void sendMessage(String message) {
            writer.println(message);
        }
    }

    public static class GameState {
        private final List<Player> players;
        private int currentPlayerIndex;
        private boolean gameEnded;
        private final int minPlayers;
        private final int maxPlayers;
        private int secretNumber;
        private final GameLogic gameLogic;
        private int totalGuesses;
        private List<Player> readyToRestartPlayers;

        public GameState(GameLogic gameLogic, int minPlayers=1, int maxPlayers=2) {
            this.gameLogic = gameLogic;
            this.secretNumber = gameLogic.getSecretNumber();
            this.players = new ArrayList<>();
            this.currentPlayerIndex = 0;
            this.gameEnded = false;
            this.minPlayers = minPlayers;
            this.maxPlayers = maxPlayers;
            this.totalGuesses = 0;
            this.readyToRestartPlayers = new ArrayList<>();
        }

        public boolean addPlayer(Player player) {
            if (players.size() >= maxPlayers) {
                return false;
            }
            return players.add(player);
        }

        public boolean isReadyToStart() {
            return players.size() >= minPlayers;
        }

        public boolean hasRoomForMorePlayers() {
            return players.size() < maxPlayers;
        }

        public void switchTurn() {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        public Player getCurrentPlayer() {
            return players.get(currentPlayerIndex);
        }

        public List<Player> getOtherPlayers() {
            List<Player> others = new ArrayList<>(players);
            others.remove(currentPlayerIndex);
            return others;
        }

        public void broadcastMessage(String message) {
            for (Player player : players) {
                player.sendMessage(message);
            }
        }

        public void broadcastToOthers(String message, Player excludePlayer) {
            for (Player player : players) {
                if (player != excludePlayer) {
                    player.sendMessage(message);
                }
            }
        }

        public boolean isGameEnded() {
            return gameEnded;
        }

        public void setGameEnded(boolean gameEnded) {
            this.gameEnded = gameEnded;
        }

        public int getSecretNumber() {
            return secretNumber;
        }

        public int getPlayerCount() {
            return players.size();
        }

        public String getGameStatus() {
            StringBuilder status = new StringBuilder();
            status.append("Players in game: ");
            for (int i = 0; i < players.size(); i++) {
                status.append(players.get(i).getId());
                if (i == currentPlayerIndex) {
                    status.append(" (current)");
                }
                if (i < players.size() - 1) {
                    status.append(", ");
                }
            }
            return status.toString();
        }

        public synchronized GameEndResult checkGameEnd(int guess, Player currentPlayer) {
            totalGuesses++;

            if (guess == secretNumber) {
                return new GameEndResult(true, currentPlayer.getId() + " wins! They correctly guessed " + secretNumber + "!");
            }
            return new GameEndResult(false, null);
        }

        public synchronized void handleGameEnd(String endMessage) {
            gameEnded = true;
            readyToRestartPlayers.clear();
            broadcastMessage(endMessage);
            broadcastMessage("Type 'restart' to play again or 'quit' to exit");
        }

        public synchronized boolean playerWantsRestart(Player player) {
            if (!readyToRestartPlayers.contains(player)) {
                readyToRestartPlayers.add(player);
                broadcastMessage(player.getId() + " wants to play again. (" +
                        readyToRestartPlayers.size() + "/" + players.size() + " players ready)");
            }
            return readyToRestartPlayers.size() == players.size();
        }

        public synchronized void restartGame() {
            secretNumber = gameLogic.getSecretNumber();
            gameEnded = false;
            totalGuesses = 0;
            readyToRestartPlayers.clear();
            currentPlayerIndex = 0; // First player starts again
            broadcastMessage("Game restarted! New number has been generated.");
            broadcastMessage("Try to guess the number between 1 and 100");
            getCurrentPlayer().sendMessage("It's your turn!");
            broadcastToOthers("Waiting for " + getCurrentPlayer().getId() + " to make a move...",
                    getCurrentPlayer());
        }

        public synchronized void removePlayer(Player player) {
            players.remove(player);
            readyToRestartPlayers.remove(player);
            broadcastMessage(player.getId() + " has left the game.");

            if (players.size() < minPlayers) {
                handleGameEnd("Not enough players to continue. Waiting for more players to join...");
            }
        }

        // Helper class for game end results
        public static class GameEndResult {
            public final boolean isGameOver;
            public final String message;

            public GameEndResult(boolean isGameOver, String message) {
                this.isGameOver = isGameOver;
                this.message = message;
            }
        }
    }
}

