package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface ShouldAddFailure extends OperationFailureResult permits ShouldAddChildFailure, ShouldAddParentFailure {
}
