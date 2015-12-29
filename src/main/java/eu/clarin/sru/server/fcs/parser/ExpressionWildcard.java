package eu.clarin.sru.server.fcs.parser;

public class ExpressionWildcard extends QueryNode {

    ExpressionWildcard() {
        super(QueryNodeType.EXPRESSION_WILDCARD);
    }


    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

}
