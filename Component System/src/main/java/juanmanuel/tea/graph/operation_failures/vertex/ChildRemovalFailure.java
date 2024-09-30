package juanmanuel.tea.graph.operation_failures.vertex;

import juanmanuel.tea.graph.operation_failures.FailureResults;

public sealed interface ChildRemovalFailure extends OperationFailureResult permits FailureResults.VertexNotPresent, FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference {
}
