package aplication.update;

import aplication.objects.GameObject;

import java.util.HashSet;
import java.util.Set;

public class GraphicUpdater extends GameObject implements Updater<GraphicUpdater, GraphicUpdater.GraphicUpdated> {
    private final Set<GraphicUpdated> graphicUpdated = new HashSet<>();

    public GraphicUpdater() {
        super();
        addUpdater(this);
    }

//    @Override
//    public void add(GraphicUpdater gameObject) {
//        if (gameObject instanceof GraphicUpdated graphicUpdated) {
//            this.graphicUpdated.add(graphicUpdated);
//            gameObject.addUpdater(this);
//        }
//    }

    @Override
    public void add(GraphicUpdated graphicUpdated) {
        this.graphicUpdated.add(graphicUpdated);
        if (graphicUpdated instanceof GameObject gameObject)
            gameObject.addUpdater(this);

    }

    @Override
    public Class<GraphicUpdater> updaterClass() {
        return GraphicUpdater.class;
    }

    @Override
    public Class<GraphicUpdated> updatedClass() {
        return GraphicUpdated.class;
    }

    public void update() {
        for (GraphicUpdated graphicUpdated : this.graphicUpdated) {
            graphicUpdated.updateGraphic();
        }
    }

    @FunctionalInterface
    public interface GraphicUpdated {
        void updateGraphic();
//
//        default Class<GraphicUpdater> updater() {
//            return GraphicUpdater.class;
//        }
//
//        default Class<GraphicUpdated> updated() {
//            return GraphicUpdated.class;
//        }

    }
}
