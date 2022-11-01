package aplication;

public record Orientation(long i, long j, long k) {
    public Orientation() {
        this(0, 0, 0);
    }
}
