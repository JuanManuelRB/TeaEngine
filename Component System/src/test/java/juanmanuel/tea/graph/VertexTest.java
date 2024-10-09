package juanmanuel.tea.graph;

import juanmanuel.tea.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static juanmanuel.tea.graph.policy.VertexPolicy.EdgeModificationVertexPolicy.CONNECT_CHILD_POLICY;
import static juanmanuel.tea.graph.validation.VertexOperationValidator.VerticesOperationValidation.CONNECT_CHILD_VALIDATION;
import static org.junit.jupiter.api.Assertions.*;

class VertexTest {
    @Test
    void policiesManagerIsNotNull() {
        DummyVertex vertex = new DummyVertex();
        assertNotNull(vertex.policiesManager(), "The policies manager should not be null");
    }

    @Test
    void validationsManagerIsNotNull() {
        DummyVertex vertex = new DummyVertex();
        assertNotNull(vertex.validationsManager(), "The validation manager should not be null");
    }

    @Test
    void callbacksManagerIsNotNull() {
        DummyVertex vertex = new DummyVertex();
        assertNotNull(vertex.callbacksManager(), "The callbacks manager should not be null");
    }

    @Test
    void policies() {
        DummyVertex vertex = new DummyVertex(true);
        DummyVertex other = new DummyVertex(true);

        assertTrue(vertex.policiesManager().isUnset(CONNECT_CHILD_POLICY, other));
        assertFalse(vertex.policiesManager().isAccepted(CONNECT_CHILD_POLICY, other));
        assertFalse(vertex.policiesManager().isRejected(CONNECT_CHILD_POLICY, other));

        vertex.policiesManager().accept(CONNECT_CHILD_POLICY, other);

        assertFalse(vertex.policiesManager().isUnset(CONNECT_CHILD_POLICY, other));
        assertTrue(vertex.policiesManager().isAccepted(CONNECT_CHILD_POLICY, other));
        assertFalse(vertex.policiesManager().isRejected(CONNECT_CHILD_POLICY, other));

        vertex.policiesManager().reject(CONNECT_CHILD_POLICY, other);

        assertTrue(vertex.policiesManager().isRejected(CONNECT_CHILD_POLICY, other));
        assertFalse(vertex.policiesManager().isAccepted(CONNECT_CHILD_POLICY, other));
        assertFalse(vertex.policiesManager().isUnset(CONNECT_CHILD_POLICY, other));

        vertex.policiesManager().unset(CONNECT_CHILD_POLICY, other);

        assertTrue(vertex.policiesManager().isUnset(CONNECT_CHILD_POLICY, other));
        assertFalse(vertex.policiesManager().isAccepted(CONNECT_CHILD_POLICY, other));
        assertFalse(vertex.policiesManager().isRejected(CONNECT_CHILD_POLICY, other));
    }

    @Test
    void validations() {
        DummyVertex vertex = new DummyVertex(true);
        DummyVertex other = new DummyVertex(true);

        assertInstanceOf(Result.Success.class, vertex.validationsManager().validateOperation(CONNECT_CHILD_VALIDATION, other));

        vertex.validationsManager().addOperationValidation(CONNECT_CHILD_VALIDATION, o -> Result.fail("Test: " + o));

        assertInstanceOf(Result.Failure.class, vertex.validationsManager().validateOperation(CONNECT_CHILD_VALIDATION, other));
    }

    @Test
    void addChildWithParentRejectPolicy() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex parent = new DummyVertex(true);
        DummyVertex child = new DummyVertex(true);
        graph.addVertex(parent);
        graph.addVertex(child);
    }

    @Test
    void addChildWithChildRejectPolicy() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        DummyVertex child = new DummyVertex(true);
        graph.addVertex(vertex);
        graph.addVertex(child);
        graph.addEdge(vertex, child);
        assertTrue(vertex.childrenIn(graph).contains(child), "The vertex should have the child");
    }

    @Test
    void ingressEdgesIsEmptyOnFirstGraphAddition() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        graph.addVertex(vertex);
        assertTrue(vertex.ingressEdgesIn(graph).isEmpty(), "The ingress edges should be empty on creation");
    }

    @Test
    void ingressEdges() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        List<DummyVertex> parentVertices = List.of(
                new DummyVertex("A", true),
                new DummyVertex("B", true),
                new DummyVertex("C", true),
                new DummyVertex("D", true),
                new DummyVertex("E", true),
                new DummyVertex("F", true),
                new DummyVertex("G", true),
                new DummyVertex("H", true),
                new DummyVertex("I", true),
                new DummyVertex("J", true)
        );
        graph.addVertex(vertex);

        int i = 0;
        for (; i < parentVertices.size(); i++) {
            DummyVertex v = parentVertices.get(i);
            assertEquals(i, vertex.ingressEdgesIn(graph).size(), "The number of ingress edges should be equal to the number of parents");
            vertex.addParent(v, graph);
            assertEquals(i + 1, vertex.ingressEdgesIn(graph).size(), "The number of ingress edges should increment after adding a parent");
        }

        List<DummyVertex> childVertices = List.of(
                new DummyVertex("A", true),
                new DummyVertex("B", true),
                new DummyVertex("C", true),
                new DummyVertex("D", true),
                new DummyVertex("E", true),
                new DummyVertex("F", true),
                new DummyVertex("G", true),
                new DummyVertex("H", true),
                new DummyVertex("I", true),
                new DummyVertex("J", true)
        );

        for (var v : childVertices) {
            vertex.addChild(v, graph);
            assertEquals(i, vertex.ingressEdgesIn(graph).size(), "Adding a child should not affect the ingress edges");
        }

    }

    @Test
    void egressEdgesIsEmptyOnFirstGraphAddition() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        graph.addVertex(vertex);
        assertTrue(vertex.egressEdgesIn(graph).isEmpty(), "The egress edges should be empty on creation");
    }

    @Test
    void egressEdges() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        List<DummyVertex> childVertices = List.of(
                new DummyVertex("A", true),
                new DummyVertex("B", true),
                new DummyVertex("C", true),
                new DummyVertex("D", true),
                new DummyVertex("E", true),
                new DummyVertex("F", true),
                new DummyVertex("G", true),
                new DummyVertex("H", true),
                new DummyVertex("I", true),
                new DummyVertex("J", true)
        );
        graph.addVertex(vertex);

        int i = 0;
        for (; i < childVertices.size(); i++) {
            DummyVertex v = childVertices.get(i);
            assertEquals(i, vertex.egressEdgesIn(graph).size(), "The number of egress edges should be equal to the number of parents");
            vertex.addChild(v, graph);
            assertEquals(i + 1, vertex.egressEdgesIn(graph).size(), "Adding a parent should not affect the egress edges");
        }

        List<DummyVertex> parentVertices = List.of(
                new DummyVertex("A", true),
                new DummyVertex("B", true),
                new DummyVertex("C", true),
                new DummyVertex("D", true),
                new DummyVertex("E", true),
                new DummyVertex("F", true),
                new DummyVertex("G", true),
                new DummyVertex("H", true),
                new DummyVertex("I", true),
                new DummyVertex("J", true)
        );

        for (var v : parentVertices) {
            vertex.addParent(v, graph);
            assertEquals(i, vertex.egressEdgesIn(graph).size(), "The number of egress edges should be equal to the number of children");
        }
    }

    @Test
    void ingressEdgesIsEmptyAfterGraphRemoval() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        DummyVertex vertex2 = new DummyVertex(true);
        graph.addVertex(vertex);
        vertex.addChild(vertex2, graph);
        graph.removeVertex(vertex);
        assertTrue(vertex.ingressEdgesIn(graph).isEmpty(), "The ingress edges should be empty after the graph removal");
    }

    @Test
    void egressEdgesIsEmptyAfterGraphRemoval() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        DummyVertex vertex2 = new DummyVertex(true);
        graph.addVertex(vertex);
        vertex.addChild(vertex2, graph);
        graph.removeVertex(vertex);
        assertTrue(vertex.egressEdgesIn(graph).isEmpty(), "The egress edges should be empty after the graph removal");
    }

    @Test
    void ingressEdgesIsEmptyAfterVertexRemovalAndReAddition() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        DummyVertex vertex2 = new DummyVertex(true);
        graph.addVertex(vertex);
        vertex.addChild(vertex2, graph);
        graph.removeVertex(vertex);
        graph.addVertex(vertex);
        assertTrue(vertex.ingressEdgesIn(graph).isEmpty(), "The ingress edges should be empty after the vertex removal and re-addition");
    }

    @Test
    void egressEdgesIsEmptyAfterVertexRemovalAndReAddition() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);
        DummyVertex vertex2 = new DummyVertex(true);
        graph.addVertex(vertex);
        vertex.addChild(vertex2, graph);
        graph.removeVertex(vertex);
        graph.addVertex(vertex);
        assertTrue(vertex.egressEdgesIn(graph).isEmpty(), "The egress edges should be empty after the vertex removal and re-addition");
    }

    @Test
    void hasChild() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex("Test Vertex", true);
        DummyVertex child = new DummyVertex("Child Vertex", true);

        assertFalse(vertex.hasChild(child, graph), "The vertex should not have any children");

        graph.addVertex(vertex);
        assertFalse(vertex.hasChild(child, graph), "The vertex should not have any children");

        graph.addVertex(child);
        assertFalse(vertex.hasChild(child, graph), "The vertex should not have any children");

        graph.addEdge(vertex, child);
        assertTrue(vertex.hasChild(child, graph), "The vertex should have the child");
    }

    @Test
    void hasChildType() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex("Test Vertex", true);
        DummyVertex child = new DummyVertex("Child Vertex", true);
        graph.addVertex(vertex);
        vertex.addChild(child, graph);
        assertTrue(vertex.childrenIn(graph).contains(child), "The vertex should have the child");
        assertTrue(vertex.hasChild(DummyVertex.class, graph), "The vertex should have the child");
    }

    @Test
    void hasParent() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex("Test Vertex", true);
        DummyVertex parent = new DummyVertex("Parent Vertex", true);

        assertFalse(vertex.hasParent(parent, graph), "The vertex should not have any parents");

        graph.addVertex(vertex);
        assertFalse(vertex.hasParent(parent, graph), "The vertex should not have any parents");

        graph.addVertex(parent);
        assertFalse(vertex.hasParent(parent, graph), "The vertex should not have any parents");

        graph.addEdge(parent, vertex);
        assertTrue(vertex.hasParent(parent, graph), "The vertex should have the parent");
    }

    @Test
    void hasParentType() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex("Test Vertex", true);
        DummyVertex parent = new DummyVertex("Parent Vertex", true);
        graph.addVertex(vertex);
        vertex.addParent(parent, graph);
        assertTrue(vertex.parentsIn(graph).contains(parent), "The vertex should have the parent");
        assertTrue(vertex.hasParent(DummyVertex.class, graph), "The vertex should have the parent");
    }

    @Test
    void egressEdgesIn() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex("Test Vertex", true);

        assertEquals(0, vertex.egressEdgesIn(graph).size(), "If the vertex is not in the graph, the egress edges should be empty");

        graph.addVertex(vertex);
        assertEquals(0, vertex.egressEdgesIn(graph).size(), "Egress edges should not contain any edge if the vertex has no children");

        DummyVertex[] vertices = new DummyVertex[10];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex("Vertex " + i, true);
            switch (graph.addVertex(vertices[i])) {
                case Result.Failure<DummyVertex, Graph.VertexAdditionFailure> v ->
                        Assertions.fail("The vertex addition operation should be successful\n"
                        + "The error is: " + v.cause().getClass() + ": " + v.cause().message());
                case Result.Success<DummyVertex, Graph.VertexAdditionFailure> v -> {
                }
            }

            assertEquals(0, vertex.egressEdgesIn(graph).size(), "The vertex " + i + " should not be in the egress edges");
        }

        for (int i = 0; i < vertices.length; i++) {

            switch (graph.addEdge(vertex, vertices[i])) {
                case Result.Failure<ApplicationEdge, Graph.EdgeAdditionFailure> v ->
                        Assertions.fail("The edge addition operation should be successful\n"
                        + "The error is: " + v.cause().getClass() + ": " + v.cause().message());
                case Result.Success<ApplicationEdge, Graph.EdgeAdditionFailure> v -> {
                }
            }
            assertEquals(i + 1, vertex.egressEdgesIn(graph).size(), "The vertex " + i + " should be in the egress edges");
        }
        System.out.println(graph);
    }

    @Test
    void ingressEdgesIn() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);

        assertEquals(0, vertex.ingressEdgesIn(graph).size());

        graph.addVertex(vertex);
        assertEquals(0, vertex.ingressEdgesIn(graph).size());

        DummyVertex[] vertices = new DummyVertex[10];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex("Vertex " + i, true);
            graph.addVertex(vertices[i]);
            assertEquals(0, vertex.ingressEdgesIn(graph).size());
        }

        for (int i = 0; i < vertices.length; i++) {
            graph.addEdge(vertices[i], vertex);
            assertEquals(i + 1, vertex.ingressEdgesIn(graph).size());
        }
    }

    @Test
    void childrenIn() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);

        assertEquals(0, vertex.childrenIn(graph).size());

        graph.addVertex(vertex);
        assertEquals(0, vertex.childrenIn(graph).size());

        DummyVertex[] vertices = new DummyVertex[10];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex("Vertex " + i, true);
            graph.addVertex(vertices[i]);
            assertEquals(0, vertex.childrenIn(graph).size());
        }

        for (int i = 0; i < vertices.length; i++) {
            graph.addEdge(vertex, vertices[i]);
            assertEquals(i + 1, vertex.childrenIn(graph).size());
        }
        assertTrue(vertex.childrenIn(graph).containsAll(Arrays.asList(vertices)));
    }

    @Test
    void parentsIn() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);

        assertEquals(0, vertex.parentsIn(graph).size());

        graph.addVertex(vertex);
        assertEquals(0, vertex.parentsIn(graph).size());

        DummyVertex[] vertices = new DummyVertex[10];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex("Vertex " + i, true);
            graph.addVertex(vertices[i]);
            assertEquals(0, vertex.parentsIn(graph).size());
        }

        for (int i = 0; i < vertices.length; i++) {
            graph.addEdge(vertices[i], vertex);
            assertEquals(i + 1, vertex.parentsIn(graph).size());
        }
        assertTrue(vertex.parentsIn(graph).containsAll(Arrays.asList(vertices)));
    }

    @Test
    void descendantsIn() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);

        assertEquals(0, vertex.descendantsIn(graph).size());

        graph.addVertex(vertex);
        assertEquals(0, vertex.descendantsIn(graph).size());

        DummyVertex[] childrenVertices = new DummyVertex[10];
        for (int i = 0; i < childrenVertices.length; i++) {
            childrenVertices[i] = new DummyVertex("Vertex " + i, true);
            graph.addVertex(childrenVertices[i]);
            assertEquals(0, vertex.descendantsIn(graph).size());
        }

        for (int i = 0; i < childrenVertices.length; i++) {
            graph.addEdge(vertex, childrenVertices[i]);
            assertEquals(i + 1, vertex.descendantsIn(graph).size());
        }

        assertTrue(vertex.descendantsIn(graph).containsAll(Arrays.asList(childrenVertices)));

        DummyVertex[] grandChildrenVertices = new DummyVertex[10];
        for (int i = 0; i < grandChildrenVertices.length; i++) {
            grandChildrenVertices[i] = new DummyVertex("Vertex " + i, true);
            graph.addVertex(grandChildrenVertices[i]);
        }

        for (var childVertex : childrenVertices) {
            for (DummyVertex grandChildrenVertex : grandChildrenVertices) {
                graph.addEdge(childVertex, grandChildrenVertex);
            }
        }

        assertEquals(20, vertex.descendantsIn(graph).size());
    }

    @Test
    void ancestorsIn() {
        DummyGraph graph = new DummyGraph(true);
        DummyVertex vertex = new DummyVertex(true);

        assertEquals(0, vertex.ancestorsIn(graph).size());

        graph.addVertex(vertex);
        assertEquals(0, vertex.ancestorsIn(graph).size());

        DummyVertex[] parentVertices = new DummyVertex[10];
        for (int i = 0; i < parentVertices.length; i++) {
            parentVertices[i] = new DummyVertex("Vertex " + i, true);
            graph.addVertex(parentVertices[i]);
            assertEquals(0, vertex.ancestorsIn(graph).size());
        }

        for (int i = 0; i < parentVertices.length; i++) {
            graph.addEdge(parentVertices[i], vertex);
            assertEquals(i + 1, vertex.ancestorsIn(graph).size());
        }

        assertTrue(vertex.ancestorsIn(graph).containsAll(Arrays.asList(parentVertices)));

        DummyVertex[] grandParentVertices = new DummyVertex[10];
        for (int i = 0; i < grandParentVertices.length; i++) {
            grandParentVertices[i] = new DummyVertex("Vertex " + i, true);
            graph.addVertex(grandParentVertices[i]);
        }

        for (var parentVertex : parentVertices) {
            for (DummyVertex grandParentVertex : grandParentVertices) {
                graph.addEdge(grandParentVertex, parentVertex);
            }
        }

        assertEquals(20, vertex.ancestorsIn(graph).size());
    }

    @Test
    void edgePathTo() {
        DummyGraph graph = new DummyGraph();

        List<DummyVertex> vertices = List.of(
                new DummyVertex("A"),
                new DummyVertex("B"),
                new DummyVertex("C"),
                new DummyVertex("D"),
                new DummyVertex("E"),
                new DummyVertex("F"),
                new DummyVertex("G"),
                new DummyVertex("H"),
                new DummyVertex("I"),
                new DummyVertex("J")
        );

        for (DummyVertex vertex : vertices) {
            graph.addVertex(vertex);
        }

        for (int i = 0; i < vertices.size() - 1; i++) {
            graph.addEdge(vertices.get(i), vertices.get(i + 1));
        }

        for (int i = 0; i < vertices.size(); i++) {
            assertEquals(9 - i, vertices.get(i).edgePathTo(vertices.get(9), graph).size());
        }
    }

    @Test
    void edgePathFrom() {
        DummyGraph graph = new DummyGraph();

        List<DummyVertex> vertices = List.of(
                new DummyVertex("A"),
                new DummyVertex("B"),
                new DummyVertex("C"),
                new DummyVertex("D"),
                new DummyVertex("E"),
                new DummyVertex("F"),
                new DummyVertex("G"),
                new DummyVertex("H"),
                new DummyVertex("I"),
                new DummyVertex("J")
        );

        for (DummyVertex vertex : vertices) {
            graph.addVertex(vertex);
        }

        for (int i = 0; i < vertices.size() - 1; i++) {
            graph.addEdge(vertices.get(i), vertices.get(i + 1));
        }

        for (int i = 0; i < vertices.size(); i++) {
            assertEquals(9 - i, vertices.get(9).edgePathFrom(vertices.get(i), graph).size());
        }

    }

    @Test
    void shortestPathTo() { // TODO: 2021-10-14  Implement test

    }

    @Test
    void shortestPathFrom() {

    }
}