package eu.clarin.sru.server.fcs.parser;

public class QueryGroup extends QueryNode {
    private int minOccurs;
    private int maxOccurs;


    QueryGroup(QueryNode content, int minOccurs, int maxOccurs) {
        super(QueryNodeType.QUERY_GROUP, content);
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }


    public QueryNode getContent() {
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
        sb.append("(")
            .append(nodeType.toDisplayString())
            .append(" ");
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
        if (!children.isEmpty()) {
            for (QueryNode child : children) {
                visitAnyNode(visitor, child);
            }
        }
        visitor.visit(this);
    }

}
