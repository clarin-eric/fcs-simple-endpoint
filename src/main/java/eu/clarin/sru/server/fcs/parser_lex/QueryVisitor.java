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
 * Interface implementing a Visitor pattern for LexCQL expression trees.
 */
public interface QueryVisitor {
    /**
     * Visit a <em>search_clause_group</em> query node.
     *
     * @param node the node to visit
     */
    public void visit(SearchClauseGroup node);

    /**
     * Visit a <em>subquery</em> query node.
     *
     * @param node the node to visit
     */
    public void visit(Subquery node);

    /**
     * Visit a <em>search_clause</em> query node.
     *
     * @param node the node to visit
     */
    public void visit(SearchClause node);

    /**
     * Visit a <em>relation</em> query node.
     *
     * @param node the node to visit
     */
    public void visit(Relation node);

    /**
     * Visit a <em>modifier</em> query node.
     *
     * @param node the node to visit
     */
    public void visit(Modifier node);

} // interface QueryVisitor
