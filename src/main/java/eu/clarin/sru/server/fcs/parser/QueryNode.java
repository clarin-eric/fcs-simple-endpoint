package eu.clarin.sru.server.fcs.parser;

import java.util.Collections;
import java.util.List;

public abstract class QueryNode {
    protected final QueryNodeType nodeType;
    protected final List<QueryNode> children;
    protected QueryNode parent;


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


    protected QueryNode(QueryNodeType nodeType, QueryNode child) {
        this.nodeType = nodeType;
        if (child != null) {
            child.setParent(this);
            this.children = Collections.singletonList(child);
        } else {
            this.children = Collections.emptyList();
        }
    }


    protected QueryNode(QueryNodeType nodeType) {
        this.nodeType = nodeType;
        this.children = Collections.emptyList();
    }


    public QueryNodeType getNodeType() {
        return nodeType;
    }


    public QueryNode getParent() {
        return parent;
    }


    public List<QueryNode> getChildren() {
        return children;
    }


    public int getChildCount() {
        final List<QueryNode> children = getChildren();
        return (children != null) ? children.size() : 0;
    }


    public QueryNode getChild(int idx) {
        final List<QueryNode> children = getChildren();
        if ((children != null) && !children.isEmpty()) {
            if ((idx >= 0) && (idx < children.size())) {
                return children.get(idx);
            }
        }
        return null;
    }


    public QueryNode getFirstChild() {
        return getChild(0);
    }


    public QueryNode getLastChild() {
        return getChild(getChildCount() - 1);
    }


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


    protected final void visitAnyNode(QueryVisitor vistor, QueryNode node) {
        if (node instanceof QueryDisjunction) {
            vistor.visit((QueryDisjunction) node);
        } else if (node instanceof QueryGroup) {
            vistor.visit((QueryGroup) node);
        } else if (node instanceof QuerySegment) {
            vistor.visit((QuerySegment) node);
        } else if (node instanceof QuerySequence) {
            vistor.visit((QuerySequence) node);
        } else if (node instanceof ExpressionAnd) {
            vistor.visit((ExpressionAnd) node);
        } else if (node instanceof Expression) {
            vistor.visit((Expression) node);
        } else if (node instanceof ExpressionGroup) {
            vistor.visit((ExpressionGroup) node);
        } else if (node instanceof ExpressionNot) {
            vistor.visit((ExpressionNot) node);
        } else if (node instanceof ExpressionOr) {
            vistor.visit((ExpressionOr) node);
        } else if (node instanceof ExpressionWildcard) {
            vistor.visit((ExpressionWildcard) node);
        } else if (node instanceof SimpleWithin) {
            vistor.visit((SimpleWithin) node);
        } else {
            throw new RuntimeException("unexpected node type: "  +
                    node.getNodeType());
        }
    }

}
