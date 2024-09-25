package juanmanuel.tea.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public non-sealed class ApplicationEdge extends DefaultWeightedEdge implements GraphElement {
    @Override
    public ApplicationVertex<?> getSource() {
        return (ApplicationVertex<?>) super.getSource();
    }

    @Override
    public ApplicationVertex<?> getTarget() {
        return (ApplicationVertex<?>) super.getTarget();
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public String toString() {
        return StringTemplate.STR."\{getSource()} --\{getWeight()}--> \{getTarget()}";
    }
}
