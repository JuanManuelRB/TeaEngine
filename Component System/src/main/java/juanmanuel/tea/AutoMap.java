package juanmanuel.tea;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * A map that automatically generates keys for its values.
 * The keys are generated using a function that takes a value and returns a key.
 * The provided function must be able to generate unique keys for the values for the map to behave correctly.
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
 */
public class AutoMap<K, V> implements Map<K, V> {
    private final Map<K, V> map;
    private final Function<V, K> keyGenerator;

    /**
     * Creates an AutoMap.
     * @param map The map to use.
     * @param keyGenerator The function that generates keys for the values.
     */
    public AutoMap(Map<K, V> map, Function<V, K> keyGenerator) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(keyGenerator);
        this.map = map;
        this.keyGenerator = keyGenerator;
    }

    /**
     * Puts a value in the map.
     * The key for the value is generated using the key generator function.
     * The key can be retrieved using the key generator function and computing the key for the value.
     * @param value The value to put in the map.
     * @return the value that was put in the map.
     */
    public V put(V value) {
        K key = keyGenerator.apply(value);
        return put(key, value);
    }

    /**
     * Gets the key generator function.
     * @return the key generator function.
     */
    public Function<V, K> keyGenerator() {
        return keyGenerator;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
//        throw new UnsupportedOperationException("Use put(V value) instead.");
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
