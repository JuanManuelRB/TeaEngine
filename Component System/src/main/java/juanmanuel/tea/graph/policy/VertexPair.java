package juanmanuel.tea.graph.policy;

import java.lang.ref.WeakReference;

/**
 * Represents a pair of vertices.
 *
 * @param source the source vertex.
 * @param target the target vertex.
 * @param <U>    the type of the vertices.
 */
public record VertexPair<U extends juanmanuel.tea.graph.Vertex<U>>(WeakReference<U> source, WeakReference<U> target) {
    public VertexPair(U source, U target) {
        this(new WeakReference<>(source), new WeakReference<>(target));
    }
} // TODO: Remove from collection when the reference is collected. Assign a reference queue to the weak references.
