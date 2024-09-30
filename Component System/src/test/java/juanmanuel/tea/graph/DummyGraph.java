package juanmanuel.tea.graph;

public final class DummyGraph extends Graph<DummyVertex, ApplicationEdge> {
    public DummyGraph(boolean acceptUnsetPolicies) {
        this();
        acceptUnsetPolicy(acceptUnsetPolicies);
    }

    public DummyGraph() {
        super(ApplicationEdge.class);
    }
}
