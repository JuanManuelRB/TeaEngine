package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface ShouldConnectFailure extends OperationFailureResult permits ShouldConnectChildFailure, ShouldConnectParentFailure {
}
