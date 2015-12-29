package eu.clarin.sru.server.fcs.parser;


public class ExpressionNot extends QueryNode {

    ExpressionNot(QueryNode child) {
        super(QueryNodeType.EXPRESSION_NOT, child);
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
        visitAnyNode(visitor, children.get(0));
        visitor.visit(this);
    }

}
