package clsvis.model;

import clsvis.Utils;

/**
 * Indicates kind of element and holds some of properties.<br/>
 * This class is equivalent of {@link javax.lang.model.element.ElementKind}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public enum ElementKind {
    Class( 0xFFF2CC ),
    Interface( 0xCCFFCC ),
    Enum( 0xCCCCFF ),
    AnnotationType( 0xFECCFF ),
    Throwable( 0xFF9F80 ),
    Extends( 0x4F4F4F, '\u25b2' ),
    Implements( 0xA9A9A9, '\u25b2' ),
    Constants( 0xFF6F60, '\u25a0' ),
    Fields( 0xA0C9E8, '\u25a0' ),
    Properties( 0x4A72A1, '\u25a0' ),
    Constructors( 0xFFA500, '\u2666' ),
    Methods( 0xFF00FF, '\u2666' ),
    Annotations( 0xFECCFF, '\u25cf' ),
    Parameters( 0x008080, '\u25cf' ),
    Throws( 0xA90000, '\u25cf' ),;

    /** RGB color of this kind. */
    public final int colorNum;
    /** Symbol representing this kind. */
    public final String symbolStr;
    /** Symbol with title of this kind (title itself is returned by method toString()). */
    public final String titleWithSymbolStr;

    private static final String CLASS_TEMPLATE
            = "<span style=\"background-color: #%s; color: black\">&nbsp;<code><b>%c</b></code>&nbsp;</span>";
    private static final String MEMBER_TEMPLATE = "<span color=%s>&nbsp;<code>%c</code> </span>";
    private static final char CLASS_INDICATOR = '\0';

    private ElementKind(int color) {
        this( color, CLASS_INDICATOR );
    }

    private ElementKind(int color, char symbol) {
        this.colorNum = color;
        String template;
        if (symbol == CLASS_INDICATOR) {
            symbol = name().charAt( 0 );
            template = CLASS_TEMPLATE;
        } else {
            template = MEMBER_TEMPLATE;
        }
        this.symbolStr = String.format( template, Utils.colorAsRRGGBB( color ), symbol );
        this.titleWithSymbolStr = symbolStr + name();
    }
}
