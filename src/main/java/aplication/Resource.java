package aplication;

import graphic.GraphicResource;

public sealed interface Resource<T> permits GraphicResource {
    T get();
}
