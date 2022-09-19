import aplication.Application;
import engine.Logic;
import graphic.window.AbstractWindow;

import java.nio.file.Path;

public class Pokegame extends Application {

    public static void main(String[] args) {
//        Pokegame poke = new GameBuilder<Pokegame>.setTitle("Poke Game").setScene();
        new Pokegame().start();

    }

    @Override
    public void init() {
        // Load saved game, load textures, load entities, load game, etc.


    }




    /*
    private static class Cube extends Element {
        private Path material;
        private Logic logic;

        public Cube(String name, Box shape) {
        }

        public Cube setMaterial(Path path) {
            this.material = path;
            return this;
        }

        public Cube setMaterial(String path) {
            return setMaterial(Path.of(path));
        }

        public Cube setLogic(Logic logic) {
            this.logic = logic;
            return this;
        }

        public void render(AbstractWindow window){
            window.render(this);
        }

    }

    public static class Element {
    }
    */
}
