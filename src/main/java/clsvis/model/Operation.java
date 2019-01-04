package clsvis.model;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * Represents class operation.<br/>
 * This class is language-independent similarity to {@link javax.lang.model.element.ExecutableElement}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class Operation extends ParameterizableElement {

    public final List<ParameterizableElement> parameters;
    public final List<ParameterizableElement> throwables;

    public Operation(String id, String name, Class type, Type genericType,
            Collection<ElementModifier> modifiers, ElementKind kind, ElementVisibility visibility,
            List<ParameterizableElement> parameters, List<ParameterizableElement> throwables) {
        super( id, name, type, genericType, modifiers, kind, visibility );
        this.parameters = parameters;
        this.throwables = throwables;
    }

    public String getParametersAsString() {
        if (parameters.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder( 0x100 );
        for (ParameterizableElement parameter : parameters) {
            if (sb.length() > 0) {
                sb.append( ", " );
            }
            //sb.append( String.format("%s : %s", parameter.name, parameter.shortTypeName) );
            sb.append( parameter.shortTypeName );
        }
        return sb.toString();
    }

    @Override
    public String getSignature() {
        return name + "( " + getParametersAsString() + " )";
    }
}
