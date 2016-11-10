/**
 * This software is copyright (c) 2013-2016 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.parser;

import java.util.Collections;
import java.util.Set;


/**
 * A FCS-QL expression tree SIMPLE expression node.
 */
public class Expression extends QueryNode {
    private final String qualifier;
    private final String identifier;
    private final Operator operator;
    private final String regex;
    private final Set<RegexFlag> regex_flags;


    /**
     * Constructor.
     *
     * @param qualifier
     *            the layer identifier qualifier or <code>null</code> if none
     * @param identifier
     *            the layer identifier
     * @param operator
     *            the operator
     * @param regex
     *            the regular expression
     * @param regex_flags
     *            the regular expression flags or <code>null</code> of none
     */
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


    /**
     * Get the layer identifier.
     *
     * @return the layer identifier
     */
    public String getLayerIdentifier() {
        return identifier;
    }


    /**
     * Get the layer identifier qualifier.
     *
     * @return the layer identifier qualifier or <code>null</code> if none
     */
    public String getLayerQualifier() {
        return qualifier;
    }


    /**
     * Get the operator.
     *
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }


    /**
     * Get the regex value.
     *
     * @return the regex value
     */
    public String getRegexValue() {
        return regex;
    }


    /**
     * Get the regex flags set.
     *
     * @return the regex flags set or <code>null</code> if none
     */
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
        for (int i = 0; i < regex.length(); i++) {
            char ch = regex.charAt(i);
            switch (ch) {
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                sb.append(ch);
            }
        }
        sb.append("\"");
        sb.append(" (").append(regex.length()).append(")");
        if (regex_flags != null) {
            sb.append("/");
            if (regex_flags.contains(RegexFlag.CASE_INSENSITIVE)) {
                sb.append("i");
            }
            if (regex_flags.contains(RegexFlag.CASE_SENSITIVE)) {
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

} // class Expression
