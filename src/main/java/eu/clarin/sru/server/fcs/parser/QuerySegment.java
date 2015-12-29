package eu.clarin.sru.server.fcs.parser;

public class QuerySegment extends QueryNode {
    private final int minOccurs;
    private final int maxOccurs;


    QuerySegment(QueryNode expression, int minOccurs, int maxOccurs) {
        super(QueryNodeType.QUERY_SEGMENT, expression);
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }


    public QueryNode getExpression() {
        return children.get(0);
    }


    public int getMinOccurs() {
        return minOccurs;
    }


    public int getMaxOccurs() {
        return maxOccurs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(nodeType.toDisplayString());
        sb.append(" ");
        if (minOccurs != 1) {
            sb.append("@min=");
            if (minOccurs == Constants.OCCURS_UNBOUNDED) {
                sb.append("*");
            } else {
                sb.append(minOccurs);
            }
            sb.append(" ");
        }
        if (maxOccurs != 1) {
            sb.append("@max=");
            if (maxOccurs == Constants.OCCURS_UNBOUNDED) {
                sb.append("*");
            } else {
                sb.append(maxOccurs);
            }
            sb.append(" ");

        }
        sb.append(children.get(0));
        sb.append(")");
        return sb.toString();
    }


    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

}
