package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface ShouldDisconnectFailure extends OperationFailureResult permits ShouldDisconnectChildFailure, ShouldDisconnectParentFailure {
}
