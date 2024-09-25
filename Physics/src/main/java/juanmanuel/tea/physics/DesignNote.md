```java
import juanmanuel.tea.physics.PhysicsUpdated;
import juanmanuel.tea.physics.PhysicsUpdater;

public static void main(String[] args) {
    class MyPhysicsUpdated implements PhysicsUpdated {
        @Override
        public void updatePhysics() {
            // Do something
        }
    }
    
    PhysicsUpdater physicsUpdater = new DefaultPhysicsUpdater();
    PhysicsUpdated myPhysicsUpdated = new MyPhysicsUpdatedGO();
    
//    enterOnUpdaterPolicy(SUBSCRIBE, PhysicsUpdater.class)
//            .applyTo(physicsUpdated);
    
    enterOnUpdaterPolicy(SUBSCRIBE_CHILDREN, PhysicsUpdater.class) // Subscribes the children of the applied object
//            .applyTo(myPhysicsUpdated); // Applies the policy to the object, but not to its children.
            .applyOnCascadeTo(myPhysicsUpdated); // Applies the policy to the object and its children, and their children, and so on.
    
//    enterOnUpdaterPolicy(SUBSCRIBE_DESCENDANTS, PhysicsUpdater.class) // Subscribes the descendants of the applied object
//            .applyTo(myPhysicsUpdated); // Applies the policy to the object, but not to its descendants.
    
    // The first policy is set for all the descendants of the physicsUpdater, the second policy is only set for the applied object.
    // The behavior of the two policies is similar, but not exactly the same. Since the first policy is set for all the
    // descendants of the physicsUpdater, even if any descendant is removed from the physicsUpdater, the policy will still be applied.
    
}
```