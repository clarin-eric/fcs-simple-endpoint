package eu.clarin.sru.server.fcs.parser;

import java.util.List;

public class QueryDisjunction extends QueryNode {

    QueryDisjunction(List<QueryNode> children) {
        super(QueryNodeType.QUERY_DISJUNCTION, children);
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
