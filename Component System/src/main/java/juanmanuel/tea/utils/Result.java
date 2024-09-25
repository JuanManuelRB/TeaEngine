package juanmanuel.tea.utils;

import java.util.Objects;
import java.util.function.Consumer;

public sealed interface Result<S, F> {
    boolean isSuccessful();
    boolean isFailure();
    S orElseThrow() throws Throwable;

    record Failure<S, F>(F cause) implements Result<S, F> {
        public Failure {
            Objects.requireNonNull(cause, "Throwable cannot be null");
        }

        @Override
        public boolean isSuccessful() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public S orElseThrow() throws Throwable {
            throwIfThrowable();
            throw new RuntimeException(cause().toString());
        }

        public void throwIfThrowable() throws Throwable {
            if (cause instanceof Throwable t)
                throw t;
        }

        static <U, V> Failure<U, V> of(Failure<?, V> f) {
            return new Failure<>(f.cause());
        }
    }

    record Success<S, F>(S value) implements Result<S, F> {
        public Success() {
            this(null);
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public S orElseThrow() throws RuntimeException {
            return value();
        }
    }

    static <T, U> Result<T, U> success(T value) {
        return new Success<>(value);
    }

    static <T, U> Result<T, U> success() {
        return new Success<>();
    }

    static <T, F> Result<T, F> fail(F cause) {
        return new Failure<>(cause);
    }

    default void ifSuccessful(Consumer<S> consumer) {
        if (isSuccessful()) {
            consumer.accept(((Success<S, F>) this).value);
        }
    }

    default void ifFailure(Consumer<F> consumer) {
        if (isFailure()) {
            consumer.accept(((Failure<S, F>) this).cause);
        }
    }



}
