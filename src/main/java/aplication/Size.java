package aplication;

public record Size(long width, long height, long depth) {
	public Size {
		if (width < 0)
			throw new IllegalArgumentException("The width cant be negative");

		if (height < 0)
			throw new IllegalArgumentException("The height cant be negative");

		if (depth < 0)
			throw new IllegalArgumentException("The depth cant be negative");
	}

	public long volume() {
		return width * height * depth;
	}


}