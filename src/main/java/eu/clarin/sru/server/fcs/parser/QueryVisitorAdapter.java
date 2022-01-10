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
 * Convenience class to implement FCS-QL expression tree visitors. Default
 * method implementations do nothing.
 */
public class QueryVisitorAdapter implements QueryVisitor {

    @Override
    public void visit(QuerySegment node) {
    }


    @Override
    public void visit(QueryGroup node) {
    }


    @Override
    public void visit(QuerySequence node) {
    }


    @Override
    public void visit(QueryDisjunction node) {
    }


    @Override
    public void visit(QueryWithWithin node) {
    }


    @Override
    public void visit(Expression node) {
    }


    @Override
    public void visit(ExpressionWildcard node) {
    }


    @Override
    public void visit(ExpressionGroup node) {
    }


    @Override
    public void visit(ExpressionOr node) {
    }


    @Override
    public void visit(ExpressionAnd node) {
    }


    @Override
    public void visit(ExpressionNot node) {
    }


    @Override
    public void visit(SimpleWithin node) {
    }

} // class QueryVistorAdapter
