/**
 * This software is copyright (c) 2013-2025 by
 *  - Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.parser_lex;

/**
 * A LexCQL expression tree search_clausse_group node.
 */
public class Subquery extends QueryNode {
    private final QueryNode child;
    private final boolean inParentheses;

    Subquery(QueryNode child, boolean inParentheses) {
        super(QueryNodeType.SUBQUERY);
        this.child = child;
        this.inParentheses = inParentheses;
    }

    /**
     * Get the inner child.
     *
     * @return the right child
     */
    public QueryNode getChild() {
        return child;
    }

    /**
     * Is this query node in parentheses.
     *
     * @return <code>true</code> if this query node is in parentheses,
     *         <code>false</code> otherwise
     */
    public boolean isInParantheses() {
        return inParentheses;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(nodeType.toDisplayString());
        sb.append(' ');
        if (inParentheses) {
            sb.append("\"(\" ");
        }
        sb.append(child);
        if (inParentheses) {
            sb.append(" \")\"");
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

} // class Subquery
