package juanmanuel.tea.graph;

public class DummyVertex extends Vertex<DummyVertex> {
    private final String name;

    public DummyVertex(String name, boolean acceptUnsetPolicies) {
        this(name);
        acceptOnUnsetPolicy(acceptUnsetPolicies);
    }

    public DummyVertex(boolean acceptUnsetPolicies) {
        this();
        acceptOnUnsetPolicy(acceptUnsetPolicies);
    }

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
        return "DummyVertex{name='" + name + '\'' + '}';
    }
}
