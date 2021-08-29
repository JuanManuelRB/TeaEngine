package engine;

//import game.Main;

import game.GameLogic;

public class Main {
    public static void main(String[] args) {
        try {
            new Engine("GAME", new GameLogic()).startGame();

        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}
