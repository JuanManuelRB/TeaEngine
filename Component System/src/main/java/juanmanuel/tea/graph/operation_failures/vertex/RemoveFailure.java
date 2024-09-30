package juanmanuel.tea.graph.operation_failures.vertex;

import juanmanuel.tea.graph.operation_failures.FailureResults;

public sealed interface RemoveFailure permits FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.VertexNotPresent {
}
