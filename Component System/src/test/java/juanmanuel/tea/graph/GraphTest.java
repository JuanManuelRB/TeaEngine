package juanmanuel.tea.graph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void roots() {
        DummyGraph graph = new DummyGraph();
        DummyVertex[] vertices = new DummyVertex[10];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex(STR."Vertex \{i}");
            graph.addVertex(vertices[i]);
        }

        assertEquals(10, graph.vertexSet().size());
        assertEquals(10, graph.roots().size());

        DummyVertex[] children = new DummyVertex[10];

        for (int i = 0; i < children.length; i++) {
            children[i] = new DummyVertex(STR."Child \{i}");
            graph.addVertex(children[i]);
        }

        for (var vertex : vertices) {
            for (var child : children) {
                graph.addEdge(vertex, child);
            }
        }

        assertEquals(20, graph.vertexSet().size());
        assertEquals(10, graph.roots().size());

    }

    @Test
    void sinks() {
        DummyGraph graph = new DummyGraph();
        DummyVertex[] vertices = new DummyVertex[10];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex(STR."Vertex \{i}");
            graph.addVertex(vertices[i]);
        }

        assertEquals(10, graph.vertexSet().size());
        assertEquals(10, graph.sinks().size());

        DummyVertex[] parents = new DummyVertex[10];

        for (int i = 0; i < parents.length; i++) {
            parents[i] = new DummyVertex(STR."Parent \{i}");
            graph.addVertex(parents[i]);
        }

        for (var vertex : vertices) {
            for (var parent : parents) {
                graph.addEdge(parent, vertex);
            }
        }

        assertEquals(20, graph.vertexSet().size());
        assertEquals(10, graph.sinks().size());
    }

    @Test
    void edgeOf() {
        DummyGraph graph = new DummyGraph();
        DummyVertex vertex = new DummyVertex();
        DummyVertex child = new DummyVertex();

        assertFalse(graph.edgeOf(vertex, child).isPresent());

        graph.addVertex(vertex);
        graph.addVertex(child);

        assertFalse(graph.edgeOf(vertex, child).isPresent());

        graph.addEdge(vertex, child);

        assertTrue(graph.edgeOf(vertex, child).isPresent());
    }

    @Test
    void egressEdgesOf() {
        DummyGraph graph = new DummyGraph();
        DummyVertex vertex = new DummyVertex("Vertex");
        DummyVertex[] children = new DummyVertex[10];
        DummyVertex[] parents = new DummyVertex[10];

        for (int i = 0; i < children.length; i++) {
            children[i] = new DummyVertex(STR."Child \{i}");
            graph.addVertex(children[i]);
        }

        assertThrows(IllegalArgumentException.class, () -> graph.egressEdgesOf(vertex));

        graph.addVertex(vertex);
        assertEquals(0, graph.egressEdgesOf(vertex).size());

        for (var child : children) {
            graph.addVertex(child);
            assertEquals(0, graph.egressEdgesOf(vertex).size());
        }

        var s = 0;
        for (var child : children) {
            graph.addEdge(vertex, child);
            assertEquals(++s, graph.egressEdgesOf(vertex).size());
        }

        for (int i = 0; i < parents.length; i++) {
            parents[i] = new DummyVertex(STR."Parent \{i}");
            graph.addVertex(parents[i]);
            graph.addEdge(parents[i], vertex);
            assertEquals(s, graph.egressEdgesOf(vertex).size());
        }
    }

    @Test
    void ingressEdgesOf() {
        DummyGraph graph = new DummyGraph();
        DummyVertex vertex = new DummyVertex("Vertex");
        DummyVertex[] children = new DummyVertex[10];
        DummyVertex[] parents = new DummyVertex[10];

        for (int i = 0; i < children.length; i++) {
            children[i] = new DummyVertex(STR."Child \{i}");
            graph.addVertex(children[i]);
        }

        assertThrows(IllegalArgumentException.class, () -> graph.ingressEdgesOf(vertex));

        graph.addVertex(vertex);
        assertEquals(0, graph.ingressEdgesOf(vertex).size());

        for (var child : children) {
            graph.addVertex(child);
            assertEquals(0, graph.ingressEdgesOf(vertex).size());
        }

        var s = 0;
        for (var child : children) {
            graph.addEdge(child, vertex);
            assertEquals(++s, graph.ingressEdgesOf(vertex).size());
        }

        for (int i = 0; i < parents.length; i++) {
            parents[i] = new DummyVertex(STR."Parent \{i}");
            graph.addVertex(parents[i]);
            graph.addEdge(vertex, parents[i]);
            assertEquals(s, graph.ingressEdgesOf(vertex).size());
        }
    }

    @Test
    void size() {
        DummyGraph graph = new DummyGraph();
        DummyVertex[] vertices = new DummyVertex[10];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex(STR."Vertex \{i}");
            graph.addVertex(vertices[i]);
            assertEquals(i + 1, graph.size());
        }
    }

    @Test
    void getParents() {
        DummyGraph graph = new DummyGraph();
        DummyVertex vertex = new DummyVertex("Vertex");
        DummyVertex[] children = new DummyVertex[10];
        DummyVertex[] parents = new DummyVertex[10];

        for (int i = 0; i < children.length; i++) {
            children[i] = new DummyVertex(STR."Child \{i}");
            graph.addVertex(children[i]);
        }

        assertEquals(0, graph.getParents(vertex).size());

        graph.addVertex(vertex);
        assertEquals(0, graph.getParents(vertex).size());

        for (var child : children) {
            graph.addVertex(child);
            assertEquals(0, graph.getParents(vertex).size());
        }

        for (var child : children) {
            graph.addEdge(vertex, child);
            assertEquals(1, graph.getParents(child).size());
        }

        for (int i = 0; i < parents.length; i++) {
            parents[i] = new DummyVertex(STR."Parent \{i}");
            graph.addVertex(parents[i]);
            graph.addEdge(parents[i], vertex);
            assertEquals(i + 1, graph.getParents(vertex).size());
        }
    }

    @Test
    void getChildren() {
        DummyGraph graph = new DummyGraph();
        DummyVertex vertex = new DummyVertex("Vertex");
        DummyVertex[] children = new DummyVertex[10];
        DummyVertex[] parents = new DummyVertex[10];

        for (int i = 0; i < children.length; i++) {
            children[i] = new DummyVertex(STR."Child \{i}");
            graph.addVertex(children[i]);
        }

        assertThrows(IllegalArgumentException.class, () -> graph.getChildren(vertex));

        graph.addVertex(vertex);
        assertEquals(0, graph.getChildren(vertex).size());

        for (var child : children) {
            graph.addVertex(child);
            assertEquals(0, graph.getChildren(vertex).size());
        }


        for (int i = 0; i < children.length; i++) {
            graph.addEdge(vertex, children[i]);
            assertEquals(i + 1, graph.getChildren(vertex).size());
        }

        assertTrue(graph.getChildren(vertex).containsAll(Set.of(children)));

        for (int i = 0; i < parents.length; i++) {
            parents[i] = new DummyVertex(STR."Parent \{i}");
            graph.addVertex(parents[i]);
            graph.addEdge(parents[i], vertex);
            assertEquals(children.length, graph.getChildren(vertex).size());
        }
    }

    @Test
    void sourcesOf() {
        DummyGraph graph = new DummyGraph();
        DummyVertex[] vertices = new DummyVertex[10];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex(STR."Vertex \{i}");
            graph.addVertex(vertices[i]);
        }

        assertEquals(10, graph.vertexSet().size());
        assertEquals(10, graph.roots().size());

        for (var vertex : vertices)
            assertEquals(1, graph.sourcesOf(vertex).size());

        DummyVertex[] children = new DummyVertex[10];

        for (int i = 0; i < children.length; i++) {
            children[i] = new DummyVertex(STR."Child \{i}");
            graph.addVertex(children[i]);
        }

        for (var vertex : vertices) {
            for (var child : children) {
                graph.addEdge(vertex, child);
            }
        }

        assertEquals(20, graph.vertexSet().size());
        assertEquals(10, graph.sourcesOf(children[0]).size());
    }

    @Test
    void sinksOf() {
        DummyGraph graph = new DummyGraph();
        DummyVertex[] vertices = new DummyVertex[10];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex(STR."Vertex \{i}");
            graph.addVertex(vertices[i]);
        }

        assertEquals(10, graph.vertexSet().size());
        assertEquals(10, graph.sinks().size());

        for (var vertex : vertices)
            assertEquals(1, graph.sinksOf(vertex).size());

        DummyVertex[] parents = new DummyVertex[10];

        for (int i = 0; i < parents.length; i++) {
            parents[i] = new DummyVertex(STR."Parent \{i}");
            graph.addVertex(parents[i]);
        }

        for (var vertex : vertices) {
            for (var parent : parents) {
                graph.addEdge(parent, vertex);
            }
        }

        assertEquals(20, graph.vertexSet().size());
        assertEquals(10, graph.sinksOf(parents[0]).size());
    }

    @Test
    void shortestPathBetween() {
        DummyGraph graph = new DummyGraph();
        DummyVertex[] vertices = new DummyVertex[10];

        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new DummyVertex(STR."Vertex \{i}");
            graph.addVertex(vertices[i]);
        }

        assertEquals(10, graph.vertexSet().size());
        assertEquals(10, graph.roots().size());

        DummyVertex[] children = new DummyVertex[10];

        for (int i = 0; i < children.length; i++) {
            children[i] = new DummyVertex(STR."Child \{i}");
            graph.addVertex(children[i]);
        }

        for (var vertex : vertices) {
            for (var child : children) {
                graph.addEdge(vertex, child);
            }
        }

        assertEquals(20, graph.vertexSet().size());
        assertEquals(10, graph.roots().size());

        for (var vertex : vertices) {
            for (var child : children) {
                var path = graph.shortestPathBetween(vertex, child);
                assertNotNull(path);
                assertEquals(1, path.size());
            }
        }
    }

    @Test
    void shortestVertexPathBetween() {
        DummyGraph graph = new DummyGraph();
        List<DummyVertex> path = List.of(
                new DummyVertex("A"),
                new DummyVertex("B"),
                new DummyVertex("C"),
                new DummyVertex("D"),
                new DummyVertex("E")
        );

        for (var vertex : path) {
            graph.addVertex(vertex);
        }

        for (int i = 0; i < path.size() - 1; i++) {
            graph.addEdge(path.get(i), path.get(i + 1));
        }

    }
}