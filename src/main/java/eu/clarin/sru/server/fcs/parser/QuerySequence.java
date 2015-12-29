package eu.clarin.sru.server.fcs.parser;

import java.util.List;

public class QuerySequence extends QueryNode {

    QuerySequence(List<QueryNode> children) {
        super(QueryNodeType.QUERY_SEQUENCE, children);
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
