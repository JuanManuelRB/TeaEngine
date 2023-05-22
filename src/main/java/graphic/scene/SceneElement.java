package graphic.scene;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

abstract class SceneElement<T> implements Iterable<SceneElement<?>>, Collection<SceneElement<?>> {
    @NotNull private final T element;
    @NotNull private final List<SceneElement<?>> elements;
    private SceneElement<?> parent;

    public SceneElement(@NotNull T element, @NotNull List<SceneElement<?>> elements) {
        this.element = element;
        this.elements = elements;
    }

    public SceneElement(@NotNull T element) {
        this(element, new ArrayList<>());
    }

    public T element() {
        return element;
    }

    public SceneElement<?> parent() {
        return parent;
    }

    public SceneElement<?>[] elements() {
        return (SceneElement<?>[]) elements.stream().toArray();
    }

    @Override
    public boolean add(SceneElement<?> sceneElement) {
        sceneElement.parent = this;
        return elements.add(sceneElement);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends SceneElement<?>> c) {
        return false;
    }

    @Override
    public int size() {
        return 1 + elements.parallelStream().mapToInt(SceneElement::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object element) {
        return element == this.element || elements.parallelStream().anyMatch((elem -> (elem.contains(element))));
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.contains(element) && c.parallelStream().allMatch((elements::contains));
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<SceneElement<?>> iterator() {
        return elements.iterator();
    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public Object[] toArray() {
        return elements.parallelStream().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return (T[]) elements.parallelStream().toArray();
    }

}

