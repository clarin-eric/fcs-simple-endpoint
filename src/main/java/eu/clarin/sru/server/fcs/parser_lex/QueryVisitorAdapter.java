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
 * Convenience class to implement LexCQL expression tree visitors. Default
 * method implementations do nothing.
 */
public class QueryVisitorAdapter implements QueryVisitor {

    @Override
    public void visit(SearchClauseGroup node) {
    }

    @Override
    public void visit(Subquery node) {
    }

    @Override
    public void visit(SearchClause node) {
    }

    @Override
    public void visit(Relation node) {
    }

    @Override
    public void visit(Modifier node) {
    }

} // class QueryVistorAdapter
