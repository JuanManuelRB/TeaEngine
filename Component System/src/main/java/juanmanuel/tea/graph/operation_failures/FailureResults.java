package juanmanuel.tea.graph.operation_failures;

import juanmanuel.tea.components.StructuredComputation;
import juanmanuel.tea.graph.Graph;
import juanmanuel.tea.graph.Vertex;


public interface FailureResults {

    String message();

    /**
     * Failure type that indicates that the operation is not permitted by the vertex policy.
     *
     * @param message  A message explaining why the operation was rejected.
     * @param rejector The vertex that has the policy that rejected the operation.
     */
    record RejectedByVertexPolicy(String message, Vertex<?> rejector) implements
            Vertex.ShouldConnectChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldDisconnectChildFailure,
            Vertex.ShouldDisconnectParentFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ShouldRemoveChildFailure,
            Vertex.ShouldRemoveParentFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            Vertex.ChildDisconnectionFailure,
            Vertex.ParentDisconnectionFailure,
            Vertex.ChildRemovalFailure,
            Vertex.ParentRemovalFailure,
            Vertex.RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that the operation is not allowed by the vertex validation.
     *
     * @param message  A message explaining why the validation failed.
     * @param rejector The vertex that has the validation that rejected the operation.
     */
    record RejectedByVertexValidation(String message, Vertex<?> rejector) implements
            Vertex.ShouldConnectChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldDisconnectChildFailure,
            Vertex.ShouldDisconnectParentFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ShouldRemoveChildFailure,
            Vertex.ShouldRemoveParentFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            Vertex.ChildDisconnectionFailure,
            Vertex.ParentDisconnectionFailure,
            Vertex.ChildRemovalFailure,
            Vertex.ParentRemovalFailure,
            Vertex.RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that the operation is not permitted by the graph policy.
     *
     * @param message  A message explaining why the operation was rejected.
     * @param rejector The graph that has the policy that rejected the operation.
     */
    record RejectedByGraphPolicy(String message, Graph<?, ?> rejector) implements
            Vertex.ShouldConnectChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldDisconnectChildFailure,
            Vertex.ShouldDisconnectParentFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ShouldRemoveChildFailure,
            Vertex.ShouldRemoveParentFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            Vertex.ChildDisconnectionFailure,
            Vertex.ParentDisconnectionFailure,
            Vertex.ChildRemovalFailure,
            Vertex.ParentRemovalFailure,
            Vertex.RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that the operation is not allowed by the graph validation.
     *
     * @param message  A message explaining why the validation failed.
     * @param rejector The graph that has the validation that rejected the operation.
     */
    record RejectedByGraphValidation(String message, Graph<?, ?> rejector) implements
            Vertex.ShouldConnectChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldDisconnectChildFailure,
            Vertex.ShouldDisconnectParentFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ShouldRemoveChildFailure,
            Vertex.ShouldRemoveParentFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.ShouldRemoveVertexFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.ShouldRemoveEdgeFailure,
            Graph.VertexAdditionFailure,
            Graph.VertexRemovalFailure,
            Graph.EdgeAdditionFailure,
            Graph.EdgeRemovalFailure,
            Vertex.ChildDisconnectionFailure,
            Vertex.ParentDisconnectionFailure,
            Vertex.ChildRemovalFailure,
            Vertex.ParentRemovalFailure,
            Vertex.RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    /**
     * Failure type that indicates that a vertex is already present in a graph.
     *
     * @param message A message explaining the failure.
     */
    record VertexAlreadyPresent(String message) implements
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            Graph.ShouldAddVertexFailure,
            Graph.VertexAdditionFailure,
            StructuredComputation.SetAfterFailure
//            Vertex.ShouldAddToGraphFailure,
//            Vertex.AdditionToGraphFailure
    {}

    record SelfReference(String message) implements
            Vertex.ShouldConnectChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldDisconnectChildFailure,
            Vertex.ShouldDisconnectParentFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildDisconnectionFailure,
            Vertex.ParentDisconnectionFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ShouldRemoveChildFailure,
            Vertex.ShouldRemoveParentFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            Vertex.ChildRemovalFailure,
            Vertex.ParentRemovalFailure,
            StructuredComputation.SetAfterFailure {}

    record EdgeAlreadyExists(String message) implements
            Vertex.ParentAdditionFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldConnectChildFailure,
            Graph.ShouldAddEdgeFailure,
            Graph.EdgeAdditionFailure,
            StructuredComputation.SetAfterFailure
    {}

    record EdgeNotPresent(String message) implements
            Vertex.ShouldDisconnectChildFailure,
            Vertex.ShouldDisconnectParentFailure,
            Vertex.ShouldRemoveChildFailure,
            Vertex.ShouldRemoveParentFailure,
            Vertex.ParentDisconnectionFailure,
            Vertex.ChildDisconnectionFailure,
            Vertex.ChildRemovalFailure,
            Vertex.ParentRemovalFailure,
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
            Vertex.ShouldConnectChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldDisconnectChildFailure,
            Vertex.ShouldDisconnectParentFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ShouldRemoveChildFailure,
            Vertex.ShouldRemoveParentFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildDisconnectionFailure,
            Vertex.ParentDisconnectionFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            Vertex.ChildRemovalFailure,
            Vertex.ParentRemovalFailure,
            Vertex.RemoveFailure,
            StructuredComputation.SetAfterFailure
    {}

    record GraphCycleDetected(String message) implements
            Graph.ShouldAddEdgeFailure,
            Graph.EdgeAdditionFailure,
            Vertex.ShouldConnectChildFailure,
            Vertex.ShouldConnectParentFailure,
            Vertex.ShouldAddChildFailure,
            Vertex.ShouldAddParentFailure,
            Vertex.ChildConnectionFailure,
            Vertex.ParentConnectionFailure,
            Vertex.ChildAdditionFailure,
            Vertex.ParentAdditionFailure,
            StructuredComputation.SetAfterFailure
    {}

}
