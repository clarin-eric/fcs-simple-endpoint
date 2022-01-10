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

import java.util.Arrays;


/**
 * FCS-QL expression tree QUERY-WITH-WITHIN node.
 */
public class QueryWithWithin extends QueryNode {

    /**
     * Constructor.
     *
     * @param query
     *            the query node
     * @param within
     *            the within node
     */
    QueryWithWithin(QueryNode query, QueryNode within) {
        super(QueryNodeType.QUERY_WITH_WITHIN, Arrays.asList(query, within));
    }


    /**
     * Get the query clause.
     *
     * @return the query clause
     */
    public QueryNode getQuery() {
        return getChild(0);
    }


    /**
     * Get the within clause (= search context)
     *
     * @return the witin clause
     */
    public QueryNode getWithin() {
        return getChild(1);
    }


    @Override
    public void accept(QueryVisitor visitor) {
        children.get(0).accept(visitor);
        if (children.size() > 1) {
            children.get(1).accept(visitor);
        }
        visitor.visit(this);
    }

} // class QueryWithWithin
