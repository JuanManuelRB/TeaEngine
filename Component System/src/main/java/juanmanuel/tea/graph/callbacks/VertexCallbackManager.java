package juanmanuel.tea.graph.callbacks;

import juanmanuel.tea.graph.ApplicationGraph;
import juanmanuel.tea.graph.ApplicationVertex;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

// TODO: Value Object

/**
 * Manages the callbacks for vertex operations.
 * @param <V> The type of the vertex over which the callbacks are performed.
 */
public class VertexCallbackManager<V extends ApplicationVertex<V>> {
    private WeakHashMap<BiConsumer<V, ApplicationGraph<? super V>>, EnumSet<ApplicationVertex.VertexCallbackType>> vertexCallbacks;
    private WeakHashMap<Consumer<ApplicationGraph<? super V>>, EnumSet<ApplicationVertex.GraphCallbackType>> graphCallbacks;

    private Set<BiConsumer<?, ?>> storedBiConsumerCallbacks;
    private Set<Consumer<?>> storedConsumerCallbacks;

    /**
     * Adds a callback of a given operation type over a vertex.
     * The callback is stored in a weak reference to allow it to be garbage collected if it is not referenced elsewhere.
     * @param callback The callback to add.
     * @param type The type of operation that triggers the callback.
     */
    public void addCallback(BiConsumer<V, ApplicationGraph<? super V>> callback, ApplicationVertex.VertexCallbackType type) {
        if (vertexCallbacks == null)
            vertexCallbacks = new WeakHashMap<>();

        var set = vertexCallbacks.computeIfAbsent(callback, _ -> EnumSet.noneOf(ApplicationVertex.VertexCallbackType.class));
        set.add(type);
        vertexCallbacks.put(callback, set);
    }

    /**
     * Adds a callback of a given operation type over a graph.
     * The callback is stored in a weak reference to allow it to be garbage collected if it is not referenced elsewhere.
     * @param callback The callback to add.
     * @param type The type of operation that triggers the callback.
     */
    public void addCallback(Consumer<ApplicationGraph<? super V>> callback, ApplicationVertex.GraphCallbackType type) {
        if (graphCallbacks == null)
            graphCallbacks = new WeakHashMap<>();

        var set = graphCallbacks.computeIfAbsent(callback, _ -> EnumSet.noneOf(ApplicationVertex.GraphCallbackType.class));
        set.add(type);
        graphCallbacks.put(callback, set);
    }

    /**
     * Stores a callback so it is not garbage collected.
     * <p>The callbacks stored this way do not show up in the callback lists.</p>
     * It is intended to be used by the vertex to store its own callbacks, so they are not garbage collected.
     * @param callback The callback to store.
     * @return true if the callback was stored, false otherwise.
     */
    protected boolean storeCallback(Consumer<?> callback) {
        if (storedConsumerCallbacks == null)
            storedConsumerCallbacks = new HashSet<>();
        return storedConsumerCallbacks.add(callback);
    }

    protected boolean storeCallback(BiConsumer<?, ?> callback) {
        if (storedBiConsumerCallbacks == null)
            storedBiConsumerCallbacks = new HashSet<>();
        return storedBiConsumerCallbacks.add(callback);
    }

    protected void removeStoredConsumerCallbackIf(Predicate<Consumer<?>> predicate) {
        if (storedConsumerCallbacks != null) {
            storedConsumerCallbacks.removeIf(predicate);
        }
    }

    protected void removeStoredBiConsumerCallbackIf(Predicate<BiConsumer<?, ?>> predicate) {
        if (storedBiConsumerCallbacks != null) {
            storedBiConsumerCallbacks.removeIf(predicate);
        }
    }

    public void removeVertexCallback(BiConsumer<V, ApplicationGraph<V>> callback) {
        vertexCallbacks.remove(callback);
    }

    public void removeGraphCallback(Consumer<ApplicationGraph<V>> callback) {
        graphCallbacks.remove(callback);
    }

    public void removeVertexCallback(BiConsumer<V, ApplicationGraph<V>> callback, ApplicationVertex.VertexCallbackType type) {
        var set = vertexCallbacks.get(callback);
        if (set != null) {
            set.remove(type);
            if (set.isEmpty()) {
                vertexCallbacks.remove(callback);
            }
        }
    }

    public void removeGraphCallback(Consumer<ApplicationGraph<V>> callback, ApplicationVertex.GraphCallbackType type) {
        var set = graphCallbacks.get(callback);
        if (set != null) {
            set.remove(type);
            if (set.isEmpty()) {
                graphCallbacks.remove(callback);
            }
        }
    }

    public Set<BiConsumer<V, ApplicationGraph<? super V>>> getCallbacksFor(ApplicationVertex.VertexCallbackType type) {
        Set<BiConsumer<V, ApplicationGraph<? super V>>> result = Collections.newSetFromMap(new WeakHashMap<>());
        for (var entry : vertexCallbacks.entrySet()) {
            if (entry.getValue().contains(type)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Set<Consumer<ApplicationGraph<? super V>>> getCallbacksFor(ApplicationVertex.GraphCallbackType type) {
        Set<Consumer<ApplicationGraph<? super V>>> result = Collections.newSetFromMap(new WeakHashMap<>());
        for (var entry : graphCallbacks.entrySet()) {
            if (entry.getValue().contains(type)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
