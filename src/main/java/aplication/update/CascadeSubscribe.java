package aplication.update;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation for classes that can be subscribed to updaters. The classes must be subclasses of {@link GameObject}.
 * @see GameObject
 * @see Updater
 */
@Target(ElementType.TYPE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface CascadeSubscribe {
    Class<? extends Updater<?, ?>>[] value();
}
