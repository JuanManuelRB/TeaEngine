package engine;

import graphic.window.AbstractWindow;
import io.inputs.KeyListener;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

/**
 *
 */
public final class Engine implements Runnable, AutoCloseable{
    private final Thread gameEngine;
    private final AbstractLogic gameLogic;

    public Engine(String gameName, AbstractLogic game){
        gameEngine = new Thread(this, gameName);
        this.gameLogic = game;
    }

    /**
     *
     */
    @Override
    public void run() {
        try {
            init();
            loop();

        } catch (Exception e) {
            e.printStackTrace();//TODO

        } catch (Throwable throwable) {
            throwable.printStackTrace();

        } finally {
            end();

        }
    }

    @Override
    public void close() throws Exception {

    }


    public void startGame() {
        gameEngine.start();
    }

    /**
     * Metodo que inicializa GLFW Y OpenGL.
     *
     * @throws Exception
     */
    private void init() throws Exception{
        // El callback puede cambiarse para que sea mas util que solo la salida estandar.
        GLFWErrorCallback.createPrint(System.err).set();

        System.out.println("Versión de LWJGL: " + Version.getVersion());
        System.out.println("Inicializando LWJGL");

        // Check GLFW initialization
        if (!GLFW.glfwInit())
            throw new IllegalStateException("No ha sido posible inicializar GLFW");

        gameLogic.init();
    }

    /**
     * Bucle principal.
     *
     * @throws Exception
     */
    private void loop() throws Throwable {
        //TODO: no se debe utilizar Window.get(), se debe utilizar una instancia de AbstractWindow.
        while(!gameLogic.closing() && !KeyListener.get().activeKey(GLFW.GLFW_KEY_ESCAPE)) {// TODO: Cambiar tecla escape (ESC, Esc) para cerrar ventana por otra o ninguna
            // Primero se actualiza la lógica y luego se actualizan los gráficos.
            gameLogic.updateLogic(10); // TODO: El numero de actualizaciones sera variable segun el tiempo disponible.
            gameLogic.render(); // TODO: EL metodo renderizado debera efectuarse tantas veces como se indique.
        }
    }

    private void update() {}

    private void end(){
        //TODO: finalize window, finalize shader program, finalize render.
        gameLogic.end();
    }



}
