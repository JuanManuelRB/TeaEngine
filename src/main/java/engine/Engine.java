package engine;

import org.lwjgl.Version;


/**
 *
 */
public abstract class Engine implements Runnable, AutoCloseable {
    private final Thread applicationThread;
    private final ApplicationLogic applicationLogic;

    public Engine(String applicationName, ApplicationLogic applicationLogic) {
        this.applicationLogic = applicationLogic;
        applicationThread = new Thread(this, applicationName);
    }

    public Engine(ApplicationLogic applicationLogic) {
        this.applicationLogic = applicationLogic;
        applicationThread = new Thread(this);
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
        //TODO: finalize window, finalize shader program, finalize render.
        applicationLogic.close();
    }

    /**
     * Initialize the application.
     *
     * @throws Exception
     */
    private void init() throws Exception {
        System.out.println("Versión de LWJGL: " + Version.getVersion());
        System.out.println("Inicializando " + applicationThread.getName());

        applicationLogic.init();
    }

    /**
     * Main loop. Executed until the logic signals to end.
     *
     * @throws Exception
     */
    private synchronized void loop() throws Exception {
        //TODO: no se debe utilizar Window.get(), se debe utilizar una instancia de AbstractWindow.
        while(!applicationLogic.closing()) {// TODO: Cambiar tecla escape (ESC, Esc) para cerrar ventana por otra o ninguna
            update();
        }
    }

    private void update() throws Exception {
        // Primero se actualiza la lógica y luego se actualizan los gráficos.
        applicationLogic.update(); // TODO: El numero de actualizaciones sera variable segun el tiempo disponible.
        applicationLogic.render(); // TODO: EL metodo renderizado debera efectuarse tantas veces como se indique.
    }
}
