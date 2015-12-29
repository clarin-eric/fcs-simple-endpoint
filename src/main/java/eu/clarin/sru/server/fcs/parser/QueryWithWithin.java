package eu.clarin.sru.server.fcs.parser;

import java.util.Arrays;

public class QueryWithWithin extends QueryNode {

    QueryWithWithin(QueryNode query, QueryNode within) {
        super(QueryNodeType.QUERY_WITH_WITHIN, Arrays.asList(query, within));
    }


    public QueryNode getQuery() {
        return getChild(0);
    }


    public QueryNode getWithin() {
        return getChild(1);
    }


    @Override
    public void accept(QueryVisitor visitor) {
        visitAnyNode(visitor, children.get(0));
        if (children.size() > 1) {
            visitAnyNode(visitor, children.get(1));
        }
        visitor.visit(this);
    }

}
