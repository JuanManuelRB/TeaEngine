package juanmanuel.tea.graph.operation_failures.vertex;

import juanmanuel.tea.graph.operation_failures.FailureResults;

public sealed interface ShouldAddParentFailure extends ShouldAddFailure permits
        FailureResults.EdgeAlreadyExists,
        FailureResults.GraphCycleDetected,
        FailureResults.RejectedByGraphPolicy,
        FailureResults.RejectedByGraphValidation,
        FailureResults.RejectedByVertexPolicy,
        FailureResults.RejectedByVertexValidation,
        FailureResults.SelfReference,
        FailureResults.VertexAlreadyPresent,
        FailureResults.VertexNotPresent {
}
