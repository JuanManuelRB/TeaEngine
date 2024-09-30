package juanmanuel.tea.graph.operation_failures.vertex;

public sealed interface AddFailureResult extends OperationFailureResult permits ChildAdditionFailure, ParentAdditionFailure {
}
