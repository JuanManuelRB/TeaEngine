package aplication.update;

/**
 * An interface for objects that can update other updated objects.
 * @param <Self> the type of the updater
 * @param <Upd> the type of the updated
 */
public interface Updater<Self extends Updater<Self, Upd>, Upd extends Updated> {

    /**
     * Starts one update cycle.
     */
    void update();

    /**
     * Updates the given game object.
     * @param updated the game object to update
     */
    void update(Upd updated);

    /**
     * Gets the name of the updater.
     * @return the name of the updater
     */
    String name();

    /**
     * Adds the game object to the updater.
     * Called by the game objects when they are added to another game object that has an updater.
     * @param updated the game object to add
     */
    boolean subscribe(Upd updated);

    /**
     * Removes the game object from the updater.
     * Called by the game objects when they are removed from another game object that has an updater or when all the parents of the game object are removed.
     * @param updated the game object to remove
     */
    boolean unsubscribe(Upd updated);

    /**
     * Checks if the updater updates the game object.
     * @param updated the game object to check
     * @return true if the updater updates the game object
     */
    boolean updates(Upd updated);

    /**
     * Gets the class of the updater.
     * @return the class of the updater
     */
    Class<Self> updaterClass();

    /**
     * Gets the class of the updated.
     * @return the class of the updated
     */
    Class<Upd> updatedClass();
}
