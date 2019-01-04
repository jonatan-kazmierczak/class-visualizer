package clsvis.model;

import clsvis.Utils;
import clsvis.gui.ColorContext;
import java.beans.ConstructorProperties;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Top level class representing language element.<br/>
 * This class is language-independent equivalent of {@link javax.lang.model.element.Element}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public abstract class LangElement implements Comparable<LangElement> {

    public final String id;
    public final String name;
    //public Class type;
    //public final Type genericType;
    public final String fullTypeName;
    public final String shortTypeName;
    public final String originalTypeName;
    //public final boolean typeIsLoadable;
    public final ElementKind kind;
    public final ElementVisibility visibility;
    public final Collection<ElementModifier> modifiers;

    @ConstructorProperties( { "id", "name", "type", "genericType", "modifiers", "kind", "visibility" } )
    public LangElement(String id, String name, Class type, Type genericType,
            Collection<ElementModifier> modifiers, ElementKind kind, ElementVisibility visibility) {
        this.id = id;
        this.name = name;
        //this.type = type;
        //this.genericType = genericType;
        this.kind = kind;
        this.visibility = visibility;
        this.modifiers = modifiers.isEmpty() ? Collections.EMPTY_SET : EnumSet.copyOf( modifiers );
        Class originalType = Utils.getClassType( type );
        this.originalTypeName = originalType != null ? type.getName() : null;
        //this.typeIsLoadable = Utils.getClassType(type) != null;
        String fullTypeName = (genericType instanceof Class) ? type.getCanonicalName() : genericType.toString();
        if (fullTypeName == null) {
            fullTypeName = type.getName(); // anonymous class
        }
        this.fullTypeName = fullTypeName;
        //this.fullTypeName = fullTypeName.replace(".", "::");

        String shortTypeName = (genericType instanceof Class) ? type.getSimpleName() : this.fullTypeName;
        if (shortTypeName == null || shortTypeName.length() == 0) {
            shortTypeName = this.fullTypeName; // anonymous class
        }		// for generics and anonymous: remove all package paths
        this.shortTypeName = shortTypeName.replaceAll( "\\w+[\\.\\$](?!\\d+)", "" );
    }

    public boolean isAbstract() {
        return modifiers.contains( ElementModifier.Abstract );
    }

    public boolean isStatic() {
        return modifiers.contains( ElementModifier.Static );
    }

    public String getStereotypesAsString(String separator, boolean withoutAbstractAndStatic) {
        if (separator == null) {
            separator = "";
        }
        StringBuilder sb = new StringBuilder( 0x80 );

        for (ElementModifier modifier : modifiers) {
            if (ElementModifier.visibilityModifiers.contains( modifier )
                    || modifier == ElementModifier.Abstract && withoutAbstractAndStatic
                    || modifier == ElementModifier.Static && withoutAbstractAndStatic) {
                continue;
            }
            sb.append( "<span color=" ).append( ColorContext.UmlStereotype.colorStr ).append( ">\u00ab" )
                    .append( modifier ).append( "\u00bb</span>" );
            sb.append( separator );
        }
        return sb.toString();
    }

    /**
     * Unique signature - name for most cases, overridden by {@link Operation}.
     */
    public String getSignature() {
        return name;
    }

    /**
     * Suffix added to the end of representation on the graph - overridden by {@link Constant}.
     */
    public String getDeclarationSuffix() {
        return "";
    }

    @Override
    public String toString() {
        //TODO: deal with arrays!
        return String.format( "<html>%s%s%s%s %s : <span color=%s>%s</span>%s%s%s %s",
                kind.symbolStr,
                isStatic() ? "<u>" : "",
                isAbstract() ? "<i>" : "",
                visibility.symbolStr,
                name,
                ColorContext.UmlType.colorStr,
                fullTypeName.replace( "<", "&lt;" ),
                getDeclarationSuffix(),
                isAbstract() ? "</i>" : "",
                isStatic() ? "</u>" : "",
                getStereotypesAsString( " ", false )
        );
    }

    @Override
    public boolean equals(Object other) {
        return id.equals( (other != null && other instanceof LangElement) ? ((LangElement) other).id : null );
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(LangElement other) {
        int result = name.compareTo( other.name );
        if (result == 0) {
            result = id.compareTo( other.id );
        }
        return result;
    }
}
