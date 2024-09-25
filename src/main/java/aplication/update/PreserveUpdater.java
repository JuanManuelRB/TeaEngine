package aplication.update;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * An annotation for classes that can be subscribed to updaters. The classes must be subclasses of {@link aplication.objects.GameObject}.
 * Allows to preserve the updaters when leaving the game object graph.
 * @see GameObject
 * @see Updater
 */
@Target(java.lang.annotation.ElementType.TYPE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface PreserveUpdater {
    Class<? extends Updater<?, ?>>[] updaters();
}
