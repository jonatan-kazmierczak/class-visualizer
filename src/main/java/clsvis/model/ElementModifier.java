package clsvis.model;

import java.beans.Introspector;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Constants representing modifiers of language element.<br/>
 * This class is improved equivalent of {@link javax.lang.model.element.Modifier}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 *
 * @see java.lang.reflect.Modifier
 */
public enum ElementModifier {
    Public,
    Protected,
    Private,
    Abstract,
    Static,
    Final,
    Transient,
    Volatile,
    Synchronized,
    Native,
    Strict, // TODO: should be represented as strictfp
    ReadOnly,
    //WriteOnly,
    Interface,
    Enum, // TODO: should be presented as "enumeration"
    Annotation,
    Record,
    Sealed,
    Throwable,
    LocalClass,
    MemberClass,
    Synthetic,
    Bridge,
    Default,
    Implicit,
    VarArgs,
    ;

    public static final Set<ElementModifier> visibilityModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Private, ElementModifier.Protected, ElementModifier.Public ) );

    private final String asString;

    private ElementModifier() {
        asString = Introspector.decapitalize( name() );
    }

    @Override
    public String toString() {
        return asString;
    }
}
