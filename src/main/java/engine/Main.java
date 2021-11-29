package engine;

//import game.Main;

import game.PokeGameLogic;

public class Main {
    public static void main(String[] args) {
        try {
            new Engine("GAME", new PokeGameLogic()).startGame();

        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}
