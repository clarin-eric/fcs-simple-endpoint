package eu.clarin.sru.server.fcs.parser;

import java.util.List;


public class ExpressionAnd extends QueryNode {

    ExpressionAnd(List<QueryNode> children) {
        super(QueryNodeType.EXPRESSION_AND, children);
    }


    @Override
    public void accept(QueryVisitor visitor) {
        if (!children.isEmpty()) {
            for (QueryNode child : children) {
                visitAnyNode(visitor, child);
            }
        }
        visitor.visit(this);
    }

}
