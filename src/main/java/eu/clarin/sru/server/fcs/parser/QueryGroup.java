/**
 * This software is copyright (c) 2013-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.parser;


/**
 * A FCS-QL expression tree GROUP query node.
 */
public class QueryGroup extends QueryNode {
    private final int minOccurs;
    private final int maxOccurs;


    /**
     * Constructor.
     *
     * @param content
     *            the child
     * @param minOccurs
     *            the minimum occurrence
     * @param maxOccurs
     *            the maximum occurrence
     */
    QueryGroup(QueryNode content, int minOccurs, int maxOccurs) {
        super(QueryNodeType.QUERY_GROUP, content);
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }


    /**
     * Get the group content.
     *
     * @return the content of the GROUP query
     */
    public QueryNode getContent() {
        return children.get(0);
    }


    /**
     * Get the minimum occurrence of group content.
     *
     * @return the minimum occurrence
     *
     */
    public int getMinOccurs() {
        return minOccurs;
    }


    /**
     * Get the maximum occurrence of group content.
     *
     * @return the maximum occurrence
     *
     */
    public int getMaxOccurs() {
        return maxOccurs;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(')
            .append(nodeType.toDisplayString())
            .append(' ');
        if (minOccurs != 1) {
            sb.append("@min=");
            if (minOccurs == QueryNode.OCCURS_UNBOUNDED) {
                sb.append('*');
            } else {
                sb.append(minOccurs);
            }
            sb.append(' ');
        }
        if (maxOccurs != 1) {
            sb.append("@max=");
            if (maxOccurs == QueryNode.OCCURS_UNBOUNDED) {
                sb.append('*');
            } else {
                sb.append(maxOccurs);
            }
            sb.append(' ');

        }
        sb.append(children.get(0));
        sb.append(')');
        return sb.toString();
    }


    @Override
    public void accept(QueryVisitor visitor) {
        if (!children.isEmpty()) {
            for (QueryNode child : children) {
                child.accept(visitor);
            }
        }
        visitor.visit(this);
    }

} // class QueryGroup
