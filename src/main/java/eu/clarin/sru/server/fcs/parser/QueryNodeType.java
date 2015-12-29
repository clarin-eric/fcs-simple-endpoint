package eu.clarin.sru.server.fcs.parser;

public enum QueryNodeType {
    QUERY_SEGMENT {
        @Override
        String toDisplayString() {
            return "QuerySegment";
        }
    },
    QUERY_GROUP {
        @Override
        String toDisplayString() {
            return "QueryGroup";
        }
    },
    QUERY_SEQUENCE {
        @Override
        String toDisplayString() {
            return "QuerySequence";
        }
    },
    QUERY_DISJUNCTION {
        @Override
        String toDisplayString() {
            return "QueryDisjunction";
        }
    },
    QUERY_WITH_WITHIN {
        @Override
        String toDisplayString() {
            return "QueryWithWithin";
        }
    },
    EXPRESSION {
        @Override
        String toDisplayString() {
            return "Expression";
        }
    },
    EXPRESSION_WILDCARD {
        @Override
        String toDisplayString() {
            return "Wildcard";
        }
    },
    EXPRESSION_GROUP {
        @Override
        String toDisplayString() {
            return "Group";
        }
    },
    EXPRESSION_OR {
        @Override
        String toDisplayString() {
            return "Or";
        }
    },
    EXPRESSION_AND {
        @Override
        String toDisplayString() {
            return "And";
        }
    },
    EXPRESSION_NOT {
        @Override
        String toDisplayString() {
            return "Not";
        }
    },
    SIMPLE_WITHIN {
        @Override
        String toDisplayString() {
            return "SimpleWithin";
        }
    };

    abstract String toDisplayString();
}