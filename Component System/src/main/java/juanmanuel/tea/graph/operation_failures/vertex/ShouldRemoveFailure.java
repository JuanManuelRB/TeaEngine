package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface ShouldRemoveFailure extends OperationFailureResult permits ShouldRemoveChildFailure, ShouldRemoveParentFailure {
}
