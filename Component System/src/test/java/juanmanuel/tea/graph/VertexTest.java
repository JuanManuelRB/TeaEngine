package juanmanuel.tea.graph;

import juanmanuel.tea.utils.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VertexTest {

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
        DummyGraph graph = new DummyGraph();
        DummyVertex vertex = new DummyVertex();

        assertEquals(0, vertex.ancestorsIn(graph).size());

        graph.addVertex(vertex);
        assertEquals(0, vertex.ancestorsIn(graph).size());

        DummyVertex[] parentVertices = new DummyVertex[10];
        for (int i = 0; i < parentVertices.length; i++) {
            parentVertices[i] = new DummyVertex("Vertex " + i);
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
            grandParentVertices[i] = new DummyVertex("Vertex " + i);
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