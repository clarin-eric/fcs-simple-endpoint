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
 * Interface implementing a Visitor pattern for FCS-QL expression trees.
 */
public interface QueryVisitor {
    /**
     * Visit a <em>segment</em> query node.
     *
     * @param querySegment
     *            the node to visit
     */
    public void visit(QuerySegment querySegment);


    /**
     * Visit a <em>group</em> query node.
     *
     * @param queryGroup
     *            the node to visit
     */
    public void visit(QueryGroup queryGroup);


    /**
     * Visit a <em>sequence</em> query node.
     *
     * @param querySequence
     *            the node to visit
     */
    public void visit(QuerySequence querySequence);


    /**
     * Visit a <em>or</em> query node.
     *
     * @param queryDisjunction
     *            the node to visit
     */
    public void visit(QueryDisjunction queryDisjunction);


    /**
     * Visit a <em>query</em> with within node.
     *
     * @param queryWithWithin
     *            the node to visit
     */
    public void visit(QueryWithWithin queryWithWithin);


    /**
     * Visit a <em>simple</em> expression node.
     *
     * @param expression
     *            the node to visit
     */
    public void visit(Expression expression);


    /**
     * Visit a <em>wildcard</em> expression node.
     *
     * @param expressionWildcard
     *            the node to visit
     */
    public void visit(ExpressionWildcard expressionWildcard);


    /**
     * Visit a <em>group</em> expression node.
     *
     * @param expressionGroup
     *            the node to visit
     */
    public void visit(ExpressionGroup expressionGroup);


    /**
     * Visit a <em>or</em> expression node.
     *
     * @param expressionOr
     *            the node to visit
     */
    public void visit(ExpressionOr expressionOr);


    /**
     * Visit a <em>and</em> expression node.
     *
     * @param expressionAnd
     *            the node to visit
     */
    public void visit(ExpressionAnd expressionAnd);


    /**
     * Visit a <em>not</em> expression node.
     *
     * @param expressionNot
     *            the node to visit
     */
    public void visit(ExpressionNot expressionNot);


    /**
     * Visit a <em>simple within</em> node.
     *
     * @param simpleWithin
     *            the node to visit
     */
    public void visit(SimpleWithin simpleWithin);

} // interface QueryVisitor
