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
 * Interface implementing a Visitor pattern for FCS-QL expression trees.
 */
public interface QueryVisitor {
    /**
     * Visit a <em>segment</em> query node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(QuerySegment node);


    /**
     * Visit a <em>group</em> query node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(QueryGroup node);


    /**
     * Visit a <em>sequence</em> query node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(QuerySequence node);


    /**
     * Visit a <em>or</em> query node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(QueryDisjunction node);


    /**
     * Visit a <em>query</em> with within node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(QueryWithWithin node);


    /**
     * Visit a <em>simple</em> expression node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(Expression node);


    /**
     * Visit a <em>wildcard</em> expression node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(ExpressionWildcard node);


    /**
     * Visit a <em>group</em> expression node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(ExpressionGroup node);


    /**
     * Visit a <em>or</em> expression node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(ExpressionOr node);


    /**
     * Visit a <em>and</em> expression node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(ExpressionAnd node);


    /**
     * Visit a <em>not</em> expression node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(ExpressionNot node);


    /**
     * Visit a <em>simple within</em> node.
     *
     * @param node
     *            the node to visit
     */
    public void visit(SimpleWithin node);

} // interface QueryVisitor
