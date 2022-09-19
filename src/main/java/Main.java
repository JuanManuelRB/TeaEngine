import graphic.scene.Scene;
import graphic.window.Window;

public class Main {
    public static void main(String[] args) {
        try (var window = Window.builder().title("Nombre de Ventana").build()) {
            var generationRules = Generation.get();
            var scene = Scene.builder().dimensions().generation(generationRules);
            var game = new GameApplication();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}