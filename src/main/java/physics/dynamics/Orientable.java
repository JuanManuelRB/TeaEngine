package physics.dynamics;


import juanmanuel.gealma.vga.vga3.Trivector3;
import juanmanuel.gealma.vga.vga3.Vector3;

public interface Orientable {

    /**
     * The forward vector of the object. Normalized
     * @return The forward vector of the object.
     */
    public Vector3 forward();
    public Vector3 up();

    default Vector3 right() {
//        return forward().outer(up()).dual();TODO: Check this
        return forward().outer(up()).times(Trivector3.ONE);
    }

    default Vector3 backward() {
        return forward().unaryMinus();
    }

    default Vector3 down() {
        return up().unaryMinus();
    }

    default Vector3 left() {
        return up().outer(forward()).times(Trivector3.ONE);
    }

}
