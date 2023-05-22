import graphic.window.Window;

public class ExampleApplication {
    public static void main(String[] args) {
        try (var window = Window.builder().title("Nombre de Ventana").build()) {

//            var scene = Scene.builder().dimensions(new Vector3(500, 500, 0)).setView();
//            scene.display(window);
//
//            var game = new GameApplication();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}