package juanmanuel.tea.graph.operation_failures;

import juanmanuel.tea.components.StructuredComputation;
import juanmanuel.tea.graph.Graph;
import juanmanuel.tea.graph.Vertex;
import juanmanuel.tea.graph.operation_failures.vertex.*;


public interface FailureResults {

    String message();

    /**
     * Failure type that indicates that the operation is not permitted by the vertex policy.
     *
     * @param message  A message explaining why the operation was rejected.
     * @param rejector The vertex that has the policy that rejected the operation.
     */
    record RejectedByVertexPolicy(String message, Vertex<?> rejector) implements
            ShouldConnectChildFailure,
            ShouldConnectParentFailure,
            ShouldDisconnectChildFailure,
            ShouldDisconnectParentFailure,
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ShouldRemoveChildFailure,
            ShouldRemoveParentFailure,
            ChildConnectionFailure,
            ParentConnectionFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            ChildDisconnectionFailure,
            ParentDisconnectionFailure,
            ChildRemovalFailure,
            ParentRemovalFailure,
            RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that the operation is not allowed by the vertex validation.
     *
     * @param message  A message explaining why the validation failed.
     * @param rejector The vertex that has the validation that rejected the operation.
     */
    record RejectedByVertexValidation(String message, Vertex<?> rejector) implements
            ShouldConnectChildFailure,
            ShouldConnectParentFailure,
            ShouldDisconnectChildFailure,
            ShouldDisconnectParentFailure,
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ShouldRemoveChildFailure,
            ShouldRemoveParentFailure,
            ChildConnectionFailure,
            ParentConnectionFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            ChildDisconnectionFailure,
            ParentDisconnectionFailure,
            ChildRemovalFailure,
            ParentRemovalFailure,
            RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that the operation is not permitted by the graph policy.
     *
     * @param message  A message explaining why the operation was rejected.
     * @param rejector The graph that has the policy that rejected the operation.
     */
    record RejectedByGraphPolicy(String message, Graph<?, ?> rejector) implements
            ShouldConnectChildFailure,
            ShouldConnectParentFailure,
            ShouldDisconnectChildFailure,
            ShouldDisconnectParentFailure,
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ShouldRemoveChildFailure,
            ShouldRemoveParentFailure,
            ChildConnectionFailure,
            ParentConnectionFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            ChildDisconnectionFailure,
            ParentDisconnectionFailure,
            ChildRemovalFailure,
            ParentRemovalFailure,
            RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that the operation is not allowed by the graph validation.
     *
     * @param message  A message explaining why the validation failed.
     * @param rejector The graph that has the validation that rejected the operation.
     */
    record RejectedByGraphValidation(String message, Graph<?, ?> rejector) implements
            ShouldConnectChildFailure,
            ShouldConnectParentFailure,
            ShouldDisconnectChildFailure,
            ShouldDisconnectParentFailure,
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ShouldRemoveChildFailure,
            ShouldRemoveParentFailure,
            ChildConnectionFailure,
            ParentConnectionFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            ChildDisconnectionFailure,
            ParentDisconnectionFailure,
            ChildRemovalFailure,
            ParentRemovalFailure,
            RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that a vertex is already present in a graph.
     *
     * @param message A message explaining the failure.
     */
    record VertexAlreadyPresent(String message) implements
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.VertexAdditionFailure,
            StructuredComputation.SetAfterFailure
//            Vertex.ShouldAddToGraphFailure,
//            Vertex.AdditionToGraphFailure
    {}

    record SelfReference(String message) implements
            ShouldConnectChildFailure,
            ShouldConnectParentFailure,
            ShouldDisconnectChildFailure,
            ShouldDisconnectParentFailure,
            ChildConnectionFailure,
            ParentConnectionFailure,
            ChildDisconnectionFailure,
            ParentDisconnectionFailure,
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ShouldRemoveChildFailure,
            ShouldRemoveParentFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            ChildRemovalFailure,
            ParentRemovalFailure,
            StructuredComputation.SetAfterFailure {}

    record EdgeAlreadyExists(String message) implements
            ParentAdditionFailure,
            ChildAdditionFailure,
            ParentConnectionFailure,
            ChildConnectionFailure,
            ShouldAddParentFailure,
            ShouldAddChildFailure,
            ShouldConnectParentFailure,
            ShouldConnectChildFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.EdgeAdditionFailure,
            StructuredComputation.SetAfterFailure
    {}

    record EdgeNotPresent(String message) implements
            ShouldDisconnectChildFailure,
            ShouldDisconnectParentFailure,
            ShouldRemoveChildFailure,
            ShouldRemoveParentFailure,
            ParentDisconnectionFailure,
            ChildDisconnectionFailure,
            ChildRemovalFailure,
            ParentRemovalFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.EdgeRemovalFailure,
            StructuredComputation.SetAfterFailure
    {}




    record VertexNotPresent(String message, Vertex<?> vertex) implements
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            ShouldConnectChildFailure,
            ShouldConnectParentFailure,
            ShouldDisconnectChildFailure,
            ShouldDisconnectParentFailure,
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ShouldRemoveChildFailure,
            ShouldRemoveParentFailure,
            ChildConnectionFailure,
            ParentConnectionFailure,
            ChildDisconnectionFailure,
            ParentDisconnectionFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            ChildRemovalFailure,
            ParentRemovalFailure,
            RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    record GraphCycleDetected(String message) implements
            Graph.ShouldAddEdgeFailure,
            Graph.EdgeAdditionFailure,
            ShouldConnectChildFailure,
            ShouldConnectParentFailure,
            ShouldAddChildFailure,
            ShouldAddParentFailure,
            ChildConnectionFailure,
            ParentConnectionFailure,
            ChildAdditionFailure,
            ParentAdditionFailure,
            StructuredComputation.SetAfterFailure
    {}

}
