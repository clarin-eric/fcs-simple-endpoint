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


/**
 * A FCS-QL expression tree GROUP expression node.
 */
public class ExpressionGroup extends QueryNode {

    /**
     * Constructor.
     *
     * @param content
     *            the group content
     */
    ExpressionGroup(QueryNode content) {
        super(QueryNodeType.EXPRESSION_GROUP, content);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(")
            .append(nodeType.toDisplayString())
            .append(children.get(0))
            .append(")");
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

} // class ExpressionGroup
