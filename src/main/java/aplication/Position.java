package aplication;


public record Position(long x, long y, long z) {
    public Position() {
        this(0, 0, 0);
    }

    public Position plus(Position other) {
        return new Position(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Position minus(Position other) {
        return new Position(this.x - other.x, this.y - other.y, this.z - other.z);
    }


	
}