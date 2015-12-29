package eu.clarin.sru.server.fcs.parser;

public class SimpleWithin extends QueryNode {
    public enum Scope {
        SENTENCE {
            @Override
            public String toDisplayString() {
                return "Sentence";
            }

        },
        UTTERANCE {
            @Override
            public String toDisplayString() {
                return "Utterance";
            }

        },
        PARAGRAPH {
            @Override
            public String toDisplayString() {
                return "Paragraph";
            }

        },
        TURN {
            @Override
            public String toDisplayString() {
                return "Turn";
            }
        },
        TEXT {
            @Override
            public String toDisplayString() {
                return "Text";
            }
        },
        SESSION {
            @Override
            public String toDisplayString() {
                return "Session";
            }
        };

        abstract String toDisplayString();
    }
    private final Scope scope;


    public SimpleWithin(Scope scope) {
        super(QueryNodeType.SIMPLE_WITHIN);
        this.scope = scope;
    }


    public Scope getScope() {
        return scope;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(")
            .append(nodeType.toDisplayString())
            .append(" ")
            .append(scope.toDisplayString())
            .append(")");
        return sb.toString();
    }


    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

} // class SimpleWithin
