package eu.clarin.sru.server.fcs.parser;

public enum Operator {
    EQUALS {
        @Override
        String toDisplayString() {
            return "Eq";
        }
    },
    NOT_EQUALS {
        @Override
        String toDisplayString() {
            return "Ne";
        }
    };

    abstract String toDisplayString();
}
