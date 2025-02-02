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
}

