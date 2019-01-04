package clsvis.gui.model;

import clsvis.gui.ColorContext;
import clsvis.model.Class_;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 * Presentation wrapper for {@link Class_} object. It redefines method {@link Class_#toString()}. Used for simple
 * presentation on tree.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class ClassPresentationWrapper implements TreeNode {

    public final Class_ class_;
    public final ClassPresentationWrapper parent;
    public int subtreeClassesCount;
    public List<ClassPresentationWrapper> children;

    public ClassPresentationWrapper(Class_ class_, ClassPresentationWrapper parent) {
        this.class_ = class_;
        this.parent = parent;
    }

    @Override
    public String toString() {
        return String.format( "<html><span color=#%s>%s%s<b>%s</b>%s%s</span> (%s) %s",
                class_.relationsProcessed ? ColorContext.ClassProcessed.colorStr : ColorContext.ClassUnprocessed.colorStr,
                class_.isStatic() ? "<u>" : "",
                class_.isAbstract() ? "<i>" : "",
                class_.name,
                class_.isAbstract() ? "</i>" : "",
                class_.isStatic() ? "</u>" : "",
                class_.fullTypeName,
                subtreeClassesCount > 0 ? String.format( "<span color=green>(%d)</span>", subtreeClassesCount ) : "" );
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get( childIndex );
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf( node );
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public Enumeration<TreeNode> children() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClassPresentationWrapper other = (ClassPresentationWrapper) obj;
        return !(this.class_ != other.class_ && (this.class_ == null || !this.class_.equals( other.class_ )));
    }

    @Override
    public int hashCode() {
        return (this.class_ != null ? this.class_.hashCode() : 0);
    }
}
