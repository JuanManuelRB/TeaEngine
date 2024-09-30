package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface AttachFailureResult extends OperationFailureResult permits ChildConnectionFailure, ParentConnectionFailure {
}
