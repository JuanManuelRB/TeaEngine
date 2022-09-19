package graphic.scene;


public class Transition extends View {
    public Transition(Scene scene) {
        super(scene);
    }

    public void transitionTo(View view) {

    }

    @Override
    public View getViewOf(Scene scene) {
        return null;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
