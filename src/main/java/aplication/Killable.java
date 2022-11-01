package aplication;

@FunctionalInterface
public interface Killable<T> {
    void kill(T killer);
}
