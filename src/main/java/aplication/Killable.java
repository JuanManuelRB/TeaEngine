package aplication;

@FunctionalInterface
public interface Killable<T> {
    void onKill(T killer);
}
