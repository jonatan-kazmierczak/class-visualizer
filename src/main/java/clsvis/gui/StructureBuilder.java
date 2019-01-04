package clsvis.gui;

import clsvis.gui.model.ClassPresentationWrapper;
import clsvis.model.RelationType;
import clsvis.model.Annotation_;
import clsvis.model.Class_;
import clsvis.model.ElementKind;
import clsvis.model.LangElement;
import clsvis.model.Operation;
import clsvis.model.ParameterizableElement;
import clsvis.model.RelationDirection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Builds various graphical structures: trees, table.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public abstract class StructureBuilder {

    // Class Hierarchy Tree
    
    public static ClassPresentationWrapper buildClassesTreeNode2(Class_ class_, ClassPresentationWrapper parent) {
        ClassPresentationWrapper classNode = new ClassPresentationWrapper( class_, null );
        Collection<Class_> subClasses = class_.getRelations( RelationType.SuperClass, RelationDirection.Inbound );
        if (subClasses.isEmpty()) {
            classNode.children = Collections.EMPTY_LIST;
        } else {
            int subtreeCount = 0;
            ArrayList<ClassPresentationWrapper> subClassNodes = new ArrayList<>( subClasses.size() );
            for (Class_ subClass : subClasses) {
                ClassPresentationWrapper subNode = buildClassesTreeNode2( subClass, classNode );
                subClassNodes.add( subNode );
                subtreeCount += subNode.subtreeClassesCount + 1;
            }
            classNode.children = subClassNodes;
            classNode.subtreeClassesCount += subtreeCount;
        }
        return classNode;
    }

    
    // Class Members Tree
    
    private static final String MEMBERS_TREE_NODE_SUBTITLE_PREFIX = "<html>";

    private static final Object[][] relationsToProcess = {
        { RelationType.SuperClass, RelationDirection.Outbound, "Generalization (SuperClass)" },
        { RelationType.SuperInterface, RelationDirection.Outbound, "Abstractions (SuperInterfaces)" },
        { RelationType.SuperInterface, RelationDirection.Inbound, "Specializations (SubInterfaces)" },
        { RelationType.SuperClass, RelationDirection.Inbound, "Specializations/Realizations (SubClasses)" },
        { RelationType.InnerClass, RelationDirection.Outbound, "Nestings (Inner Classes)" },
        { RelationType.Association, RelationDirection.Outbound, "Associations (Uses)" },
        { RelationType.Dependency, RelationDirection.Outbound, "Dependencies (Uses)" },
        { RelationType.DependencyAnnotation, RelationDirection.Outbound, "Dependencies (Annotations)" },
        { RelationType.DependencyThrows, RelationDirection.Outbound, "Dependencies (Throws)" },
        { RelationType.InnerClass, RelationDirection.Inbound, "Nesting Owner (Outer Class)" },
        { RelationType.Association, RelationDirection.Inbound, "Association Usages (Used By)" },
        { RelationType.Dependency, RelationDirection.Inbound, "Dependency Usages (Used By)" },
        { RelationType.DependencyAnnotation, RelationDirection.Inbound, "Dependency Usages (Annotated)" },
        { RelationType.DependencyThrows, RelationDirection.Inbound, "Dependency Usages (Thrown By)" }, };

    public static DefaultMutableTreeNode buildMembersTreeNode(Class_ class_) {
        DefaultMutableTreeNode classNode = new DefaultMutableTreeNode( class_ );
        DefaultMutableTreeNode contentNode = new DefaultMutableTreeNode( "<html>&ni; Content" );
        buildElementTreeNode( class_.annotations,
                MEMBERS_TREE_NODE_SUBTITLE_PREFIX + ElementKind.Annotations.titleWithSymbolStr, contentNode, false );
        for (Entry<ElementKind, List<ParameterizableElement>> entry : class_.membersMap.entrySet()) {
            buildElementTreeNode( entry.getValue(),
                    MEMBERS_TREE_NODE_SUBTITLE_PREFIX + entry.getKey().titleWithSymbolStr, contentNode, true );
        }
        if (!contentNode.isLeaf()) {
            classNode.add( contentNode );
        }

        DefaultMutableTreeNode relationsNode = new DefaultMutableTreeNode( "<html>&harr; Relations" );
        for (Object[] nodeDef : relationsToProcess) {
            RelationType relationType = (RelationType) nodeDef[ 0 ];
            RelationDirection relationDirection = (RelationDirection) nodeDef[ 1 ];
            String title = (String) nodeDef[ 2 ];
            buildElementTreeNode( class_.getRelations( relationType, relationDirection ),
                    MEMBERS_TREE_NODE_SUBTITLE_PREFIX + relationType.asString.get( relationDirection ) + title,
                    relationsNode, false );
        }
        if (!relationsNode.isLeaf()) {
            classNode.add( relationsNode );
        }

        return classNode;
    }

    private static void buildElementTreeNode(
            Collection<? extends LangElement> elements, String title, DefaultMutableTreeNode parentNode,
            boolean withAnnotations) {
        if (elements.isEmpty()) {
            return;
        }

        DefaultMutableTreeNode elementsNode = new DefaultMutableTreeNode(
                title + " <span color=green>(" + elements.size() + ")</span>" );
        for (LangElement element : elements) {
            DefaultMutableTreeNode elementNode = new DefaultMutableTreeNode( element );
            if (withAnnotations && element instanceof ParameterizableElement) {
                ParameterizableElement parameterizableElement = (ParameterizableElement) element;
                buildElementTreeNode( parameterizableElement.annotations,
                        MEMBERS_TREE_NODE_SUBTITLE_PREFIX + ElementKind.Annotations.titleWithSymbolStr, elementNode, false );
            }
            if (element instanceof Operation) {
                Operation operation = (Operation) element;
                buildElementTreeNode( operation.parameters,
                        MEMBERS_TREE_NODE_SUBTITLE_PREFIX + ElementKind.Parameters.titleWithSymbolStr, elementNode, true );
                buildElementTreeNode( operation.throwables,
                        MEMBERS_TREE_NODE_SUBTITLE_PREFIX + ElementKind.Throws.titleWithSymbolStr, elementNode, true );
            }
            elementsNode.add( elementNode );
        }
        parentNode.add( elementsNode );
    }

    
    // Class UML Table
    
    public static String buildClassUMLTable(Class_ class_) {
        String namespace = class_.getNamespaceUml();
        // Existing parents
        Collection<Class_> parents = class_.relationsMap.get( RelationDirection.Outbound ).get( RelationType.SuperClass );
        if (parents.isEmpty()) {
            parents = class_.relationsMap.get( RelationDirection.Outbound ).get( RelationType.SuperInterface );
        }
        StringBuilder parentsSB = new StringBuilder( 0x80 );
        for (Class_ parent : parents) {
            parentsSB.append(
                    String.format( "<div align=left>%s%s<b>%s</b>%s%s<div>",
                            parent.isStatic() ? "<u>" : "",
                            parent.isAbstract() ? "<i>" : "",
                            parent.shortTypeName,
                            parent.isAbstract() ? "</i>" : "",
                            parent.isStatic() ? "</u>" : "" ) );
        }
        if (parentsSB.length() > 0) {
            parentsSB.append( "<br>" );
        }
        return "<html>"
                + "<head><style>"
                + "table {border-style: solid; border-width: 1; border-color: black} "
                + "td {font: 12pt SansSerif; border-style: solid; border-width: 1; border-color: black} "
                + "</style></head>"
                + "<body>"
                + "<table align=center cellspacing=0 border=1>"
                + "<tr><td nowrap>" + parentsSB
                + "<div align=center>"
                + buildAnnotationsUMLTable( class_.annotations, "<br>", ColorContext.UmlStereotype.colorStr )
                + class_.getStereotypesAsString( "<br>", true )
                + (namespace != null ? namespace + "<br>" : "")
                + String.format( "%s%s<b><span color=%s>%s</span></b>%s%s",
                        class_.isStatic() ? "<u>" : "",
                        class_.isAbstract() ? "<i>" : "",
                        ColorContext.UmlClassName.colorStr,
                        class_.getShortNameWithParams().replaceAll( "<", "&lt;" ),
                        class_.isAbstract() ? "</i>" : "",
                        class_.isStatic() ? "</u>" : ""
                )
                + "</div>"
                + //"</td></tr>"
                buildMembersUMLTable( class_, ElementKind.Constants )
                + buildMembersUMLTable( class_, ElementKind.Fields )
                + buildMembersUMLTable( class_, ElementKind.Properties )
                + buildMembersUMLTable( class_, ElementKind.Constructors )
                + buildMembersUMLTable( class_, ElementKind.Methods )
                + "</body>"
                + "</html>";
    }

    private static String buildMembersUMLTable(Class_ class_, ElementKind elementKind) {
        Collection<? extends ParameterizableElement> elements = class_.membersMap.get( elementKind );
        if (elements.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder( 0x2000 );
        sb.append( String.format(
                "<tr><td nowrap><div align=center color=%s>%s</div>", ColorContext.UmlSectionTitle.colorStr, elementKind ) );
        for (ParameterizableElement element : elements) {
            String annotationsStr = buildAnnotationsUMLTable( element.annotations, " ", ColorContext.UmlStereotype.colorStr );
            String stereotypesStr = element.getStereotypesAsString( " ", true );
            if (stereotypesStr.length() > 0 || annotationsStr.length() > 0) {
                sb.append( "&nbsp;&nbsp;&nbsp;" ).append( stereotypesStr ).append( annotationsStr ).append( "<br>" );
            }
            sb.append( String.format( "%s%s%s %s%s : <span color=%s>%s</span>%s%s%s<br>\n",
                    element.isStatic() ? "<u>" : "",
                    element.isAbstract() ? "<i>" : "",
                    element.visibility.symbolStr,
                    element.name.replace( "<", "&lt;" ),
                    (element instanceof Operation)
                            ? String.format( "( <span color=%s>%s</span> )",
                                    ColorContext.UmlParams.colorStr,
                                    ((Operation) element).getParametersAsString().replace( "<", "&lt;" ) )
                            : "",
                    ColorContext.UmlType.colorStr,
                    element.shortTypeName.replace( "<", "&lt;" ),
                    element.getDeclarationSuffix(),
                    element.isAbstract() ? "</i>" : "",
                    element.isStatic() ? "</u>" : ""
            ) );
        }
        return sb.toString();
    }

    private static String buildAnnotationsUMLTable(
            Collection<Annotation_> annotations, String separator, String annotationColor) {
        StringBuilder sb = new StringBuilder( 0x100 );
        for (Annotation_ annotation : annotations) {
            sb.append( String.format( "<span color=%s>\u00ab%s\u00bb</span>", annotationColor, annotation.name ) );
            sb.append( separator );
        }
        return sb.toString();
    }

    private static final Set<ElementKind> memberKinds = EnumSet.of(
            ElementKind.Constants, ElementKind.Fields, ElementKind.Properties,
            ElementKind.Constructors, ElementKind.Methods );

    public static String buildClassRelationsSummaryTable(Class_ class_) {
        int members = 0;
        for (ElementKind elementKind : memberKinds) {
            members += class_.getMembers( elementKind ).size();
        }
        int parents = class_.getRelations( RelationType.SuperClass, RelationDirection.Outbound ).size()
                + class_.getRelations( RelationType.SuperInterface, RelationDirection.Outbound ).size();
        int usedBy = class_.relationsToCheck.get( RelationDirection.Inbound ).size()
                + class_.getRelations( RelationType.DependencyThrows, RelationDirection.Inbound ).size()
                + class_.getRelations( RelationType.DependencyAnnotation, RelationDirection.Inbound ).size();
        int uses = class_.relationsToCheck.get( RelationDirection.Outbound ).size()
                + class_.getRelations( RelationType.DependencyThrows, RelationDirection.Outbound ).size()
                + class_.getRelations( RelationType.DependencyAnnotation, RelationDirection.Outbound ).size();
        int children = class_.getRelations( RelationType.SuperClass, RelationDirection.Inbound ).size()
                + class_.getRelations( RelationType.SuperInterface, RelationDirection.Inbound ).size();
        return String.format(
                "<html>"
                + "<head><style>"
                + "td {text-align: center} "
                + "</style></head>"
                + "<body>"
                + "%s<br>"
                + "<table cellspacing=4>"
                + "<tr><td>Members:<th>%s"
                + "<tr><td>Relations:<td>&uarr;<td>&darr;<td>&larr;<td>&rarr;"
                + "<tr><td>&nbsp;<th>%s<th>%s<th>%s<th>%s",
                class_.getFullNameUml(),
                formatRelationsCount( members ),
                formatRelationsCount( parents ),
                formatRelationsCount( children ),
                formatRelationsCount( usedBy ),
                formatRelationsCount( uses ) );
    }

    private static String formatRelationsCount(int v) {
        return v > 0 ? Integer.toString( v ) : " ";
    }
}
