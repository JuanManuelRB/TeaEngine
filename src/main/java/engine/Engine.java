package engine;

import io.inputs.KeyListener;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;


/**
 *
 */
public abstract class Engine implements Runnable, AutoCloseable{
    private final Thread gameEngine;
    private final AbstractLogic gameLogic;

    public Engine(String gameName, AbstractLogic game) {
        gameEngine = new Thread(this, gameName);
        this.gameLogic = game;
    }

    @Override
    public void run() {
        try (this) {
            init();
            loop();

        } catch (Exception e) {
            e.printStackTrace();//TODO

        } catch (Throwable throwable) {
            throwable.printStackTrace();

        }
    }

    @Override
    public void close() throws Exception {
        end();
    }

    /**
     * Initialize GLFW and OpenGL.
     *
     * @throws Exception
     */
    private void init() throws Exception {
        System.out.println("Versión de LWJGL: " + Version.getVersion());
        System.out.println("Inicializando LWJGL");

        gameLogic.init();
    }

    /**
     * Bucle principal.
     *
     * @throws Exception
     */
    private void loop() throws Exception {
        //TODO: no se debe utilizar Window.get(), se debe utilizar una instancia de AbstractWindow.
        while(!gameLogic.closing()) {// TODO: Cambiar tecla escape (ESC, Esc) para cerrar ventana por otra o ninguna
            // Primero se actualiza la lógica y luego se actualizan los gráficos.
            gameLogic.update(); // TODO: El numero de actualizaciones sera variable segun el tiempo disponible.
            gameLogic.render(); // TODO: EL metodo renderizado debera efectuarse tantas veces como se indique.
        }
    }

    private void update() {}

    private void end(){
        //TODO: finalize window, finalize shader program, finalize render.
        gameLogic.end();
    }
}
