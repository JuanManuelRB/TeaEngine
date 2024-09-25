# Notas de Diseño

- Cada Escena se ejecuta en su propio hilo.
- Cada Vista puede ejecutarse en un hilo de ejecución.
- Una Vista puede ser la composición de otras varias Vistas.
- Existe una Vista principal.

Como quiero que sea la aplicación:


### Programa basico

```java

import aplication.update.PhysicsUpdater;

class Main {
    public static void main(String[] args) {
        var phyUpdater = new PhysicsUpdater();
        var firstGameObject = new CustomGameObject("First");
        var secondGameObject = new CustomGameObject("Second");
        phyUpdater.addChild(firstGameObject) // Subscribes the GameObject to the updater no matter if the game object has cascade subscribe enabled or not
                .allowCascadeSubscriptionOf(PhysicsUpdater.class, GraphicUpdater.class) // Cascade subscribe to the specified updaters
                .disallowCascadeSubscribeOf(PhysicsUpdater.class) // Cascade subscribe to all updaters except the specified ones
                .cascadeSubscribeAll()
                .cascadeSubscribeNone()
                .allowCascadeUnsubscriptionOf(PhysicsUpdater.class, GraphicUpdater.class) // Cascade unsubscribe to the specified updaters
                .disallowCascadeUnsubscribeOf(PhysicsUpdater.class) // Cascade unsubscribe to all updaters except the specified ones
                .cascadeUnsubscribeAll()
                .cascadeUnsubscribeNone();
        phyUpdater.addChild(secondGameObject);
        phyUpdater.update();
    }
}
```

### Programa con scheduler

```java
class Main {
    public static void main(String[] args) {
        var goArr = new GameObject[10];
        ... // Fill the array
        var firstGameObject = new CustomGameObject("First");
        var secondGameObject = new CustomGameObject("Second");

        var phyUpdater = new PhysicsUpdater();
//        phyUpdater.addChild(goArr[0]); No es necesario, ya que se añaden en el batch. Es sinonimo de phyUpdater.addBatch(goArr)
        phyUpdater.addBatch(goArr) // Crea un nuevo batch, añade todos los GameObjects al updater y los ejecuta simultaneamente. Cada batch se ejecuta en paralelo
                .continueSecuential(firstGameObject, secondGameObject) // Una vez terminado el batch, se ejecuta otro batch con los GameObjects especificados
                .continueSecuential( // Este batch se ejecuta en secuencia al anterior batch
                        SECUENTIAL, // Indica que cada uno de los elementos del batch se ejecuta en secuencia
                        () -> System.out.println("Next 1"),
                        () -> System.out.println("Next 2"))
                .continueParallel( // Este batch se ejecuta en paralelo al anterior batch, pero no antes.
                        PARALLEL, // Indica que cada uno de los elementos del batch se ejecuta en paralelo
                        () -> System.out.println("Next 3"),
                        () -> System.out.println("Next 4"));
        Optional<PhysicBatch> batch = phyUpdater.batchOf(goArr[0]); // Devuelve el batch al que pertenece el GameObject, si no pertenece a ninguno devuelve Optional.absent()
        phyUpdater.start(); // Inicia el updater
    }
}
```



