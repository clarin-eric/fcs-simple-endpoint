package eu.clarin.sru.server.fcs.parser;

public class ExpressionGroup extends QueryNode {

    ExpressionGroup(QueryNode content) {
        super(QueryNodeType.EXPRESSION_GROUP, content);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(")
            .append(nodeType.toDisplayString())
            .append(children.get(0))
            .append(")");
        return sb.toString();
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
