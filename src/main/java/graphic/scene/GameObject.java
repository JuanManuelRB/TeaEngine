package graphic.scene;

import java.util.List;

public interface GameObject<E extends GameObject<?>> {
    List<E> childGameObjects();
    <T extends GameObject<E>> T parentGameObject();
}
