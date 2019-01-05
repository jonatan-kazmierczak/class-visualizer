package clsvis.gui.graph;

import clsvis.model.RelationType;
import java.awt.Shape;
import java.util.List;

/**
 * Describes edge connecting 2 instances of {@link Vertex}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
class Edge {

    public final Vertex fromVertex;
    public final Vertex toVertex;
    public final RelationType relationType;
    public final EdgePosition position;
    public EdgeLineStyle lineStyle;
    /** Connector of 2 vertices (last element) with terminators (first elements). */
    public List<Shape> connector;

    public Edge(Vertex fromVertex, Vertex toVertex, RelationType relationType, EdgePosition position) {
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
        this.relationType = relationType;
        this.position = position;
    }
}
