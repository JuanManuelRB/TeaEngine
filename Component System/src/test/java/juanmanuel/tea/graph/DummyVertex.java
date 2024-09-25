package juanmanuel.tea.graph;

public class DummyVertex extends ApplicationVertex<DummyVertex> {
    private final String name;
    public DummyVertex(String name) {
        super();
        this.name = name;
    }

    public DummyVertex() {
        super();
        this.name = "dummy";
    }

    @Override
    public String toString() {
        return STR."DummyVertex{name='\{name}\{'\''}\{'}'}";
    }
}
