package clsvis.gui;

import clsvis.Utils;

/**
 * Contains definitions of colors for given graphical elements.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public enum ColorContext {
    UmlClassName( 0xFF0000 ),
    UmlSectionTitle( 0x008000 ),
    UmlType( 0x000080 ),
    UmlParams( 0x008080 ),
    UmlStereotype( 0x808000 ),
    ClassProcessed( 0x000000 ),
    ClassUnprocessed( 0x585858 );

    public final int colorInt;
    public final String colorStr;

    private ColorContext(int color) {
        this.colorInt = color;
        this.colorStr = Utils.colorAsRRGGBB( color );
    }
}
