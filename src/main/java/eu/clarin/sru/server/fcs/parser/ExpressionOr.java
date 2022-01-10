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

import java.util.List;


/**
 * A FCS-QL expression tree OR expression node.
 */
public class ExpressionOr extends QueryNode {

    /**
     * Constructor.
     *
     * @param children the children
     */
    ExpressionOr(List<QueryNode> children) {
        super(QueryNodeType.EXPRESSION_OR, children);
    }


    /**
     * Get the OR expression operands
     *
     * @return a list of expressions
     */
    public List<QueryNode> getOperands() {
        return children;
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

} // class ExpressionOr
