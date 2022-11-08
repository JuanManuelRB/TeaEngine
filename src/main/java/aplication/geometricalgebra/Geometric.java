package aplication.geometricalgebra;



public sealed interface Geometric permits GeometricObject, GeometricObject.Bivector, GeometricObject.Trivector, GeometricObject.Vector {
    double scalar();
    GeometricObject.Vector vector();
    GeometricObject.Bivector bivector();
    GeometricObject.Trivector trivector();

//    public static GeometricObject add(Geometric first, Geometric second) {
//        return new GeometricObject()
//    }
}
