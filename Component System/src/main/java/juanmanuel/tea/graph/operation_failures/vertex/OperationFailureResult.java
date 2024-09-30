package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface OperationFailureResult permits AddFailureResult, AttachFailureResult, ChildRemovalFailure, ParentRemovalFailure, ShouldAddFailure, ShouldConnectFailure, ShouldDisconnectFailure, ShouldRemoveFailure, DisconnectFailureResult {

    String message();
}
