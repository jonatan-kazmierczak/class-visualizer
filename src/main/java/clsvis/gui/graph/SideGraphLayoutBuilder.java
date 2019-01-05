package clsvis.gui.graph;

import clsvis.model.RelationDirection;
import clsvis.model.RelationType;
import clsvis.model.Class_;
import clsvis.model.ElementModifier;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FontMetrics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds visual layout of graph of {@link Class_}es.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
class SideGraphLayoutBuilder {

    private static final Logger logger = Logger.getLogger( SideGraphLayoutBuilder.class.getName() );

    // decision tables
    private static final RelationType[] relationTypes = {
        RelationType.SuperInterface, RelationType.SuperClass,
        RelationType.InnerClass, RelationType.InnerClass,
        RelationType.Association, RelationType.Dependency, RelationType.DependencyAnnotation, RelationType.DependencyThrows,
        RelationType.Association, RelationType.Dependency, RelationType.DependencyAnnotation, RelationType.DependencyThrows,
    };
    private static final RelationDirection[] relationDirections = {
        RelationDirection.Inbound, RelationDirection.Inbound,
        RelationDirection.Outbound, RelationDirection.Inbound,
        RelationDirection.Outbound, RelationDirection.Outbound, RelationDirection.Outbound, RelationDirection.Outbound,
        RelationDirection.Inbound, RelationDirection.Inbound, RelationDirection.Inbound, RelationDirection.Inbound,
    };

    /** Equal to size of the biggest screen. Initialized in constructor. */
    private static Dimension maxGraphSize;

    SideGraphLayoutBuilder() {
        // Find max screen resolution
        int width = 0, height = 0;
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            DisplayMode dm = gd.getDisplayMode();
            if (dm.getWidth() * dm.getHeight() > width * height) {
                width = dm.getWidth();
                height = dm.getHeight();
            }
        }
        if (logger.isLoggable( Level.FINEST )) {
            logger.finest( String.format( "#### max screen size: %d x %d", width, height ) );
        }
        maxGraphSize = new Dimension( width, height );
    }

    public void buildGraphLayout(
            Class_ class_, Collection<Vertex> vertices, Collection<Edge> edges,
            Dimension dimension,
            int cellPadding, int cellSpacing,
            FontMetrics classFM, FontMetrics abstractClassFM) {

        // Main class placed in center row and center column
        // SuperClasses and superInterfaces
        Collection<Vertex> rowTopVerts = new ArrayList<Vertex>( 0x10 );
        // Main class
        Collection<Vertex> rowCenterVerts = new ArrayList<Vertex>( 1 );
        // SubClasses and SubInterfaces
        Collection<Vertex> rowBottomVerts = new ArrayList<Vertex>( 0x80 );
        // Classes referenced from main one
        Collection<Vertex> colLeftVerts = new ArrayList<Vertex>( 0x200 );
        // Classes referencing main one
        Collection<Vertex> colRightVerts = new ArrayList<Vertex>( 0x200 );
        // Decision table of all vertices - the same number of elems as relationTypes
        Collection[] verticesTbl = {
            rowBottomVerts, rowBottomVerts,
            colRightVerts, colLeftVerts,
            colRightVerts, colRightVerts, colRightVerts, colRightVerts,
            colLeftVerts, colLeftVerts, colLeftVerts, colLeftVerts,
        };

        // Used classes - to avoid painting the same class more than once
        Collection<Class_> usedClasses = new HashSet<Class_>();

        // Create 2 collections containing half of SuperInterfaces each
        Collection<Class_> generalizations1 = new ArrayList<Class_>( 8 );
        Collection<Class_> generalizations2 = new ArrayList<Class_>( 8 );
        Collection<Class_> superInterfaces = class_.getRelations( RelationType.SuperInterface, RelationDirection.Outbound );
        int generCount = superInterfaces.size();
        int generIdx = 0;
        for (Class_ superInterface : superInterfaces) {
            if (++generIdx > (generCount >> 1)) {
                generalizations2.add( superInterface );
            } else {
                generalizations1.add( superInterface );
            }
        }

        // Create vertices and edges with proper dimensions but without positioning
        Vertex mainClassVertex = createVertex( class_, cellPadding, classFM, abstractClassFM, null );
        mainClassVertex.main = true;
        rowCenterVerts.add( mainClassVertex );
        // Top row
        createVerticesAndEdges(
                generalizations1, usedClasses,
                RelationType.SuperInterface, RelationDirection.Outbound, EdgePosition.Vertical,
                mainClassVertex, rowTopVerts, edges, cellPadding, cellSpacing, classFM, abstractClassFM );
        createVerticesAndEdges(
                class_.getRelations( RelationType.SuperClass, RelationDirection.Outbound ), usedClasses,
                RelationType.SuperClass, RelationDirection.Outbound, EdgePosition.Vertical,
                mainClassVertex, rowTopVerts, edges, cellPadding, cellSpacing, classFM, abstractClassFM );
        createVerticesAndEdges(
                generalizations2, usedClasses,
                RelationType.SuperInterface, RelationDirection.Outbound, EdgePosition.Vertical,
                mainClassVertex, rowTopVerts, edges, cellPadding, cellSpacing, classFM, abstractClassFM );
        // Remaining elements
        for (int i = 0; i < relationTypes.length; i++) {
            createVerticesAndEdges(
                    class_.getRelations( relationTypes[ i ], relationDirections[ i ] ),
                    usedClasses, relationTypes[ i ], relationDirections[ i ], getEdgePosition( relationTypes[ i ] ),
                    mainClassVertex, verticesTbl[ i ], edges, cellPadding, cellSpacing, classFM, abstractClassFM );
        }

        // Calculate minimum component size - at least vertex size + 20 * cellSpacing
        int minReqWidth = mainClassVertex.width + cellSpacing * 20;
        int minReqHeight = mainClassVertex.height + cellSpacing * 20;

        int[] rowTopSums = summarizeSizes( rowTopVerts, cellSpacing );
        int[] rowBottomSums = summarizeSizes( rowBottomVerts, cellSpacing );
        int[] colLeftSums = summarizeSizes( colLeftVerts, cellSpacing );
        int[] colRightSums = summarizeSizes( colRightVerts, cellSpacing );

        minReqWidth = Math.max( minReqWidth, rowTopSums[ 2 ] );
        minReqWidth = Math.max( minReqWidth, rowBottomSums[ 2 ] );
        minReqWidth += specialSum( colLeftSums[ 0 ], colRightSums[ 0 ], cellSpacing );

        minReqHeight = Math.max( minReqHeight, colLeftSums[ 3 ] );
        minReqHeight = Math.max( minReqHeight, colRightSums[ 3 ] );
        minReqHeight += specialSum( rowTopSums[ 1 ], rowBottomSums[ 1 ], cellSpacing );

        if (logger.isLoggable( Level.FINEST )) {
            logger.finest( String.format( "---- computed sizes: min:[%d,%d], pref:[%d,%d]",
                    minReqWidth, minReqHeight, dimension.width, dimension.height ) );
        }

        // Adjust sizes if smaller than required minimum - with scrollbars corrections
        int areaWidth = Math.max( dimension.width - 20, minReqWidth );
        int areaHeight = Math.max( dimension.height - 20, minReqHeight );
        dimension.setSize( areaWidth, areaHeight );

        int centerX = areaWidth / 2;
        int centerY = areaHeight / 2;

        // Position vertices
        positionRowVertices( rowTopVerts, VertexPlacement.Top, areaWidth, areaHeight, cellSpacing );
        positionRowVertices( rowBottomVerts, VertexPlacement.Bottom, areaWidth, areaHeight, cellSpacing );
        positionColumnVertices( colLeftVerts, VertexPlacement.Left, areaWidth, areaHeight, cellSpacing );
        positionColumnVertices( colRightVerts, VertexPlacement.Right, areaWidth, areaHeight, cellSpacing );
        mainClassVertex.setLocation( centerX - mainClassVertex.width / 2, centerY - mainClassVertex.height / 2 );
        positionEdges( edges, cellSpacing );

        // Copy to result layouted collection
        vertices.addAll( rowTopVerts );
        vertices.addAll( rowCenterVerts );
        vertices.addAll( rowBottomVerts );
        vertices.addAll( colLeftVerts );
        vertices.addAll( colRightVerts );

        // Cleanup of linked lists
        for (Collection coll : new Collection[]{ rowTopVerts, rowBottomVerts, colLeftVerts, colRightVerts }) {
            coll.clear();
        }
    }

    private static void positionRowVertices(
            Collection<Vertex> vertices,
            VertexPlacement placement, int areaWidth, int areaHeight, int cellSpacing) {
        if (vertices.isEmpty()) {
            return;
        }
        // Calculate base coordinates
        int centerX = areaWidth / 2; // center of available area
        int[] sizes = summarizeSizes( vertices, cellSpacing );
        int totalWidth = sizes[ 2 ];
        int maxHeight = sizes[ 1 ];
        int currX = centerX - totalWidth / 2 + cellSpacing;

        // Position vertices
        for (Vertex vertex : vertices) {
            int y = (placement == VertexPlacement.Top)
                    ? maxHeight - vertex.height // top row
                    : areaHeight - maxHeight; // bottom row
            vertex.setLocation( currX, y );
            currX += vertex.width + cellSpacing;
        }
    }

    private static void positionColumnVertices(
            Collection<Vertex> vertices,
            VertexPlacement placement, int areaWidth, int areaHeight, int cellSpacing) {
        if (vertices.isEmpty()) {
            return;
        }
        // Calculate base coordinates
        int centerY = areaHeight / 2;
        int[] sizes = summarizeSizes( vertices, cellSpacing );
        int totalHeight = sizes[ 3 ];
        int maxWidth = sizes[ 0 ];
        int currY = centerY - totalHeight / 2 + cellSpacing;

        // Position vertices
        for (Vertex vertex : vertices) {
            int x = (placement == VertexPlacement.Left)
                    ? maxWidth - vertex.width // left column
                    : areaWidth - maxWidth; // right column
            vertex.setLocation( x, currY );
            currY += vertex.height + cellSpacing;
        }
    }

    private static void positionEdges(Collection<Edge> edges, int cellSpacing) {
        for (Edge edge : edges) {
            ArrayList<Shape> connector = new ArrayList<Shape>( 3 );
            // Relation line
            int x1, y1, x2, y2;
            Path2D path = new Path2D.Double();
            if (isVerticalOrientation( edge )) {
                // Inheritance
                // Element below
                x1 = (int) edge.fromVertex.getCenterX();
                y1 = edge.fromVertex.y;
                // Element above
                x2 = (int) edge.toVertex.getCenterX();
                y2 = edge.toVertex.y + edge.toVertex.height + cellSpacing;
                //Top Triangle
                int x3 = x2;
                int y3 = y2 - cellSpacing;
                path.moveTo( x3, y3 );
                path.lineTo( x3 - cellSpacing / 2, y2 );
                path.lineTo( x3 + cellSpacing / 2, y2 );
                path.closePath();
            } else {
                // Horizontal
                // Nesting, association/dependency
                x1 = edge.fromVertex.x + edge.fromVertex.width + cellSpacing;
                // For main class, nesting is drawn above association/dependency
                y1 = edge.fromVertex.main
                        && !edge.fromVertex.class_.getRelations( RelationType.InnerClass, RelationDirection.Outbound ).isEmpty()
                        && !(edge.fromVertex.class_.getRelations( RelationType.Association, RelationDirection.Outbound ).isEmpty()
                        && edge.fromVertex.class_.getRelations( RelationType.Dependency, RelationDirection.Outbound ).isEmpty()
                        && edge.fromVertex.class_.getRelations( RelationType.DependencyThrows, RelationDirection.Outbound ).isEmpty())
                        ? edge.relationType == RelationType.InnerClass
                                ? edge.fromVertex.y + cellSpacing / 2
                                : edge.fromVertex.y + edge.fromVertex.height - cellSpacing / 2
                        : (int) edge.fromVertex.getCenterY();
                x2 = edge.toVertex.x - cellSpacing;
                y2 = (int) edge.toVertex.getCenterY();
                // Finishing right Line
                int x3 = x2 + cellSpacing;
                int y3 = y2;
                path.moveTo( x3, y3 );
                path.lineTo( x2, y2 );
                // Right Arrow (if not bi-di association)
                if (edge.relationType != RelationType.Association
                        || !edge.toVertex.class_.getRelations( RelationType.Association, RelationDirection.Outbound )
                        .contains( edge.fromVertex.class_ )) {
                    // 2 pixels shift to avoid accidental connections
                    path.moveTo( x3, y3 );
                    path.lineTo( x2 + 3, y3 - cellSpacing / 2 );
                    path.moveTo( x3, y3 );
                    path.lineTo( x2 + 3, y3 + cellSpacing / 2 );
                }
                // Starting line
                int x4 = x1 - cellSpacing;
                int y4 = y1;
                if (edge.relationType == RelationType.InnerClass) {
                    // Nesting
                    // Circle
                    int y5 = y4 - cellSpacing / 2;
                    connector.add( new Arc2D.Double( x4, y5, cellSpacing, cellSpacing, 0, 360, Arc2D.CHORD ) );
                    // Vertical line
                    int x6 = x1 - cellSpacing / 2;
                    path.moveTo( x6, y5 );
                    path.lineTo( x6, y5 + cellSpacing );
                }
                // Association/dependency: horizontal line
                path.moveTo( x4, y4 );
                path.lineTo( x1, y4 );
            } // if
            connector.add( path );
            connector.add( new Line2D.Double( x1, y1, x2, y2 ) );
            edge.connector = connector;

            // Realization or dependency: dashed line, otherwise: solid line
            edge.lineStyle
                    = isVerticalOrientation( edge )
                    && !edge.fromVertex.class_.modifiers.contains( ElementModifier.Interface )
                    && edge.toVertex.class_.modifiers.contains( ElementModifier.Interface )
                    || edge.relationType == RelationType.Dependency
                    || edge.relationType == RelationType.DependencyThrows
                    || edge.relationType == RelationType.DependencyAnnotation
                            ? EdgeLineStyle.Dashed : EdgeLineStyle.Solid;
        } // for edges
    }

    /**
     * Returns sizes of vertices: minWidth, minHeight, sumWidth, sumHeight.
     */
    private static int[] summarizeSizes(Collection<Vertex> vertices, int cellSpacing) {
        int minWidth = 0, minHeight = 0, sumWidth = 0, sumHeight = 0;
        for (Vertex vertex : vertices) {
            minWidth = Math.max( minWidth, vertex.width + cellSpacing );
            minHeight = Math.max( minHeight, vertex.height + cellSpacing );
            sumWidth += vertex.width + cellSpacing;
            sumHeight += vertex.height + cellSpacing;
        }
        return new int[]{
            minWidth + cellSpacing, minHeight + cellSpacing, sumWidth + cellSpacing, sumHeight + cellSpacing };
    }

    /**
     * Returns bigger value multiplied by 2.
     */
    private static int specialSum(int val1, int val2, int boundery) {
        return Math.max( val1, val2 ) << 1;
    }

    private static void createVerticesAndEdges(
            Collection<Class_> targetClasses,
            Collection<Class_> usedClasses,
            RelationType relationType,
            RelationDirection direction,
            EdgePosition position,
            Vertex mainClassVertex,
            Collection<Vertex> vertices, Collection<Edge> edges,
            int cellPadding, int cellSpacing, FontMetrics classFM, FontMetrics abstractClassFM) {
        // Check, if all related classes can fit into 1 full screen
        // - if not, make 1 symbol-replacement for all of them
        boolean tooManyClasses
                = relationType == RelationType.SuperClass || relationType == RelationType.SuperInterface
                        ? (mainClassVertex.width + cellSpacing) * (targetClasses.size() + 2) > maxGraphSize.width
                        : (mainClassVertex.height + cellSpacing) * (targetClasses.size() + 2) > maxGraphSize.height;
        if (tooManyClasses) {
            createVertexAndEdge(
                    mainClassVertex.class_, relationType, direction, position, mainClassVertex, vertices, edges, cellPadding,
                    classFM, abstractClassFM, String.valueOf( targetClasses.size() ) + " classes" );
        } else {
            // Process target classes
            for (Class_ targetClass : targetClasses) {
                // Don't paint the same class more than once
                if (!usedClasses.add( targetClass )) {
                    continue;
                }
                createVertexAndEdge(
                        targetClass, relationType, direction, position, mainClassVertex, vertices, edges, cellPadding,
                        classFM, abstractClassFM, null );
            }
        }
    }

    private static void createVertexAndEdge(
            Class_ targetClass,
            RelationType relationType,
            RelationDirection direction,
            EdgePosition position,
            Vertex mainClassVertex,
            Collection<Vertex> vertices, Collection<Edge> edges,
            int cellPadding, FontMetrics classFM, FontMetrics abstractClassFM,
            String title
    ) {
        Vertex targetVertex = createVertex( targetClass, cellPadding, classFM, abstractClassFM, title );
        Edge edge = (direction == RelationDirection.Outbound)
                ? new Edge( mainClassVertex, targetVertex, relationType, position )
                : new Edge( targetVertex, mainClassVertex, relationType, position );
        vertices.add( targetVertex );
        edges.add( edge );
    }

    private static Vertex createVertex(Class_ class_, int cellPadding, FontMetrics classFM, FontMetrics abstractClassFM, String title) {
        FontMetrics fm = title == null && class_.isAbstract() ? abstractClassFM : classFM;
        return new Vertex( 0, 0,
                fm.stringWidth( title != null ? title : class_.name ), fm.getHeight(),
                fm.getAscent(), cellPadding, class_, title );
    }

    private static EdgePosition getEdgePosition(RelationType relType) {
        return relType == RelationType.SuperClass || relType == RelationType.SuperInterface
                ? EdgePosition.Vertical : EdgePosition.Horizontal;
    }

    private static boolean isVerticalOrientation(Edge edge) {
        return edge.relationType == RelationType.SuperClass || edge.relationType == RelationType.SuperInterface;
    }
}
