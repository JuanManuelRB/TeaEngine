package juanmanuel.tea.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationVertexTest {

    @Test
    void graph() {
        var vertex = new DummyVertex();
        assertFalse(vertex.graph().isPresent()); // The vertex is not connected to any graph after creation

        var graph = new DummyGraph();
        graph.policiesManager.accept(ADD_VERTEX, vertex);
        graph.addVertex(vertex); // The vertex is in the graph, but it is not aware of it

        assertFalse(vertex.graph().isPresent());
        assertTrue(graph.containsVertex(vertex));

        assertFalse(vertex.handleGraphReplace(graph).isSuccessful());
        vertex.policiesManager.accept(SET_GRAPH_POLICY, graph);

        assertDoesNotThrow(() -> assertTrue(vertex.handleGraphReplace(graph).isSuccessful()));

        assertTrue(vertex.graph().isPresent());
        assertTrue(graph.containsVertex(vertex));
    }

    @Test
    void shouldReplaceGraph() {
        var vertex = new DummyVertex();
        assertFalse(vertex.graph().isPresent()); // The vertex is not connected to any graph after creation

        var graph = new DummyGraph();
        assertFalse(vertex.shouldReplaceGraph(graph).isSuccessful()); // The policy is not defined, it resolves to the default behavior

        vertex.policiesManager.accept(SET_GRAPH_POLICY, graph);
        graph.policiesManager.accept(ADD_VERTEX, vertex);

        assertTrue(vertex.shouldReplaceGraph(graph).isSuccessful());
    }

    @Test
    void handleGraphReplaceWhenNoGraph() {
        var vertex = new DummyVertex();
        var graph = new DummyGraph();

        assertFalse(vertex.handleGraphReplace(graph).isSuccessful()); // The policy is not defined, it resolves to the default behavior
        assertFalse(vertex.hasGraph(graph));
        assertFalse(graph.containsVertex(vertex));

        graph.policiesManager.accept(ADD_VERTEX, vertex);
        vertex.policiesManager.accept(SET_GRAPH_POLICY, graph);

        assertTrue(vertex.handleGraphReplace(graph).isSuccessful());
        assertTrue(vertex.hasGraph(graph));
        assertTrue(graph.containsVertex(vertex));
    }

    @Test
    void handleGraphReplaceWhenGraphPresent() {
        var vertex = new DummyVertex();
        var graph = new DummyGraph();
        var newGraph = new DummyGraph();

        vertex.policiesManager.accept(SET_GRAPH_POLICY, graph);
        graph.policiesManager.accept(ADD_VERTEX, vertex);
        newGraph.policiesManager.accept(ADD_VERTEX, vertex);
        vertex.handleGraphReplace(graph);

        assertTrue(vertex.handleGraphReplace(newGraph).isSuccessful());
        assertFalse(vertex.hasGraph(graph));
        assertTrue(vertex.hasGraph(newGraph));
        assertFalse(graph.containsVertex(vertex));
        assertTrue(newGraph.containsVertex(vertex));
    }

    @Test
    void shouldRemoveGraph() {

    }

    @Test
    void removeGraph() {
    }

    @Test
    void children() {
        DummyVertex parent = new DummyVertex();
        assertTrue(parent.children().isEmpty());

        DummyVertex child = new DummyVertex();
    }

    @Test
    void parents() {
    }

    @Test
    void neighbors() {
    }

    @Test
    void descendants() {
    }

    @Test
    void ancestors() {
    }

    @Test
    void siblings() {
    }

    @Test
    void fullSiblings() {
    }

    @Test
    void halfSiblings() {
    }

    @Test
    void hasChild() {
    }

    @Test
    void testHasChild() {
    }

    @Test
    void numberOfChildren() {
    }

    @Test
    void hasParent() {
    }

    @Test
    void testHasParent() {
    }

    @Test
    void numberOfParents() {
    }

    @Test
    void hasDescendant() {
    }

    @Test
    void testHasDescendant() {
    }

    @Test
    void numberOfDescendants() {
    }

    @Test
    void hasAncestor() {
    }

    @Test
    void testHasAncestor() {
    }

    @Test
    void numberOfAncestors() {
    }

    @Test
    void hasChildren() {
    }

    @Test
    void hasParents() {
    }

    @Test
    void addOnConnectChildCallback() {
    }

    @Test
    void addOnConnectParentCallback() {
    }

    @Test
    void addOnDisconnectChildCallback() {
    }

    @Test
    void addOnDisconnectParentCallback() {
    }

    @Test
    void addOnEnterGraphCallback() {
    }

    @Test
    void addOnExitGraphCallback() {
    }

    @Test
    void shouldConnectChild() {
    }

    @Test
    void shouldConnectParent() {
    }

    @Test
    void shouldDisconnectChild() {
    }

    @Test
    void shouldDisconnectParent() {
    }

    @Test
    void shouldAddChild() {
    }

    @Test
    void shouldAddParent() {
    }

    @Test
    void shouldRemoveChild() {
    }

    @Test
    void connectChild() {
    }

    @Test
    void testConnectChild() {
    }

    @Test
    void addChild() {
    }

    @Test
    void testAddChild() {
    }

    @Test
    void connectParent() {
    }

    @Test
    void testConnectParent() {
    }

    @Test
    void addParent() {
    }

    @Test
    void testAddParent() {
    }

    @Test
    void disconnectChild() {
    }

    @Test
    void removeChild() {
    }

    @Test
    void disconnectParent() {
    }

    @Test
    void removeParent() {
    }

    @Test
    void disconnectAll() {
    }

    @Test
    void shouldCallOnEnterGraphFor() {
    }

    @Test
    void shouldCallOnLeaveGraphFor() {
    }
}