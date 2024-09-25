package juanmanuel.tea.graph.operation_failures;

import juanmanuel.tea.graph.ApplicationGraph;
import juanmanuel.tea.graph.ApplicationVertex;

public interface FailureResults {
    /**
     * Failure type that indicates that the operation is not permitted by the vertex policy.
     *
     * @param message  A message explaining why the operation was rejected.
     * @param rejector The vertex that has the policy that rejected the operation.
     */
    record RejectedByVertexPolicy(String message, ApplicationVertex<?> rejector) implements
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldDisconnectChildFailure,
            ApplicationVertex.ShouldDisconnectParentFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ShouldRemoveChildFailure,
            ApplicationVertex.ShouldRemoveParentFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure,
            ApplicationGraph.ShouldAddVertexFailure,
            ApplicationGraph.ShouldRemoveVertexFailure,
            ApplicationGraph.ShouldAddEdgeFailure,
            ApplicationGraph.ShouldRemoveEdgeFailure,
            ApplicationGraph.VertexAdditionFailure,
            ApplicationGraph.VertexRemovalFailure,
            ApplicationGraph.EdgeAdditionFailure,
            ApplicationGraph.EdgeRemovalFailure,
            ApplicationVertex.ChildDisconnectionFailure,
            ApplicationVertex.ParentDisconnectionFailure,
            ApplicationVertex.ChildRemovalFailure,
            ApplicationVertex.ParentRemovalFailure,
            ApplicationVertex.RemoveFailure
    {}

    /**
     * Failure type that indicates that the operation is not allowed by the vertex validation.
     *
     * @param message  A message explaining why the validation failed.
     * @param rejector The vertex that has the validation that rejected the operation.
     */
    record RejectedByVertexValidation(String message, ApplicationVertex<?> rejector) implements
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldDisconnectChildFailure,
            ApplicationVertex.ShouldDisconnectParentFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ShouldRemoveChildFailure,
            ApplicationVertex.ShouldRemoveParentFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure,
            ApplicationGraph.ShouldAddVertexFailure,
            ApplicationGraph.ShouldRemoveVertexFailure,
            ApplicationGraph.ShouldAddEdgeFailure,
            ApplicationGraph.ShouldRemoveEdgeFailure,
            ApplicationGraph.VertexAdditionFailure,
            ApplicationGraph.VertexRemovalFailure,
            ApplicationGraph.EdgeAdditionFailure,
            ApplicationGraph.EdgeRemovalFailure,
            ApplicationVertex.ChildDisconnectionFailure,
            ApplicationVertex.ParentDisconnectionFailure,
            ApplicationVertex.ChildRemovalFailure,
            ApplicationVertex.ParentRemovalFailure,
            ApplicationVertex.RemoveFailure
    {}

    /**
     * Failure type that indicates that the operation is not permitted by the graph policy.
     *
     * @param message  A message explaining why the operation was rejected.
     * @param rejector The graph that has the policy that rejected the operation.
     */
    record RejectedByGraphPolicy(String message, ApplicationGraph<?> rejector) implements
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldDisconnectChildFailure,
            ApplicationVertex.ShouldDisconnectParentFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ShouldRemoveChildFailure,
            ApplicationVertex.ShouldRemoveParentFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure,
            ApplicationGraph.ShouldAddVertexFailure,
            ApplicationGraph.ShouldRemoveVertexFailure,
            ApplicationGraph.ShouldAddEdgeFailure,
            ApplicationGraph.ShouldRemoveEdgeFailure,
            ApplicationGraph.VertexAdditionFailure,
            ApplicationGraph.VertexRemovalFailure,
            ApplicationGraph.EdgeAdditionFailure,
            ApplicationGraph.EdgeRemovalFailure,
            ApplicationVertex.ChildDisconnectionFailure,
            ApplicationVertex.ParentDisconnectionFailure,
            ApplicationVertex.ChildRemovalFailure,
            ApplicationVertex.ParentRemovalFailure,
            ApplicationVertex.RemoveFailure
    {}

    /**
     * Failure type that indicates that the operation is not allowed by the graph validation.
     *
     * @param message  A message explaining why the validation failed.
     * @param rejector The graph that has the validation that rejected the operation.
     */
    record RejectedByGraphValidation(String message, ApplicationGraph<?> rejector) implements
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldDisconnectChildFailure,
            ApplicationVertex.ShouldDisconnectParentFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ShouldRemoveChildFailure,
            ApplicationVertex.ShouldRemoveParentFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure,
            ApplicationGraph.ShouldAddVertexFailure,
            ApplicationGraph.ShouldRemoveVertexFailure,
            ApplicationGraph.ShouldAddEdgeFailure,
            ApplicationGraph.ShouldRemoveEdgeFailure,
            ApplicationGraph.VertexAdditionFailure,
            ApplicationGraph.VertexRemovalFailure,
            ApplicationGraph.EdgeAdditionFailure,
            ApplicationGraph.EdgeRemovalFailure,
            ApplicationVertex.ChildDisconnectionFailure,
            ApplicationVertex.ParentDisconnectionFailure,
            ApplicationVertex.ChildRemovalFailure,
            ApplicationVertex.ParentRemovalFailure,
            ApplicationVertex.RemoveFailure
    {}

    /**
     * Failure type that indicates that a vertex is already present in a graph.
     *
     * @param message A message explaining the failure.
     */
    record VertexAlreadyPresent(String message) implements
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure,
            ApplicationGraph.ShouldAddVertexFailure,
            ApplicationGraph.VertexAdditionFailure
//            ApplicationVertex.ShouldAddToGraphFailure,
//            ApplicationVertex.AdditionToGraphFailure
    {}

    record SelfReference(String message) implements
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldDisconnectChildFailure,
            ApplicationVertex.ShouldDisconnectParentFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildDisconnectionFailure,
            ApplicationVertex.ParentDisconnectionFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ShouldRemoveChildFailure,
            ApplicationVertex.ShouldRemoveParentFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure,
            ApplicationVertex.ChildRemovalFailure,
            ApplicationVertex.ParentRemovalFailure {}

    record EdgeAlreadyExists(String message) implements
            ApplicationVertex.ParentAdditionFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationGraph.ShouldAddEdgeFailure,
            ApplicationGraph.EdgeAdditionFailure
    {}

    record EdgeNotPresent(String message) implements
            ApplicationVertex.ShouldDisconnectChildFailure,
            ApplicationVertex.ShouldDisconnectParentFailure,
            ApplicationVertex.ShouldRemoveChildFailure,
            ApplicationVertex.ShouldRemoveParentFailure,
            ApplicationVertex.ParentDisconnectionFailure,
            ApplicationVertex.ChildDisconnectionFailure,
            ApplicationVertex.ChildRemovalFailure,
            ApplicationVertex.ParentRemovalFailure,
            ApplicationGraph.ShouldRemoveEdgeFailure,
            ApplicationGraph.EdgeRemovalFailure
    {}




    record VertexNotPresent(String message, ApplicationVertex<?> vertex) implements
            ApplicationGraph.ShouldAddEdgeFailure,
            ApplicationGraph.ShouldRemoveEdgeFailure,
            ApplicationGraph.ShouldRemoveVertexFailure,
            ApplicationGraph.VertexRemovalFailure,
            ApplicationGraph.EdgeAdditionFailure,
            ApplicationGraph.EdgeRemovalFailure,
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldDisconnectChildFailure,
            ApplicationVertex.ShouldDisconnectParentFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ShouldRemoveChildFailure,
            ApplicationVertex.ShouldRemoveParentFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildDisconnectionFailure,
            ApplicationVertex.ParentDisconnectionFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure,
            ApplicationVertex.ChildRemovalFailure,
            ApplicationVertex.ParentRemovalFailure,
            ApplicationVertex.RemoveFailure
    {}

    record GraphCycleDetected(String message) implements
            ApplicationGraph.ShouldAddEdgeFailure,
            ApplicationGraph.EdgeAdditionFailure,
            ApplicationVertex.ShouldConnectChildFailure,
            ApplicationVertex.ShouldConnectParentFailure,
            ApplicationVertex.ShouldAddChildFailure,
            ApplicationVertex.ShouldAddParentFailure,
            ApplicationVertex.ChildConnectionFailure,
            ApplicationVertex.ParentConnectionFailure,
            ApplicationVertex.ChildAdditionFailure,
            ApplicationVertex.ParentAdditionFailure
    {}

}
