package juanmanuel.tea.graph.callbacks.vertex;

/// The types of callbacks that can be added to a vertex.
public enum VertexCallbackType implements CallbackType {
    ON_CONNECT_CHILD,
    ON_DISCONNECT_CHILD,
    ON_CONNECT_PARENT,
    ON_DISCONNECT_PARENT;
}
