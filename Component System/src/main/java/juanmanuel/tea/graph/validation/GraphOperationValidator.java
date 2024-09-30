package juanmanuel.tea.graph.validation;

import juanmanuel.tea.graph.Vertex;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class GraphOperationValidator<V extends Vertex<V>> implements OperationValidator {
    private final EnumMap<VertexValidation, Set<Predicate<V>>> vertexValidations
            = new EnumMap<>(VertexValidation.class);
    private final EnumMap<EdgeValidation, Set<BiPredicate<V, V>>> graphValidations
            = new EnumMap<>(EdgeValidation.class);

    public enum VertexValidation {
        ADD_VERTEX_VALIDATION,
        REMOVE_VERTEX_VALIDATION,
    }

    public enum EdgeValidation {
        CREATE_EDGE_VALIDATION,
        REMOVE_EDGE_VALIDATION
    }

    /**
     * Adds a validation for a vertex operation.
     * @param validation The operation to validate.
     * @param predicate The predicate that must be satisfied for the validation to pass.
     */
    public void addValidationForOperation(VertexValidation validation, Predicate<V> predicate) {
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
    public void addValidationForOperation(EdgeValidation validation, BiPredicate<V, V> predicate) {
        Objects.requireNonNull(validation, "validation cannot be null");
        Objects.requireNonNull(predicate, "predicate cannot be null");

        var set = graphValidations.computeIfAbsent(validation, _ -> Collections.newSetFromMap(new WeakHashMap<>()));
        set.add(predicate);
    }

    /**
     * Removes a validation for an operation.
     * @param validation The operation to remove the validation from.
     * @param key The key associated with the validation.
     * @return true if the validation was removed, false otherwise.
     */
    public boolean removeValidationForOperation(VertexValidation validation, Predicate<V> key) {
        Objects.requireNonNull(validation, "validation cannot be null");

        var set = vertexValidations.get(validation);
        if (set == null) return false;
        return set.remove(key);
    }

    /**
     * Removes a validation for an operation.
     * @param validation The operation to remove the validation from.
     * @param key The key associated with the validation.
     * @return true if the validation was removed, false otherwise.
     */
    public boolean removeValidationForOperation(EdgeValidation validation, BiPredicate<V, V> key) {
        Objects.requireNonNull(validation, "validation cannot be null");

        var map = graphValidations.get(validation);
        if (map == null) return false;
        return map.remove(key);
    }

    /**
     * Validates an operation.
     * @param validation The operation to validate.
     * @param vertex The vertex to validate the operation on.
     * @return true if the operation is valid, false otherwise.
     */
    public boolean validateOperation(VertexValidation validation, V vertex) {
        return getValidationsForOperation(validation).stream().allMatch(p -> p.test(vertex));
    }

    /**
     * Validates an operation.
     * @param validation The operation to validate.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return true if the operation is valid, false otherwise.
     */
    public boolean validateOperation(EdgeValidation validation, V source, V target) {
        return getValidationsForOperation(validation).parallelStream().allMatch(p -> p.test(source, target));
    }

    /**
     * Gets the validations for an operation.
     * @param validation The operation to get the validations for.
     * @return a set of predicates that must be satisfied for the operation to be valid.
     */
    public Set<Predicate<V>> getValidationsForOperation(VertexValidation validation) {
        var set = vertexValidations.get(validation);
        if (set == null)
            return Set.of();
        return Set.copyOf(set);
    }

    /**
     * Gets the validations for an operation.
     * @param validation The operation to get the validations for.
     * @return a set of predicates that must be satisfied for the operation to be valid.
     */
    public Set<BiPredicate<V, V>> getValidationsForOperation(EdgeValidation validation) {
        var set = graphValidations.get(validation);
        if (set == null)
            return Set.of();
        return Set.copyOf(set);
    }
}
