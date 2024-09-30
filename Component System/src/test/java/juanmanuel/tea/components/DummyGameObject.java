//package juanmanuel.tea.components;
//
//import java.util.function.Function;
//
//class DummyGameObject extends GameObject<DummyGameObject> implements DummyUpdated {
//    private final Runnable dummyUpdate;
//    private final String name;
//
//    public DummyGameObject() {
//        this.dummyUpdate = () -> {};
//        this.name = "dummy";
//    }
//
//    public DummyGameObject(String name) {
//        this.dummyUpdate = () -> {};
//        this.name = name;
//    }
//
//    public DummyGameObject(Runnable dummyUpdate) {
//        this.dummyUpdate = dummyUpdate;
//        this.name = "dummy";
//    }
//
//    public DummyGameObject(String name, Runnable dummyUpdate) {
//        this.dummyUpdate = dummyUpdate;
//        this.name = name;
//    }
//
//    public String name() {
//        return name;
//    }
//
//    @Override
//    public void dummyUpdate() {
//        dummyUpdate.run();
//    }
//
//    @Override
//    public String toString() {
//        return STR."DummyGameObject{name='\{name}'}";
//    }
//
//    @Override
//    protected void onConnectChild(DummyGameObject child) {
//        super.onConnectChild(child);
//        System.out.println(STR."Child entered: \{child} on \{this}");
//    }
//
//    @Override
//    protected void onConnectParent(DummyGameObject parent) {
//        super.onConnectParent(parent);
//        System.out.println(STR."Parent entered: \{parent} on \{this}");
//    }
//
//    private Function<DummyGameObject, ?> onParentSubscribeAction;
//
//    public <T> void addOnParentSubscribe(Function<DummyGameObject, T> function) {
//        onParentSubscribeAction = function;
//    }
//
//    @Override
//    protected <Upr extends Updater<Upr, Upd, SC>, Upd extends Updated, SC extends StructuredComputation<Upr, Upd, SC>> void onParentSubscribe(SC computation, DummyGameObject parent) {
//        super.onParentSubscribe(computation, parent);
//        System.out.println(STR."Parent subscribed: \{parent} to \{computation}");
//        if (onParentSubscribeAction != null) {
//            onParentSubscribeAction.apply(parent);
//        }
//    }
//
//    private Function<DummyGameObject, ?> onChildSubscribeAction;
//
//    public <T> void addOnChildSubscribe(Function<DummyGameObject, T> function) {
//        onChildSubscribeAction = function;
//    }
//
//    @Override
//    protected <Upr extends Updater<Upr, Upd, SC>, Upd extends Updated, SC extends StructuredComputation<Upr, Upd, SC>> void onChildSubscribe(SC computation, DummyGameObject child) {
//        super.onChildSubscribe(computation, child);
//        System.out.println(STR."Child subscribed: \{child} to \{computation}");
//        if (onChildSubscribeAction != null) {
//            onChildSubscribeAction.apply(child);
//        }
//    }
//
//    private Runnable onSubscribeAction;
//
//    public void addOnSubscribe(Runnable action) {
//        onSubscribeAction = action;
//    }
//
//    @Override
//    protected <Upr extends Updater<Upr, Upd, SC>, Upd extends Updated, SC extends StructuredComputation<Upr, Upd, SC>> void onSubscribe(SC computation) throws RuntimeException {
//        super.onSubscribe(computation);
//        System.out.println(STR."Subscribed: \{this} to \{computation}");
//        if (onSubscribeAction != null) {
//            onSubscribeAction.run();
//        }
//    }
//}
