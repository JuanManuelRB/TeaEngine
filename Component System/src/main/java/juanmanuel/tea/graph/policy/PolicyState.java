package juanmanuel.tea.graph.policy;

/**
 * Represents the state of a policy.
 * Since the state of a policy can be either ACCEPT, REJECT or UNSET, this enum provides the necessary operations to
 * combine the states of two policies. The operations follow the rules of the trivalent logic.
 */
public enum PolicyState {
    ACCEPT, REJECT, UNSET;

    public PolicyState or(PolicyState other) {
        return switch (this) {
            case ACCEPT -> ACCEPT;
            case REJECT, UNSET -> other;
        };
    }

    public PolicyState xor(PolicyState other) {
        return switch (this) {
            case ACCEPT -> other.not();
            case REJECT -> other;
            case UNSET -> UNSET;
        };
    }

    public PolicyState and(PolicyState other) {
        return switch (this) {
            case ACCEPT -> other;
            case REJECT -> this;
            case UNSET -> switch (other) {
                case ACCEPT, UNSET -> UNSET;
                case REJECT -> REJECT;
            };
        };
    }

    public PolicyState not() {
        return switch (this) {
            case ACCEPT -> REJECT;
            case REJECT -> ACCEPT;
            case UNSET -> UNSET;
        };
    }
}
