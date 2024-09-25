//package juanmanuel.tea.components;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class StructuredComputationTest {
//
//    @Test
//    void updated() {
//        var updated = (DummyUpdated) () -> {};
//        var computation = new DummyComputation(DummyUpdater.class, updated);
//
//        assertEquals(updated, computation.updated());
//    }
//
//    @Test
//    void updaterClass() {
//        var computation = new DummyComputation(DummyUpdater.class, () -> {});
//        assertEquals(DummyUpdater.class, computation.updaterClass());
//    }
//
//    @Test
//    void updatedClass() {
//        var computation = new DummyComputation(DummyUpdater.class, () -> {});
//        assertTrue(DummyUpdated.class.isAssignableFrom(computation.updatedClass()));
//        // TODO
//    }
//
//    @Test
//    void computationClass() {
//        var computation = new DummyComputation(DummyUpdater.class, () -> {});
//        assertEquals(DummyComputation.class, computation.computationClass());
//    }
//
//    @Test
//    void computationSupplier() {
//        var computation = new DummyComputation(DummyUpdater.class, () -> {});
//        var supplier = computation.computationSupplier(() -> {});
//        assertEquals(DummyComputation.class, supplier.get().computationClass());
//        assertEquals(DummyUpdater.class, supplier.get().updaterClass());
//        assertTrue(DummyUpdated.class.isAssignableFrom(supplier.get().updatedClass()));
//    }
//
//    @Test
//    void compute() {
//        var computed = new AtomicBoolean(false);
//        var updated = (DummyUpdated) () -> computed.set(true);
//        var updater = new DummyUpdater();
//        var computation = new DummyComputation(DummyUpdater.class, updated);
//
//        assertFalse(computed.get());
//        updater.update(computation);
//        assertTrue(computed.get());
//    }
//
//    @Test
//    void onStartCompute() {
//        AtomicBoolean up = new AtomicBoolean();
//        var computation = new DummyComputation(DummyUpdater.class, () -> {});
//        var updater = new DummyUpdater();
//        computation.addOnStartCompute(() -> up.set(true));
//        try {
//            computation.startComputationBy(updater);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        assertTrue(up.get());
//    }
//
//    @Test
//    void onFinishCompute() {
//        AtomicBoolean up = new AtomicBoolean();
//        var computation = new DummyComputation(DummyUpdater.class, () -> {});
//        var updater = new DummyUpdater();
//        computation.addOnFinishCompute(() -> up.set(true));
//        try {
//            computation.startComputationBy(updater);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        assertTrue(up.get());
//    }
//
//    @Test
//    void setBefore() {
//        var parent = new DummyGameObject("parent");
//        var previousChildren = List.of(
//                new DummyGameObject("previousChild1"),
//                new DummyGameObject("previousChild2"),
//                new DummyGameObject("previousChild3")
//        );
//        var newChild = new DummyGameObject("newChild");
//        var parentComputation = new DummyComputation(DummyUpdater.class, parent);
//        var previousChildrenComputations = previousChildren.stream()
//                .map(child -> new DummyComputation(DummyUpdater.class, child))
//                .toList();
//        for (var childComputation : previousChildrenComputations) {
//            try {
//                parentComputation.addChild(childComputation).orElseThrow();
//            } catch (Throwable e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        var newChildComputation = new DummyComputation(DummyUpdater.class, newChild);
//
//        var res = newChildComputation.setBefore(parent);
//
//        assertTrue(res.isPresent());
//        if (!(res instanceof Resulted.Present<DummyComputation, ?> present))
//            fail();
//        else {
//            assertEquals(parentComputation, present.value());
//            assertTrue(newChildComputation.parents().contains(parentComputation));
//            for (var childComputation : previousChildrenComputations) {
//                assertFalse(childComputation.parents().contains(parentComputation));
//            }
//        }
//    }
//
//    @Test
//    void setAfter() {
//        var child = new DummyGameObject("child");
//        var previousParents = List.of(
//                new DummyGameObject("previousParent1"),
//                new DummyGameObject("previousParent2"),
//                new DummyGameObject("previousParent3")
//        );
//        var newParent = new DummyGameObject("newParent");
//        var childComputation = new DummyComputation(DummyUpdater.class, child);
//        var previousParentsComputations = previousParents.stream()
//                .map(parent -> new DummyComputation(DummyUpdater.class, parent))
//                .toList();
//        for (var parentComputation : previousParentsComputations)
//            childComputation.addParent(parentComputation);
//
//        var newParentComputation = new DummyComputation(DummyUpdater.class, newParent);
//
//        var res = newParentComputation.setAfter(child);
//
//        assertTrue(res.isPresent());
//        if (!(res instanceof Resulted.Present<DummyComputation, ?> present))
//            fail();
//        else {
//            assertEquals(childComputation, present.value());
//            assertTrue(newParentComputation.children().contains(childComputation));
//            for (var parentComputation : previousParentsComputations) {
//                assertFalse(childComputation.parents().contains(parentComputation));
//            }
//        }
//    }
//
//    @Test
//    void addBefore() {
//
//    }
//
//    @Test
//    void addAfter() {
//
//    }
//
//    @Test
//    void updatedChildren() {
//        var parent = new DummyGameObject("parent");
//        var child = new DummyGameObject("child");
//        var parentComputation = new DummyComputation(DummyUpdater.class, parent);
//        var childComputation = new DummyComputation(DummyUpdater.class, child);
//        parentComputation.addChild(childComputation).orElseThrow();
//
//        assertTrue(parentComputation.updatedChildren().contains(child));
//    }
//
//    @Test
//    void updatedParents() {
//        var parent = new DummyGameObject("parent");
//        var child = new DummyGameObject("child");
//        var parentComputation = new DummyComputation(DummyUpdater.class, parent);
//        var childComputation = new DummyComputation(DummyUpdater.class, child);
//        childComputation.addParent(parentComputation);
//
//        assertTrue(childComputation.updatedParents().contains(parent));
//    }
//
//    @Test
//    void updatedDescendants() {
//        var parent = new DummyGameObject("parent");
//        var child = new DummyGameObject("child");
//        var grandChild = new DummyGameObject("grandChild");
//        var parentComputation = new DummyComputation(DummyUpdater.class, parent);
//        var childComputation = new DummyComputation(DummyUpdater.class, child);
//        var grandChildComputation = new DummyComputation(DummyUpdater.class, grandChild);
//        parentComputation.addChild(childComputation).orElseThrow();
//        childComputation.addChild(grandChildComputation).orElseThrow();
//
//        assertTrue(parentComputation.updatedDescendants().contains(child));
//        assertTrue(parentComputation.updatedDescendants().contains(grandChild));
//    }
//
//    @Test
//    void updatedAncestors() {
//        var parent = new DummyGameObject("parent");
//        var child = new DummyGameObject("child");
//        var grandChild = new DummyGameObject("grandChild");
//        var parentComputation = new DummyComputation(DummyUpdater.class, parent);
//        var childComputation = new DummyComputation(DummyUpdater.class, child);
//        var grandChildComputation = new DummyComputation(DummyUpdater.class, grandChild);
//        parentComputation.addChild(childComputation).orElseThrow();
//        childComputation.addChild(grandChildComputation).orElseThrow();
//
//        assertTrue(grandChildComputation.updatedAncestors().contains(child));
//        assertTrue(grandChildComputation.updatedAncestors().contains(parent));
//    }
//
//    @Test
//    void updatedGraph() {
//    }
//
//    @Test
//    void removeComputation() {
//    }
//
//    @Test
//    void isUpdatedIn() {
//    }
//
//    @Test
//    void isEquivalent() {
//    }
//
//    @Test
//    void onEnterParent() {
//    }
//
//    @Test
//    void onLeaveParent() {
//    }
//}