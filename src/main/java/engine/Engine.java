package engine;

import engine.graphic.Window;
import engine.io.inputs.KeyListener;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

/**
 *
 */
public class Engine implements Runnable{
    private final Thread gameEngine;
    private final Logic gameLogic;

    public Engine(String gameName, Logic game){
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

        } catch (Exception e){
            e.printStackTrace();//TODO
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            end();
        }
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

        System.out.println("Version de LWJGL " + Version.getVersion());
        System.out.println("Inicializando LWJGL");

        // Check GLFW initialization
        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        Window.get(); // Init window
        //TODO: create the Window instance and check for exceptions?

        gameLogic.init();




    }

    /**
     * Bucle principal.
     *
     * @throws Exception
     */
    private void loop() throws Throwable {
        //TODO
        while(!Window.closeWindow() && !KeyListener.activeKey(GLFW.GLFW_KEY_ESCAPE)) {// TODO: Cambiar tecla escape (ESC, Esc) para cerrar ventana por otra o ninguna
            // Primero se actualiza la lógica y luego se actualizan los gráficos.
            gameLogic.updateLogic(10); // TODO: El numero de actualizaciones sera variable segun el tiempo disponible.
            gameLogic.render(Window.get()); // TODO: EL metodo renderizado debera efectuarse tantas veces como se indique.
        }
    }

    private void update() {
    }

    private void end(){
        //TODO: finalize window, finalize shader program, finalize render.
        gameLogic.end();
    }

}
