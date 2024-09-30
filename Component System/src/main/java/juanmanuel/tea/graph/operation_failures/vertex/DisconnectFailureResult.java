package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface DisconnectFailureResult extends OperationFailureResult permits ChildDisconnectionFailure, ParentDisconnectionFailure {
}
