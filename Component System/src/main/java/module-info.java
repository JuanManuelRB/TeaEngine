module tea_engine.core {
    requires transitive org.jgrapht.core;
    requires org.jspecify;
    exports juanmanuel.tea.components;
    exports juanmanuel.tea.graph;
    exports juanmanuel.tea.graph.policy;
    exports juanmanuel.tea.graph.validation;
    exports juanmanuel.tea.graph.callbacks;
    exports juanmanuel.tea.graph.operation_failures;
    exports juanmanuel.tea.utils;
}