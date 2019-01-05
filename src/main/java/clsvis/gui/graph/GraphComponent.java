package clsvis.gui.graph;

import clsvis.gui.ColorContext;
import clsvis.gui.ConstantValues;
import clsvis.gui.StructureBuilder;
import clsvis.model.RelationType;
import clsvis.model.Class_;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

/**
 * Component representing graph.<br>
 * Main method causing repainting is {@link #setMainClass(clsvis.model.Class_)}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public final class GraphComponent extends JPanel implements Scrollable {

    private enum PaintAction {
        GRAPH, SELECTED_ITEM
    }

    // Constants
    private static final Logger logger = Logger.getLogger( GraphComponent.class.getName() );

    private static final BasicStroke border = new BasicStroke( 2.0f );
    private static final BasicStroke solidLine = new BasicStroke( 1.0f );
    private static final BasicStroke dashedLine = new BasicStroke(
            1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{ 10.0f }, 0.0f );
    private static final SideGraphLayoutBuilder layoutBuilder = new SideGraphLayoutBuilder();

    private static final Map<RelationType, Color> relationColors = new EnumMap<RelationType, Color>( RelationType.class );

    // Properties
    private final int size = 12;
    private final Font classFont = new Font( Font.SANS_SERIF, Font.BOLD, size );
    private final Font abstractClassFont = new Font( Font.SANS_SERIF, Font.BOLD | Font.ITALIC, size );
    private final Color selectedClassForeground = Color.red; // foreground for selected vertex
    private final Color classForeground = new Color( ColorContext.ClassProcessed.colorInt );
    private final Color classUnprocessedForeground = new Color( ColorContext.ClassUnprocessed.colorInt );

    private Class_ mainClass;
    private Vertex selectedVertex;
    private Vertex prevSelectedVertex;
    private PaintAction paintAction;

    // Attributes
    private final int cellPadding = size;
    private final int cellSpacing = size;
    private List<Vertex> vertices = Collections.EMPTY_LIST;
    private List<Edge> edges = Collections.EMPTY_LIST;

    static {
        for (RelationType relationType : RelationType.values()) {
            relationColors.put( relationType, new Color( relationType.colorNum ) );
        }
    }

    /**
     * Registers handler of Copy action.
     */
    public GraphComponent() {
        getActionMap().put( TransferHandler.getCopyAction().getValue( Action.NAME ), TransferHandler.getCopyAction() );
        setTransferHandler( new GraphTransferHandler() );
        setFocusable( true );
    }

    /**
     * Sets up the background color of itself and the parent (if JViewPort).
     */
    @Override
    public void addNotify() {
        super.addNotify();
        Color bgColor = UIManager.getColor( "TextArea.background" );
        setBackground( bgColor );
        Container parent = getParent();
        if (parent instanceof JViewport) {
            parent.setBackground( bgColor );
        }
    }

    private void prepareLayout() {
        logger.finest( "---- prepareLayout" );
        vertices = new ArrayList<Vertex>( 0x400 );
        edges = new ArrayList<Edge>( 0x400 );
        Dimension graphSize = getPreferredSize();
        layoutBuilder.buildGraphLayout(
                mainClass,
                vertices, edges,
                graphSize,
                cellPadding, cellSpacing,
                getFontMetrics( classFont ), getFontMetrics( abstractClassFont ) );
        setPreferredSize( graphSize );
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (logger.isLoggable( Level.FINEST )) {
            logger.finest( this.getClass().getSimpleName() + ".paintComponent" );
        }
        long timeStart = System.currentTimeMillis();

        super.paintComponent( g2 );
        paintGraph( g2 );
        // Current handling of selection caused by painting bug on Windows 7
        if (paintAction == PaintAction.SELECTED_ITEM) {
            paintSelection( g2 );
        }
        paintAction = null;

        long timeStop = System.currentTimeMillis();
        if (logger.isLoggable( Level.FINEST )) {
            logger.finest( String.format( "-- painting time: %dms", timeStop - timeStart ) );
        }
    }

    private void paintGraph(Graphics2D g2) {
        g2.setColor( classForeground );

        // Draw edges: from end - throwable deps before other deps
        for (int i = edges.size() - 1; i >= 0; i--) {
            drawEdge2( g2, edges.get( i ) );
        }

        // Draw vertices
        g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP );
        g2.setStroke( border );
        for (Vertex vertex : vertices) {
            if (selectedVertex == null && vertex.main) {
                prevSelectedVertex = selectedVertex = vertex;
            }
            if (vertex.isRepresentsClass()) {
                g2.setColor( ConstantValues.backgroundColorMap.get( vertex.class_.kind ) );
                g2.fill( vertex );
            }
            g2.setColor( !vertex.class_.relationsProcessed && vertex.isRepresentsClass()
                    ? classUnprocessedForeground : classForeground );
            g2.setFont( vertex.class_.isAbstract() && vertex.isRepresentsClass() ? abstractClassFont : classFont );
            g2.drawString( vertex.getTitle(), vertex.textX, vertex.textY );
            drawVertexOutline( g2, vertex );
        }
    }

    /**
     * Paint deselected vertex' border and new selected one's border.
     */
    private void paintSelection(Graphics2D g2) {
        g2.setStroke( border );
        drawVertexOutline( g2, prevSelectedVertex );
        drawVertexOutline( g2, selectedVertex );
        prevSelectedVertex = selectedVertex;
    }

    private void drawVertexOutline(Graphics2D g2, Vertex vertex) {
        g2.setColor( vertex == selectedVertex ? selectedClassForeground : classForeground );
        g2.draw( vertex );
    }

    private void drawEdge2(Graphics2D g2, Edge edge) {
        g2.setColor(
                edge.relationType == RelationType.DependencyThrows || edge.relationType == RelationType.DependencyAnnotation
                        ? relationColors.get( edge.relationType ) : classForeground );
        // line
        g2.setStroke( edge.lineStyle == EdgeLineStyle.Solid ? solidLine : dashedLine );
        int connectorCount = edge.connector.size();
        g2.draw( edge.connector.get( connectorCount - 1 ) );
        // terminators
        g2.setStroke( solidLine );
        for (int i = connectorCount - 2; i >= 0; i--) {
            g2.draw( edge.connector.get( i ) );
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Vertex vertex = vertexAtPoint( event.getX(), event.getY() );
        return vertex != null && vertex.isRepresentsClass()
                ? StructureBuilder.buildClassRelationsSummaryTable( vertex.class_ ) : null;
    }

    /**
     * Returns {@link Vertex} at the given point or null, if no vertex there.
     */
    private Vertex vertexAtPoint(int x, int y) {
        for (Vertex vertex : vertices) {
            if (vertex.contains( x, y )) {
                return vertex;
            }
        }
        return null;
    }

    /**
     * Gets main class_.
     */
    public Class_ getMainClass() {
        return mainClass;
    }

    /**
     * Sets main class_ and causes repainting of the component.
     */
    public void setMainClass(Class_ mainClass) {
        this.mainClass = mainClass;
        paintAction = PaintAction.GRAPH;
        selectedVertex = null;
        prevSelectedVertex = null;
        prepareLayout();
        revalidate();
        repaint();
    }

    /**
     * Selects visually vertex at the given point and returns class representing by it. No painting and returning null,
     * if no vertex at that point.
     */
    public Class_ selectVertexAt(int x, int y) {
        Vertex selectedVertex = vertexAtPoint( x, y );
        if (selectedVertex != null && selectedVertex != prevSelectedVertex && selectedVertex.isRepresentsClass()) {
            this.selectedVertex = selectedVertex;
            paintAction = PaintAction.SELECTED_ITEM;
            repaint();
        }
        return selectedVertex != null ? selectedVertex.class_ : null;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (vertices.isEmpty() ? 0
                : orientation == SwingConstants.VERTICAL ? vertices.get( 0 ).height : vertices.get( 0 ).width) + cellSpacing;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width) - cellSpacing;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Allows to copy graph to the clipboard as an image by Copy action.
     */
    static class GraphTransferHandler extends TransferHandler {

        @Override
        protected Transferable createTransferable(JComponent comp) {
            if (logger.isLoggable( Level.FINEST )) {
                logger.finest( this.getClass().getSimpleName() + ".createTransferable" );
            }
            if (!(comp instanceof GraphComponent)) {
                throw new IllegalStateException( "Unsupported component type: " + comp.getClass().getName() );
            }
            GraphComponent graphComponent = (GraphComponent) comp;
            Dimension size = graphComponent.getPreferredSize();
            BufferedImage image = new BufferedImage( size.width, size.height, BufferedImage.TYPE_INT_RGB );
            Graphics2D g = image.createGraphics();
            // fill the whole image area with background color
            g.setColor( Color.WHITE );
            g.fillRect( 0, 0, size.width, size.height );
            graphComponent.paintGraph( g );
            return new GraphTransferable( image );
        }

        @Override
        public int getSourceActions(JComponent comp) {
            if (logger.isLoggable( Level.FINEST )) {
                logger.finest( this.getClass().getSimpleName() + ".getSourceActions" );
            }
            return COPY;
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return false;
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
            if (logger.isLoggable( Level.FINEST )) {
                logger.finest( this.getClass().getSimpleName() + ".exportToClipboard" );
            }
            super.exportToClipboard( comp, clip, action );
        }
    } //class

    /**
     * Transfers graph image to the clipboard.
     */
    static class GraphTransferable implements Transferable {

        private static final DataFlavor[] SUPPORTED_DATA_FLAVORS = { DataFlavor.imageFlavor };

        private final Image transferData;

        public GraphTransferable(Image transferData) {
            this.transferData = transferData;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return SUPPORTED_DATA_FLAVORS;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals( flavor );
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported( flavor )) {
                throw new UnsupportedFlavorException( flavor );
            }
            return transferData;
        }
    } //class
}
