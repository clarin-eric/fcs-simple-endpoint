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
 * Convenience class to implement FCS-QL expression tree visitors. Default
 * method implementations do nothing.
 */
public class QueryVistorAdapter implements QueryVisitor {

    @Override
    public void visit(QuerySegment querySegment) {
    }


    @Override
    public void visit(QueryGroup queryGroup) {
    }


    @Override
    public void visit(QuerySequence querySequence) {
    }


    @Override
    public void visit(QueryDisjunction queryDisjunction) {
    }


    @Override
    public void visit(QueryWithWithin queryWithWithin) {
    }


    @Override
    public void visit(Expression expressionBasic) {
    }


    @Override
    public void visit(ExpressionWildcard expressionWildcard) {
    }


    @Override
    public void visit(ExpressionGroup expressionGroup) {
    }


    @Override
    public void visit(ExpressionOr expressionOr) {
    }


    @Override
    public void visit(ExpressionAnd expressionAnd) {
    }


    @Override
    public void visit(ExpressionNot expressionNot) {
    }


    @Override
    public void visit(SimpleWithin simpleWithin) {
    }

} // class QueryVistorAdapter
