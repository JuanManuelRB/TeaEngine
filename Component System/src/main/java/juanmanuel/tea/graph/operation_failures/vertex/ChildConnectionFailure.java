package juanmanuel.tea.graph.operation_failures.vertex;

import juanmanuel.tea.graph.operation_failures.FailureResults;

/// Child connection failure result.
public sealed interface ChildConnectionFailure extends AttachFailureResult permits FailureResults.EdgeAlreadyExists, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexNotPresent {
}
