package engine;

//import game.Main;

import game.PokeGameLogic;
import graphic.window.Window;

public class Main {

    public static void main(String[] args) {
        try {
            var window = Window.builder().build();
            new Engine("GAME", new PokeGameLogic(window), window).startGame();

        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}
