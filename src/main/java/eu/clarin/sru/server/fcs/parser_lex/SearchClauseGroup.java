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
public class SearchClauseGroup extends QueryNode {
    private final QueryNode leftChild;
    private final RBoolean r_boolean;
    private final QueryNode rightChild;

    SearchClauseGroup(QueryNode leftChild, RBoolean r_boolean, QueryNode rightChild) {
        super(QueryNodeType.SEARCH_CLAUSE_GROUP);
        this.leftChild = leftChild;
        this.r_boolean = r_boolean;
        this.rightChild = rightChild;
    }

    /**
     * Get the left child (search clause or group).
     *
     * @return the left child (search clause or group)
     */
    public QueryNode getLeftChild() {
        return leftChild;
    }

    /**
     * Get the right child (search clause or group).
     *
     * @return the right child (search clause or group)
     */
    public QueryNode getRightChild() {
        return rightChild;
    }

    /**
     * Get the right child (search clause or group).
     *
     * @return the right child (search clause or group)
     */
    public RBoolean getBoolean() {
        return r_boolean;
    }

    /**
     * Check if expression used a given boolean.
     *
     * @param r_boolean the boolean to check
     * @return <code>true</code> if the given boolean was used,
     *         <code>false</code> otherwise
     */
    public boolean hasBoolean(RBoolean r_boolean) {
        if (r_boolean == null) {
            throw new NullPointerException("r_boolean == null");
        }
        return this.r_boolean == r_boolean;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(nodeType.toDisplayString());
        sb.append(" ");
        sb.append(leftChild).append(" ").append(r_boolean).append(" ").append(rightChild);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

} // class SearchClauseGroup
