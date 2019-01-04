package clsvis.model;

import clsvis.gui.ColorContext;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents class, interface, enum.<br/>
 * This class is language-independent equivalent of {@link javax.lang.model.element.TypeElement}.<br/>
 * For instances of this class; id == fullTypeName and name == shortTypeName.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class Class_ extends ParameterizableElement {

    private static final Set<RelationType> CHECKABLE_RELATIONS
            = EnumSet.of( RelationType.InnerClass, RelationType.Association, RelationType.Dependency );
    private static final Set<RelationType> IMMUTABLE_RELATIONS
            = EnumSet.of( RelationType.SuperClass, RelationType.SuperInterface, RelationType.InnerClass );
    private static final Set<ElementKind> MEMBER_KINDS_EXTENDED = EnumSet.of(
            ElementKind.Constants, ElementKind.Fields, ElementKind.Properties,
            ElementKind.Constructors, ElementKind.Methods,
            ElementKind.Extends, ElementKind.Implements );

    private final String typeParametersStr;

    public final Map<ElementKind, List<ParameterizableElement>> membersMap = new EnumMap<>( ElementKind.class );
    public final Map<RelationDirection, Map<RelationType, Collection<Class_>>> relationsMap
            = new EnumMap<>( RelationDirection.class );
    public final Map<RelationDirection, Collection<Class_>> relationsToCheck = new EnumMap<>( RelationDirection.class );

    /** Indicates, if class was fully processed - with all its relations. */
    public boolean relationsProcessed;

    public Class_(String id, String name, Class clazz, Collection<ElementModifier> modifiers,
            ElementKind kind, ElementVisibility visibility) {
        super( id, name, clazz, clazz, modifiers, kind, visibility );

        TypeVariable[] typeParams = clazz.getTypeParameters();
        if (typeParams.length > 0) {
            StringBuilder sb = new StringBuilder( 0x100 );
            sb.append( "&lt;" );
            for (TypeVariable tvar : typeParams) {
                sb.append( tvar );
                Type varBound = tvar.getBounds()[ 0 ];
                String varType = varBound instanceof Class ? ((Class) varBound).getName() : varBound.toString();
                if (!varBound.equals( Object.class )) {
                    sb.append( " extends " ).append( varType );
                }
                sb.append( ", " );
            }
            sb.setLength( sb.length() - 2 );
            sb.append( '>' );
            typeParametersStr = sb.toString();
        } else {
            typeParametersStr = "";
        }

        // Init relationsMap, relationsToCheck
        for (RelationDirection relationDirection : RelationDirection.values()) {
            EnumMap<RelationType, Collection<Class_>> relationsSideMap = new EnumMap<>( RelationType.class );
            for (RelationType relationType : RelationType.values()) {
                relationsSideMap.put( relationType, new TreeSet<>() );
            }
            relationsMap.put( relationDirection, relationsSideMap );
            relationsToCheck.put( relationDirection, new HashSet<>() );
        }
    }

    public void addMember(ParameterizableElement element) {
        ElementKind memberKind = element.kind;
        List<ParameterizableElement> members = membersMap.get( memberKind );
        if (members == null) {
            members = new LinkedList<>();
            membersMap.put( memberKind, members );
        }
        members.add( element );
    }

    public Collection<ParameterizableElement> getMembers(ElementKind elementKind) {
        return membersMap.get( elementKind );
    }

    public void addRelation(RelationType relType, Class_ class_) {
        if (class_ != this) {
            addRelation( relType, class_, RelationDirection.Outbound );
            class_.addRelation( relType, this, RelationDirection.Inbound );
        }
    }

    public void addSuperInterface(Class_ class_) {
        addRelation( RelationType.SuperInterface, class_, RelationDirection.Outbound );
        class_.addRelation(
                modifiers.contains( ElementModifier.Interface ) ? RelationType.SuperInterface : RelationType.SuperClass,
                this, RelationDirection.Inbound );
    }

    private void addRelation(RelationType relType, Class_ class_, RelationDirection relDirection) {
        // Checking, if relation already exists
        if (CHECKABLE_RELATIONS.contains( relType ) && !relationsToCheck.get( relDirection ).add( class_ )) {
            return;
        }
        relationsMap.get( relDirection ).get( relType ).add( class_ );
    }

    public Collection<Class_> getRelations(RelationType relType, RelationDirection relDirection) {
        return relationsMap.get( relDirection ).get( relType );
    }

    /**
     * Has to be invoked after object is fully initialized and set.
     * It does some post-initialization.
     */
    public void membersFinished() {
        // members
        for (ElementKind elementKind : MEMBER_KINDS_EXTENDED) {
            List<ParameterizableElement> memberList = membersMap.get( elementKind );
            if (memberList != null) {
                Collections.sort( memberList );
            } else {
                membersMap.put( elementKind, Collections.EMPTY_LIST );
            }
        }
        // super class, super ifaces, inner classes
        for (RelationType relType : IMMUTABLE_RELATIONS) {
            Collection<Class_> classesSet = relationsMap.get( RelationDirection.Outbound ).get( relType );
            relationsMap.get( RelationDirection.Outbound ).put( relType,
                    classesSet.isEmpty() ? Collections.EMPTY_LIST : new ArrayList<>( classesSet ) );
        }
    }

    /**
     * Has to be invoked after relations are set.
     */
    public void relationsFinished() {
        // mark full setup
        relationsProcessed = true;
        // outer class
        Collection<Class_> outerClassesSet = relationsMap.get( RelationDirection.Inbound ).get( RelationType.InnerClass );
        relationsMap.get( RelationDirection.Inbound ).put( RelationType.InnerClass,
                outerClassesSet.isEmpty() ? Collections.EMPTY_LIST : new ArrayList<>( outerClassesSet ) );
        // outbound relations
        for (RelationType relType : RelationType.values()) {
            if (relationsMap.get( RelationDirection.Outbound ).get( relType ).isEmpty()) {
                relationsMap.get( RelationDirection.Outbound ).put( relType, Collections.EMPTY_LIST );
            }
        }
        // TODO: process inbounds according to final
    }

    public String getNamespaceUml() {
        String nameSpace = getFullNameUml();
        int namespaceBreakIdx = nameSpace.lastIndexOf( "::" );
        return (namespaceBreakIdx < 0) ? null : nameSpace.substring( 0, namespaceBreakIdx + 2 );
    }

    public String getFullNameUml() {
        return fullTypeName.replaceAll( "\\.", "::" );
    }

    /**
     * Returns short name following by type parameters (if exist).
     */
    public String getShortNameWithParams() {
        return getWithParams( name ).replaceAll( "\\w+[\\.\\$](?!\\d+)", "" );
    }

    public String getFullNameWithParams() {
        return getWithParams( id );
    }

    /**
     * Returns prefix following by type parameters (if exist).
     */
    private String getWithParams(String prefix) {
        return typeParametersStr.length() == 0 ? prefix : prefix + typeParametersStr;
    }

    @Override
    public String toString() {
        //return toStringWithNames(name, id);
        return toStringWithTypeParameters();
    }

    /**
     * Returns string representation with type parameters.
     */
    public String toStringWithTypeParameters() {
        return toStringWithNames(
                getShortNameWithParams().replace( "<", "&lt;" ),
                getWithParams( id ).replace( "<", "&lt;" ) );
    }

    /**
     * Returns HTML string with 2 parameters: short name and long name.
     */
    private String toStringWithNames(String nameParam, String fullNameParam) {
        return String.format(
                "<html><table cellspacing=0 cellpadding=0><tr>"
                + "<td style=\"border-style: solid; border-width: 1; border-color: black;\">%s"
                + "<td><span %s>&nbsp;%s%s%s <b>%s</b>%s%s</span> (%s) %s"
                + "</table>",
                kind.symbolStr,
                relationsProcessed ? "" : "color=#" + ColorContext.ClassUnprocessed.colorStr,
                isStatic() ? "<u>" : "",
                isAbstract() ? "<i>" : "",
                visibility.symbolStr,
                nameParam,
                isAbstract() ? "</i>" : "",
                isStatic() ? "</u>" : "",
                fullNameParam,
                getStereotypesAsString( " ", false )
        );
    }

}
