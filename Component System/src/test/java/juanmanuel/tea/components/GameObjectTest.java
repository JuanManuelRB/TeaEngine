//package juanmanuel.tea.components;
//
//import juanmanuel.tea.graph.ApplicationGraph;
//import org.jgrapht.graph.DefaultWeightedEdge;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class GameObjectTest {
//    private DummyGameObject gameObject;
//    private ApplicationGraph<DummyGameObject, DefaultWeightedEdge> graph;
//    private boolean parentSubscribed = false;
//    private boolean childSubscribed = false;
//    private boolean subscribed = false;
//
//    public static class Niam {
//        private final String niam;
//        public Niam(String niam) {
//            this.niam = niam;
//        }
//    }
//
//    @BeforeEach
//    void setUp() {
//        gameObject = new DummyGameObject("gameObject");
//        graph = new ApplicationGraph<>(DefaultWeightedEdge.class);
//        gameObject.handleGraphReplace(graph);
//        parentSubscribed = false;
//        childSubscribed = false;
//        subscribed = false;
//    }
//
//    @Test
//    void onParentSubscribe() {
//        var child = new DummyGameObject("child");
//        gameObject.addChild(child);
//        child.addOnParentSubscribe( _ -> {
//            parentSubscribed = true;
//            return null;
//        });
//        var computation = new DummyComputation(DummyUpdater.class, gameObject);
//        assertTrue(parentSubscribed);
//    }
//
//    @Test
//    void onChildSubscribe() {
//        var parent = new DummyGameObject("parent");
//        gameObject.addParent(parent);
//        parent.addOnChildSubscribe( _ -> {
//            childSubscribed = true;
//            return null;
//        });
//        var computation = new DummyComputation(DummyUpdater.class, gameObject);
//        assertTrue(childSubscribed);
//    }
//
//    @Test
//    void onSubscribe() {
//        gameObject.addOnSubscribe(() -> subscribed = true);
//        var computation = new DummyComputation(DummyUpdater.class, gameObject);
//        assertTrue(subscribed);
//    }
//
//    @Test
//    void isUpdatedIn() {
//    }
//
//    @Test
//    void isUpdatedBy() {
//    }
//}