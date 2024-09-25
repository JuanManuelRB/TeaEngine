package juanmanuel.tea.graph.validation;

import juanmanuel.tea.graph.ApplicationGraph;
import juanmanuel.tea.graph.ApplicationVertex;
import juanmanuel.tea.utils.Result;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Manages a vertex operations validations.
 * Given a vertex operation, it can store and validate a set of predicates that must be satisfied for the operation to be allowed.
 * The operations that can be validated are:
 * - Connect child
 * - Disconnect child
 * - Connect parent
 * - Disconnect parent
 * The validations are stored in a weak set to allow the predicates to be garbage collected if they are not referenced elsewhere.
 * If the user wants a validation to be permanently stored, they must keep a reference to the predicate elsewhere.
 * @param <V> The type of the vertex over which the validations are performed.
 */
public final class VertexOperationValidator<V extends ApplicationVertex<V>> implements OperationValidator {
    private final EnumMap<VerticesOperationValidation, Set<ResultFunction<V>>> vertexValidations
            = new EnumMap<>(VerticesOperationValidation.class);
    private final EnumMap<GraphsOperationValidation, Set<ResultFunction<ApplicationGraph<? super V>>>> graphValidations
            = new EnumMap<>(GraphsOperationValidation.class);

    @FunctionalInterface
    public interface ResultFunction<T> extends Function<T, Result<Void, String>> {}

    /**
     * Possible operations to validate over a vertex.
     */
    public enum VerticesOperationValidation {
        /**
         * Validation that represents whether a child should be connected to a vertex.
         */
        CONNECT_CHILD_VALIDATION,

        /**
         * Validation that represents whether a child should be disconnected from a vertex.
         */
        DISCONNECT_CHILD_VALIDATION,

        /**
         * Validation that represents whether a parent should be connected to a vertex.
         */
        CONNECT_PARENT_VALIDATION,

        /**
         * Validation that represents whether a parent should be disconnected from a vertex.
         */
        DISCONNECT_PARENT_VALIDATION
    }

    /**
     * Possible operations to validate over a graph.
     */
    public enum GraphsOperationValidation {

        /**
         * Validation that represents whether a graph should be set on a vertex.
         */
        ADD_TO_GRAPH_VALIDATION,

        /**
         * Validation that represents whether a graph should be removed from a vertex.
         */
        REMOVE_FROM_GRAPH_VALIDATION
    }

    /**
     * Adds a validation for a vertex operation.
     * @param validation The operation to validate.
     * @param predicate The predicate that must be satisfied for the validation to pass.
     */
    public void addOperationValidation(VerticesOperationValidation validation, ResultFunction<V> predicate) {
        Objects.requireNonNull(validation, "validation cannot be null");
        Objects.requireNonNull(predicate, "predicate cannot be null");

        var set = vertexValidations.computeIfAbsent(validation, _ -> Collections.newSetFromMap(new WeakHashMap<>()));
        set.add(predicate);
    }

    /**
     * Adds a validation for a graph operation.
     * @param validation The operation to validate.
     * @param predicate The predicate that must be satisfied for the validation to pass.
     */
    public void addOperationValidation(GraphsOperationValidation validation, ResultFunction<ApplicationGraph<? super V>> predicate) {
        Objects.requireNonNull(validation, "validation cannot be null");
        Objects.requireNonNull(predicate, "predicate cannot be null");

        var set = graphValidations.computeIfAbsent(validation, _ -> Collections.newSetFromMap(new WeakHashMap<>()));
        set.add(predicate);
    }

    /**
     * Removes a validation for an operation.
     * @param validation The operation to remove the validation from.
     * @param predicate The predicate to remove.
     * @return true if the validation was removed, false otherwise.
     */
    public boolean removeOperationValidation(VerticesOperationValidation validation, Predicate<V> predicate) {
        Objects.requireNonNull(validation, "validation cannot be null");

        var set = vertexValidations.get(validation);
        if (set == null) return false;
        return set.remove(predicate);
    }

    /**
     * Removes a validation for an operation.
     * @param validation The operation to remove the validation from.
     * @param predicate The predicate to remove.
     * @return true if the validation was removed, false otherwise.
     */
    public boolean removeOperationValidation(GraphsOperationValidation validation, Predicate<ApplicationGraph<V>> predicate) {
        Objects.requireNonNull(validation, "validation cannot be null");

        var set = graphValidations.get(validation);
        if (set == null) return false;
        return set.remove(predicate);
    }

    /**
     * Validates an operation over a vertex.
     * @param validation The operation to validate.
     * @param vertex The vertex to validate the operation on.
     * @return a result that contains a set of the validations failures messages if the operation is invalid, or null if the
     * operation is valid.
     */
    public Result<Void, Set<String>> validateOperation(VerticesOperationValidation validation, V vertex) {
        var set = vertexValidations.get(validation);
        if (set == null)
            return Result.success();

        var failures = new HashSet<String>();
        for (var predicate : set) {
            var result = predicate.apply(vertex);
            switch (result) {
                case Result.Success<Void, ?> _ -> {
                }
                case Result.Failure<?, String>(var f) -> failures.add(f);
            }
        }

        if (failures.isEmpty())
            return Result.success();
        return Result.fail(failures);
    }

    /**
     * Validates an operation over a graph.
     * @param validation The operation to validate.
     * @param graph The graph to validate the operation on.
     * @return a result that contains a set of the validations failures messages if the operation is invalid, or null if the
     * operation is valid.
     */
    public Result<Void, Set<String>> validateOperation(GraphsOperationValidation validation, ApplicationGraph<? super V> graph) {
        var set = graphValidations.get(validation);
        if (set == null)
            return Result.success();

        var failures = new HashSet<String>();
        for (var predicate : set) {
            var result = predicate.apply(graph);
            switch (result) {
                case Result.Success<Void, ?> _ -> {
                }
                case Result.Failure<?, String>(var f) -> failures.add(f);
            }
        }

        if (failures.isEmpty())
            return Result.success();
        return Result.fail(failures);
    }

    /**
     * Gets the validations for an operation.
     * @param validation The operation to get the validations for.
     * @return a set of predicates that must be satisfied for the operation to be valid.
     */
    public Set<ResultFunction<V>> getValidationsForOperation(VerticesOperationValidation validation) {
        var set = vertexValidations.get(validation);
        if (set == null)
            return new HashSet<>();
        return Set.copyOf(set);
    }

    /**
     * Gets the validations for an operation.
     * @param validation The operation to get the validations for.
     * @return a set of predicates that must be satisfied for the operation to be valid.
     */
    public Set<ResultFunction<ApplicationGraph<? super V>>> getValidationsForOperation(GraphsOperationValidation validation) {
        var set = graphValidations.get(validation);
        if (set == null)
            return new HashSet<>();
        return Set.copyOf(set);
    }
}
