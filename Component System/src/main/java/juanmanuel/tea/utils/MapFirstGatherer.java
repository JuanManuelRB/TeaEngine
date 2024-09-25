package juanmanuel.tea.utils;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Gatherer;

public record MapFirstGatherer<T, U>(Function<T, U> mapFunction, Predicate<U> predicate) implements Gatherer<T, T, U> {

    @Override
    public Integrator<T, T, U> integrator() {
        return null; // TODO: Implement
    }
}