package clsvis.model;

import java.util.Collections;

/**
 * Represents annotation.<br/>
 * This is simplified equivalent of {@link javax.lang.model.element.AnnotationMirror}.<br/>
 * Currently only string representation of annotation is kept.
 * This poor solution is caused by poor annotation API, not offering uniform access to parameters.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class Annotation_ extends LangElement {

    private static final String REPLACEMENT = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";

    public Annotation_(String id, String name, Class type) {
        super( id, name, type, type, Collections.EMPTY_LIST, ElementKind.Annotations, ElementVisibility.Local );
    }

    public String getShortName() {
        return id;
    }

    @Override
    public String toString() {
        return "<html>" + kind.symbolStr + id;
    }
}
