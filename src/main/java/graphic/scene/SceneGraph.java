package graphic.scene;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.function.Supplier;

public class SceneGraph<E> extends DirectedWeightedMultigraph<SceneObject, E> {
    public SceneGraph(Class<? extends E> edgeClass) {
        super(edgeClass);
    }

    public SceneGraph(Supplier<SceneObject> vertexSupplier, Supplier<E> edgeSupplier) {
        super(vertexSupplier, edgeSupplier);
    }
}
