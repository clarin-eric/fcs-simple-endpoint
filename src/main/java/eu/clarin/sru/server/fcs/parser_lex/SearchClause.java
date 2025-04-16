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
 * A LexCQL expression tree search_clause node.
 */
public class SearchClause extends QueryNode {
    private final String index;
    private final Relation relation;
    private final String search_term;

    /**
     * Constructor.
     *
     * @param index       the index (or field) or <code>null</code> if none
     * @param relation    the relation or <code>null</code> if none
     * @param search_term the search term
     */
    SearchClause(String index, Relation relation, String search_term) {
        super(QueryNodeType.SEARCH_CLAUSE);
        if ((index != null) && !index.isEmpty()) {
            this.index = index;
        } else {
            this.index = null;
        }
        this.relation = relation;
        this.search_term = search_term;
    }

    /**
     * Get the index (or field).
     *
     * @return the index (or field) or <code>null</code> if none
     */
    public String getIndex() {
        return index;
    }

    /**
     * Get the relation.
     *
     * @return the relation or <code>null</code> if none
     */
    public Relation getRelation() {
        return relation;
    }

    /**
     * Check if index and relation in this search clause are set.
     *
     * @return <code>true</code> if index and relation were set,
     *         <code>false</code> otherwise
     */
    public boolean hasIndexAndRelation() {
        return (index != null) && (relation != null);
    }

    /**
     * Get the search term.
     *
     * @return the search term
     */
    public String getSearchTerm() {
        return search_term;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(nodeType.toDisplayString());
        sb.append(' ');
        if (index != null) {
            sb.append(index).append(' ');
        }
        if (relation != null) {
            sb.append(relation).append(' ');
        }
        sb.append(search_term);
        sb.append(')');
        return sb.toString();
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

} // class SearchClause
