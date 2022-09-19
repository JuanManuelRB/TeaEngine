package game

import aplication.Application

fun main() {
    val application: Application = PokeApplicationLogic()
    application.start()
}

class PokeApplicationLogic(): Application() {
    override fun init() {
//        addScene(scene);
    }

}