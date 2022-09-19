package graphic;

import aplication.Resource;

public record GraphicResource() implements Resource<GraphicElement> {

    @Override
    public GraphicElement get() {
        return null;
    }
}
