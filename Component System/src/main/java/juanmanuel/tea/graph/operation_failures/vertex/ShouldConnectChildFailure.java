package juanmanuel.tea.graph.operation_failures.vertex;

import juanmanuel.tea.graph.operation_failures.FailureResults;

/**
 * Failure result of shouldConnectChild.
 */
public sealed interface ShouldConnectChildFailure extends ShouldConnectFailure permits
        FailureResults.EdgeAlreadyExists,
        FailureResults.GraphCycleDetected,
        FailureResults.RejectedByGraphPolicy,
        FailureResults.RejectedByGraphValidation,
        FailureResults.RejectedByVertexPolicy,
        FailureResults.RejectedByVertexValidation,
        FailureResults.SelfReference,
        FailureResults.VertexNotPresent {
}
