package juanmanuel.tea.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public non-sealed class ApplicationEdge extends DefaultWeightedEdge implements GraphElement {
    @Override
    public Vertex<?> getSource() {
        return (Vertex<?>) super.getSource();
    }

    @Override
    public Vertex<?> getTarget() {
        return (Vertex<?>) super.getTarget();
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public String toString() {
        return String.format("%s --%f--> %s", getSource(), getWeight(), getTarget());
    }
}
