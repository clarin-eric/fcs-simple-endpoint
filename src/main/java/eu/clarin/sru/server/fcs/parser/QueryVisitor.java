package eu.clarin.sru.server.fcs.parser;

public interface QueryVisitor {

    public void visit(QuerySegment querySegment);


    public void visit(QueryGroup queryGroup);


    public void visit(QuerySequence querySequence);


    public void visit(QueryDisjunction queryDisjunction);


    public void visit(QueryWithWithin queryWithWithin);


    public void visit(Expression expressionBasic);


    public void visit(ExpressionWildcard expressionWildcard);


    public void visit(ExpressionGroup expressionGroup);


    public void visit(ExpressionOr expressionOr);


    public void visit(ExpressionAnd expressionAnd);


    public void visit(ExpressionNot expressionNot);


    public void visit(SimpleWithin simpleWithin);

}
