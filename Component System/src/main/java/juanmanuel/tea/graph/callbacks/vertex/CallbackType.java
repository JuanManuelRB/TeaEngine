package juanmanuel.tea.graph.callbacks.vertex;

/// The types of callbacks that can be added to a vertex.
/// This is just a marker interface to group the callback types.
public sealed interface CallbackType permits GraphCallbackType, VertexCallbackType {
}
