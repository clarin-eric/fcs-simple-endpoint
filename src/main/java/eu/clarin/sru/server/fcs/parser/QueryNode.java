/**
 * This software is copyright (c) 2013-2016 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.parser;

import java.util.Collections;
import java.util.List;


/**
 * base class for FCS-QL expression tree nodes.
 *
 */
public abstract class QueryNode {
    protected final QueryNodeType nodeType;
    protected final List<QueryNode> children;
    protected QueryNode parent;


    /**
     * Constructor.
     *
     * @param nodeType
     *            the type of the node
     * @param children
     *            the children of this node or <code>null</code> if none
     */
    protected QueryNode(QueryNodeType nodeType, List<QueryNode> children) {
        this.nodeType = nodeType;
        if ((children != null) && !children.isEmpty()) {
            for (QueryNode child : children) {
                child.setParent(this);
            }
            this.children = Collections.unmodifiableList(children);
        } else {
            this.children = Collections.emptyList();
        }
    }


    /**
     * Constructor.
     *
     * @param nodeType
     *            the type of the node
     * @param child
     *            the child of this node or <code>null</code> if none
     */
    protected QueryNode(QueryNodeType nodeType, QueryNode child) {
        this.nodeType = nodeType;
        if (child != null) {
            child.setParent(this);
            this.children = Collections.singletonList(child);
        } else {
            this.children = Collections.emptyList();
        }
    }


    /**
     * Constructor.
     *
     * @param nodeType
     *            the type of the node
     */
    protected QueryNode(QueryNodeType nodeType) {
        this.nodeType = nodeType;
        this.children = Collections.emptyList();
    }


    /**
     * Get the node type of this node.
     *
     * @return the node type
     */
    public QueryNodeType getNodeType() {
        return nodeType;
    }


    /**
     * Check, if node if of given type.
     * 
     * @param nodeType
     *            type to check against
     * @return <code>true</code> if node is of given type, <code>false</code>
     *         otherwise
     */
    public boolean hasNodeType(QueryNodeType nodeType) {
        if (nodeType == null) {
            throw new NullPointerException("nodeType == null");
        }
        return this.nodeType == nodeType;
    }


    /**
     * Get the parent node of this node.
     *
     * @return the parent node or <code>null</code> if this is the root node
     */
    public QueryNode getParent() {
        return parent;
    }


    /**
     * The children of this node.
     *
     * @return the list of children of this node
     */
    public List<QueryNode> getChildren() {
        return children;
    }


    /**
     * Get the number of children of this node.
     *
     * @return the number of children of this node
     */
    public int getChildCount() {
        final List<QueryNode> children = getChildren();
        return (children != null) ? children.size() : 0;
    }


    /**
     * Get a child node by index.
     *
     * @param idx
     *            the index of the child node
     * @return the child node of this node or <code>null</code> if index is out
     *         of bounds
     */
    public QueryNode getChild(int idx) {
        final List<QueryNode> children = getChildren();
        if ((children != null) && !children.isEmpty()) {
            if ((idx >= 0) && (idx < children.size())) {
                return children.get(idx);
            }
        }
        return null;
    }


    /**
     * Get this first child node.
     *
     * @return the first child node of this node or <code>null</code> if none
     */
    public QueryNode getFirstChild() {
        return getChild(0);
    }


    /**
     * Get this last child node.
     *
     * @return the last child node of this node or <code>null</code> if none
     */
    public QueryNode getLastChild() {
        return getChild(getChildCount() - 1);
    }


    /**
     * Get a child node of specified type by index. Only child nodes of the
     * requested type are counted.
     *
     * @param <T>
     *            the class of the nodes to be considered
     * @param clazz
     *            the type to nodes to be considered
     * @param idx
     *            the index of the child node
     * @return the child node of this node or <code>null</code> if no child was
     *         found
     */
    public <T extends QueryNode> T getChild(Class<T> clazz, int idx) {
        final List<QueryNode> children = getChildren();
        if (!children.isEmpty()) {
            int pos = 0;
            for (QueryNode child : children) {
                if (clazz.isInstance(child)) {
                    if (pos == idx) {
                        return clazz.cast(child);
                    }
                    pos++;
                }
            }
        }
        return null;
    }


    /**
     * Get a first child node of specified type.
     *
     * @param <T>
     *            the class of the nodes to be considered
     * @param clazz
     *            the type to nodes to be considered
     * @return the child node of this node or <code>null</code> if no child was
     *         found
     */
    public <T extends QueryNode> T getFirstChild(Class<T> clazz) {
            return getChild(clazz, 0);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(nodeType.toDisplayString());
        if (!children.isEmpty()) {
            for (QueryNode child : children) {
                sb.append(" ").append(child);
            }
        }
        sb.append(")");
        return sb.toString();
    }


    public abstract void accept(QueryVisitor visitor);


    protected final void setParent(QueryNode parent) {
        this.parent = parent;
    }

} // abstract class QueryNode
