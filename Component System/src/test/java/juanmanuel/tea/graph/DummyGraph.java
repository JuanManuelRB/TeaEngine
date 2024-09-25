package juanmanuel.tea.graph;

public final class DummyGraph extends ApplicationGraph<DummyVertex> {
    public DummyGraph() {
        super((Class<? extends ApplicationEdge<DummyVertex>>) new ApplicationEdge<DummyVertex>().getClass());
    }
}
