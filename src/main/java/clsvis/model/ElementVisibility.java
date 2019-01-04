package clsvis.model;

/**
 * Contains possible visibilities of elements.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public enum ElementVisibility {
    Public( '+' ),
    Protected( '#' ),
    Package( '~' ),
    Private( '-' ),
    Local( '\0' ),;

    public final String symbolStr;

    private ElementVisibility(char symbol) {
        if (symbol == '\0') {
            symbolStr = ""; // special symbol - ignore it
        } else {
            symbolStr = String.format( "<code>%c</code> ", symbol );
        }
    }
}
