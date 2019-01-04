package clsvis.gui.graph;

import clsvis.model.Class_;
import java.awt.Rectangle;

/**
 * Representation of {@link Class_} on the graph.
 *
 * @author Jonatan Kazmierczak [Jonatan.Kazmierczak (at) gmail (dot) com]
 */
class Vertex extends Rectangle {
	public int textX, textY; // coordinates of text
	public final int textYShift, cellPadding;
	public final Class_ class_;
	public boolean main;
	/** Optional title - text shown instead of class name. */
	private final String title;

	public Vertex(int x, int y, int textWidth, int textHeigth,
			int textYShift, int cellPadding, Class_ class_, String title) {
		super(x, y, textWidth + cellPadding, textHeigth + cellPadding);
		this.textYShift = textYShift;
		this.cellPadding = cellPadding;
		this.class_ = class_;
		this.title = title;
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		this.textX = x + cellPadding / 2;
		this.textY = y + cellPadding / 2 + textYShift;
	}

	public String getTitle() {
		return title != null ? title : class_.name;
	}

	/** Tests, if the the vertex is a mock - not representing real class. */
	public boolean isRepresentsClass() {
		return title == null;
	}
}
