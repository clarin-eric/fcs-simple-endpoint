package eu.clarin.sru.server.fcs.parser;

import java.util.Collections;
import java.util.Set;


public class Expression extends QueryNode {
    private final String qualifier;
    private final String identifier;
    private final Operator operator;
    private final String regex;
    private final Set<RegexFlag> regex_flags;


    Expression(String qualifier, String identifier, Operator operator,
            String regex, Set<RegexFlag> regex_flags) {
        super(QueryNodeType.EXPRESSION);
        if ((qualifier != null) && !qualifier.isEmpty()) {
            this.qualifier = qualifier;
        } else {
            this.qualifier = null;
        }
        this.identifier = identifier;
        this.operator = operator;
        this.regex = regex;
        if ((regex_flags != null) && !regex_flags.isEmpty()) {
            this.regex_flags = Collections.unmodifiableSet(regex_flags);
        } else {
            this.regex_flags = null;
        }
    }


    public String getLayerIdentifier() {
        return identifier;
    }

    
    public String getLayerQualifier() {
        return qualifier;
    }


    public Operator getOperator() {
        return operator;
    }


    public String getRegexValue() {
        return regex;
    }


    public Set<RegexFlag> getRegexFlags() {
        return regex_flags;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(nodeType.toDisplayString());
        sb.append(" ");
        if (qualifier != null) {
            sb.append(qualifier).append(":");
        }
        sb.append(identifier);
        sb.append(" ");
        sb.append(operator.toDisplayString());
        sb.append(" \"");
        sb.append(regex);
        sb.append("\"");
        if (regex_flags != null) {
            sb.append("/");
            if (regex_flags.contains(RegexFlag.CASE_INSENSITVE)) {
                sb.append("i");
            }
            if (regex_flags.contains(RegexFlag.CASE_SENSITVE)) {
                sb.append("I");
            }
            if (regex_flags.contains(RegexFlag.LITERAL_MATCHING)) {
                sb.append("l");
            }
            if (regex_flags.contains(RegexFlag.IGNORE_DIACRITICS)) {
                sb.append("d");
            }
        }
        sb.append(")");
        return sb.toString();
    }


    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

}
