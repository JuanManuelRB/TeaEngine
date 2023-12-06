package aplication.objects;

import aplication.update.Updater;

import java.util.*;

/**
 * This class represents a game object. A game object is a node in a graph of game objects.
 */
public abstract class GameObject {
    // The immediate ascendants of this game object.
    private final Set<GameObject> parents = new HashSet<>(); // Maybe substitute with adjacency list

    // The immediate descendants of this game object.
    private final Set<GameObject> children = new HashSet<>(); // Maybe substitute with adjacency list

    // The updaters of this game object.
    private final Set<Updater<?, ?>> updaters = new HashSet<>();

    private boolean inGraph = false;

    /**
     * Adds the updater to the game object.
     * @param updater the updater to be added
     */
    public final <E extends GameObject & Updater<E, ?>> void addUpdater(E updater) {
        Objects.requireNonNull(updater);
        updaters.add(updater);
    }

    /**
     * Adds the game object as a child of this game object.
     * If this game object is subscribed to any updater, the child game object will be added to the updater if it can update it.
     * @param gameObject the child game object
     * @throws IllegalArgumentException when an attempt to introduce any circular relation occurs
     */
    public final void addChild(GameObject gameObject) throws IllegalArgumentException {
        Objects.requireNonNull(gameObject);
        if (this == gameObject || isAscendant(gameObject))
            throw new IllegalArgumentException("Graph cycles or self reference is not allowed");

        if (children.contains(gameObject))
            return;

        children.add(gameObject);
        gameObject.addParent(this);

        processUpdaters(updaters, gameObject);

        gameObject.enterGraphIfNeeded(this);
    }

    /**
     * Adds the game object as a parent of this game object
     * @param gameObject the parent game object
     * @throws IllegalArgumentException when an attempt to introduce any circular relation occurs
     */
    public final void addParent(GameObject gameObject) throws IllegalArgumentException {
        Objects.requireNonNull(gameObject);
        if (this == gameObject || isDescendant(gameObject))
            throw new IllegalArgumentException(STR."Graph cycles or self reference is not allowed");

        if (parents.contains(gameObject))
            return;

        parents.add(gameObject);
        gameObject.addChild(this);

        this.enterGraphIfNeeded(gameObject);
    }

    /**
     * Processes the updaters of this game object.
     * @param updaters the updaters to be processed
     * @param gameObject the game object to be processed
     */
    private void processUpdaters(Set<? extends Updater<?, ?>> updaters, Object gameObject) {
        for (Updater<?, ?> updater : updaters) {
            processUpdaterHelper(updater, gameObject);
        }
    }

    /**
     * Processes the updater.
     * @param updater the updater to be processed
     * @param gameObject the game object to be processed
     * @param <S> the type of the updater
     * @param <U> the type of the updated
     */
    private <S extends Updater<S, U>, U> void processUpdaterHelper(Updater<S, U> updater, Object gameObject) {
        Class<U> updatedClass = updater.updatedClass();
        if (updatedClass.isInstance(gameObject)) {
            U updated = updatedClass.cast(gameObject);
            updater.add(updated);
        }
    }

    /**
     * Removes the game object as a child of this game object if it is present.
     * @param gameObject the child game object
     */
    public final void removeChild(GameObject gameObject) {
        Objects.requireNonNull(gameObject);
        children.remove(gameObject);
        gameObject.removeParent(this);
        exitGraph(this);
    }

    /**
     * Removes the game object as a parent of this game object if it is present.
     * @param gameObject the parent game object
     */
    public final void removeParent(GameObject gameObject) {
        Objects.requireNonNull(gameObject);
        parents.remove(gameObject);
        gameObject.removeChild(this);
        exitGraph(gameObject);
    }

    /**
     * Returns an unmodifiable view of the set of children of this game object.
     * @return The set of children of this game object.
     */
    public final Set<GameObject> children() {
        return Collections.unmodifiableSet(children);
    }

    /**
     * Returns an unmodifiable view of the set of parents of this game object.
     * @return The set of parents of this game object.
     */
    public final Collection<GameObject> parents() {
        return Collections.unmodifiableSet(parents);
    }

    /**
     * The default iteration mode is preorder.
     * @return The ascendants of this game object.
     */
    public final Set<GameObject> ascendants() {
        return ascendants(IterationMode.PREORDER);
    }

    /**
     * The iteration mode does not ensure the order of the objects in the returned collection.
     * @param mode The iteration mode.
     * @return The ascendants of this game object.
     */
    public final Set<GameObject> ascendants(IterationMode mode) {
        return switch (mode) { // TODO: search and return collection
            case INORDER -> inorderedSearch(false);
            case PREORDER -> preorderedSearch(false);
            case POSTORDER -> postorderedSearch(false);
            case BREADTH -> breadthSearch(false);
        };
    }

    /**
     * The default iteration mode is preorder.
     * @return The descendants of this game object.
     */
    public final Collection<GameObject> descendants() {
        return descendants(IterationMode.PREORDER);
    }

    /**
     * The iteration mode does not ensure the order of the objects in the returned collection.
     * @return The descendants of this game object.
     */
    public final Collection<GameObject> descendants(IterationMode mode) {
        return null;
    }

    /**
     * Checks if the game object is a child of this game object.
     * @param gameObject The game object to check.
     * @return True if the game object is a child of this game object, false otherwise.
     */
    public final boolean isChild(GameObject gameObject) {
        return children.contains(gameObject);
    }

    /**
     * Checks if the game object is a parent of this game object.
     * @param gameObject The game object to check.
     * @return True if the game object is a parent of this game object, false otherwise.
     */
    public final boolean isParent(GameObject gameObject) {
        return parents.contains(gameObject);
    }

    /**
     * Checks if the game object is an ascendant of this game object.
     * @param gameObject The game object to check.
     * @return True if the game object is an ascendant of this game object, false otherwise.
     */
    public final boolean isAscendant(GameObject gameObject) {
        return false;
        //return ascendants().stream().anyMatch(go -> go == gameObject);
    }

    /**
     * Checks if the game object is a descendant of this game object.
     * @param gameObject The game object to check.
     * @return True if the game object is a descendant of this game object, false otherwise.
     */
    public final boolean isDescendant(GameObject gameObject) {
        return false;
        //return descendants().stream().anyMatch(go -> go == gameObject);
    }

    /**
     * Checks if this game object has a child of the given class.
     * @param clazz The class of the game object to check.
     * @return True if the game object is a child of this game object, false otherwise.
     * @param <T> The type of the game object to check.
     */
    public final <T> boolean hasChild(Class<T> clazz) {
        return children.stream().anyMatch(clazz::isInstance);
    }

    /**
     * Checks if this game object has a parent of the given class.
     * @param clazz The class of the game object to check.
     * @return True if the game object is a parent of this game object, false otherwise.
     * @param <T> The type of the game object to check.
     */
    public final <T> boolean hasParent(Class<T> clazz) {
        return parents.stream().anyMatch(clazz::isInstance);
    }

    /**
     * Checks if this game object has an ascendant of the given class.
     * @param clazz The class of the game object to check.
     * @return True if the game object is an ascendant of this game object, false otherwise.
     * @param <T> The type of the game object to check.
     */
    public final <T> boolean hasAscendant(Class<T> clazz) {
        return ascendants().stream().anyMatch(clazz::isInstance);
    }

    /**
     * Checks if this game object has a descendant of the given class.
     * @param clazz The class of the game object to check.
     * @return True if the game object is a descendant of this game object, false otherwise.
     * @param <T> The type of the game object to check.
     */
    public final <T> boolean hasDescendant(Class<T> clazz) {
        return descendants().stream().anyMatch(clazz::isInstance);
    }

    private Set<GameObject> inorderedSearch(boolean parent) {
        return null;
    }

    private Set<GameObject> preorderedSearch(boolean parent) {
        return null;
    }

    private Set<GameObject> postorderedSearch(boolean parent) {
        return null;
    }

    private Set<GameObject> breadthSearch(boolean parent) {
        return null;
    }

    private void enterGraphIfNeeded(GameObject parent) {
        if (inGraph)
            return;

        enterGraph(parent);
        inGraph = true;
    }

    /**
     * Called when the game object is added to the graph.
     * @param parent The parent game object. Usually a scene.
     */
    protected void enterGraph(GameObject parent) {}

    /**
     * Called when the game object is removed from the graph.
     * @param parent The parent game object. Usually a scene.
     */
    protected void exitGraph(GameObject parent) {}

    /**
     * Iteration modes for the graph search.
     */
    public enum IterationMode {
        INORDER,
        PREORDER,
        POSTORDER,
        BREADTH,
    }
}