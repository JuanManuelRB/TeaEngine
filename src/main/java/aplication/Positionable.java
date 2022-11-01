package aplication;


public interface Positionable {
    Position position();

    Position position(Position position);

    default double distance(Position position) {
        var vec = new Vector(position(), position);
        return vec.magnitude();
    }
}
