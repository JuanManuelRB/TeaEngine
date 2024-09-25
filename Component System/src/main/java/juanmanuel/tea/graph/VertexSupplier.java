package juanmanuel.tea.graph;

import java.util.function.Supplier;

@FunctionalInterface
public interface VertexSupplier<V extends Vertex<V>> extends Supplier<V> {
}
