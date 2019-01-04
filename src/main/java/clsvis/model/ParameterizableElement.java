package clsvis.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Language element which can be parameterized by annotations or generic types.
 * It can be class, attribute, operation.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class ParameterizableElement extends LangElement {

    public Collection<Annotation_> annotations = new ArrayList<>( 2 );
    public Collection<String> typeParameters = new ArrayList<>( 2 );

    public ParameterizableElement(String id, String name, Class type, Type genericType,
            Collection<ElementModifier> modifiers, ElementKind kind, ElementVisibility visibility) {
        super( id, name, type, genericType, modifiers, kind, visibility );
    }
}
