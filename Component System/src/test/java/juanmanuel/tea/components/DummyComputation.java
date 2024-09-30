//package juanmanuel.tea.components;
//
//import juanmanuel.tea.graph.ApplicationEdge;
//import juanmanuel.tea.graph.Graph;
//
//import java.util.function.Supplier;
//
//public class DummyComputation extends StructuredComputation<DummyUpdater, DummyUpdated, DummyComputation> {
//    private static final Graph<DummyComputation, ApplicationEdge> graph = new Graph<>(ApplicationEdge.class);
//    private final String name;
//    public DummyComputation(String name, Class<DummyUpdater> updaterClass, DummyUpdated updated) {
//        super(updaterClass, updated);
//        this.name = name;
//    }
//
//    public DummyComputation(Class<DummyUpdater> updaterClass, DummyUpdated updated) {
//        this("", updaterClass, updated);
//    }
//
//    @Override
//    protected Supplier<DummyComputation> computationSupplier(DummyUpdated updated) {
//        return () -> new DummyComputation(DummyUpdater.class, updated);
//    }
//
//    private Runnable onStartCompute = () -> {};
//
//    @Override
//    protected void onStartCompute(DummyUpdater updater) throws InterruptedException {
//        super.onStartCompute(updater);
//        onStartCompute.run();
//    }
//
//    public void addOnStartCompute(Runnable onStartCompute) {
//        this.onStartCompute = onStartCompute;
//    }
//
//    private Runnable onFinishCompute = () -> {};
//
//    @Override
//    protected void onFinishCompute(DummyUpdater updater) throws InterruptedException {
//        super.onFinishCompute(updater);
//        onFinishCompute.run();
//    }
//
//    public void addOnFinishCompute(Runnable onFinishCompute) {
//        this.onFinishCompute = onFinishCompute;
//    }
//
//    @Override
//    public String toString() {
//        return "DummyComputation{name='" + name + "'}";
//    }
//}
