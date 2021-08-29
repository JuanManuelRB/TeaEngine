package game

import engine.Engine
import game.GameLogic

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            Engine("GAME", GameLogic()).startGame()

        } catch (excp: Exception) {
            excp.printStackTrace()
            System.exit(-1)
        }
    }

}