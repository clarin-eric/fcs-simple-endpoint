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
 * A FCS-QL expression tree query segment node.
 */
public class QuerySegment extends QueryNode {
    private final int minOccurs;
    private final int maxOccurs;


    /**
     * Constructor.
     *
     * @param expression
     *            the expression
     * @param minOccurs
     *            the minimum occurrence
     * @param maxOccurs
     *            the maximum occurrence
     */
    QuerySegment(QueryNode expression, int minOccurs, int maxOccurs) {
        super(QueryNodeType.QUERY_SEGMENT, expression);
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }


    /**
     * Get the expression for this segment.
     *
     * @return the expression
     */
    public QueryNode getExpression() {
        return children.get(0);
    }


    /**
     * Get the minimum occurrence of this segment.
     *
     * @return the minimum occurrence
     *
     */
    public int getMinOccurs() {
        return minOccurs;
    }


    /**
     * Get the maximum occurrence of this segment.
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
        sb.append("(");
        sb.append(nodeType.toDisplayString());
        sb.append(" ");
        if (minOccurs != 1) {
            sb.append("@min=");
            if (minOccurs == QueryNode.OCCURS_UNBOUNDED) {
                sb.append("*");
            } else {
                sb.append(minOccurs);
            }
            sb.append(" ");
        }
        if (maxOccurs != 1) {
            sb.append("@max=");
            if (maxOccurs == QueryNode.OCCURS_UNBOUNDED) {
                sb.append("*");
            } else {
                sb.append(maxOccurs);
            }
            sb.append(" ");

        }
        sb.append(children.get(0));
        sb.append(")");
        return sb.toString();
    }


    @Override
    public void accept(QueryVisitor visitor) {
        children.get(0).accept(visitor);
        visitor.visit(this);
    }

} // class QuerySegment
