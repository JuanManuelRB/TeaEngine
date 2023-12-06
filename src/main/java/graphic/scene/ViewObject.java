package graphic.scene;

import graphic.Renderable;
import graphic.render.View;
import graphic.render.program.Mesh;

import java.util.List;


public class ViewObject extends SceneObject implements View {
    private Camera camera;

    public ViewObject(Camera camera) {
        this.camera = camera;
    }

    @Override
    public Camera camera() {
        return camera;
    }

    @Override
    public SceneObject scene() {
        return null;
    }


    public List<Mesh> meshes() {
        return objectGraph.vertexSet().parallelStream()
                .filter((object) -> (object instanceof Renderable))
                .map((object) -> ((Renderable) object).mesh())
                .toList();
    }

}
