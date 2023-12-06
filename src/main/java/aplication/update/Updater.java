package aplication.update;

/**
 * An interface for objects that can update other updated objects.
 * @param <Self> the type of the updater
 * @param <Upd> the type of the updated
 */
public interface Updater<Self extends Updater<Self, Upd>, Upd> {

    /**
     * Updates the updated objects.
     */
    void update();

    /**
     * Adds the game object to the updater.
     * @param gameObject the game object to add
     */
    void add(Upd gameObject);

    Class<Self> updaterClass();
    Class<Upd> updatedClass();
}
