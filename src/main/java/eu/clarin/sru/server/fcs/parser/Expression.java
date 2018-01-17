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
     * Check if the expression used a given <em>Layer Type Identifier</em>.
     *
     * @param identifier
     *            the Layer Type Identifier to check against
     * @return <code>true</code> if this identifier was used, <code>false</code>
     *         otherwise
     */
    public boolean hasLayerIdentifier(String identifier) {
        if (identifier == null) {
            throw new NullPointerException("identifier == null");
        }
        return this.identifier.equals(identifier);
    }


    /**
     * Get the Layer Type Identifier qualifier.
     *
     * @return the Layer Type Identifier qualifier or <code>null</code> if none
     *         was used in this expression
     */
    public String getLayerQualifier() {
        return qualifier;
    }


    /**
     * Check if the Layer Type Identifier qualifier is empty.
     *
     * @return <code>true</code> if no Layer Type Identifier qualifier was set,
     *         <code>false</code> otherwise
     */
    public boolean isLayerQualifierEmpty() {
        return (qualifier == null);
    }


    /**
     * Check if the expression used a given qualifier for the Layer Type
     * Identifier.
     *
     * @param qualifier
     *            the qualifier to check against
     * @return <code>true</code> if this identifier was used, <code>false</code>
     *         otherwise
     */
    public boolean hasLayerQualifier(String qualifier) {
        if (qualifier == null) {
            throw new NullPointerException("qualifier == null");
        }
        if (this.qualifier != null) {
            return this.qualifier.equals(qualifier);
        } else {
            return false;
        }
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
     * Check if expression used a given operator.
     *
     * @param operator
     *            the operator to check
     * @return <code>true</code> if the given operator was used,
     *         <code>false</code> otherwise
     */
    public boolean hasOperator(Operator operator) {
        if (operator == null) {
            throw new NullPointerException("operator == null");
        }
        return this.operator == operator;
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
     * @return the regex flags set or <code>null</code> if no flags were used
     *         in this expression
     */
    public Set<RegexFlag> getRegexFlags() {
        return regex_flags;
    }


    /**
     * Check if a regex flag set is empty.
     *
     * @return <code>true</code> if no regex flags where set, <code>false</code>
     *         otherwise
     */
    public boolean isRegexFlagsEmpty() {
        return (regex_flags == null);
    }


    /**
     * Check if a regex flag is set.
     *
     * @param flag
     *            the flag to be checked
     * @return <code>true</code> if the flag is set, <code>false</code>
     *         otherwise
     */
    public boolean hasRegexFlag(RegexFlag flag) {
        if (flag == null) {
            throw new NullPointerException("flag == null");
        }
        if (regex_flags != null) {
            return regex_flags.contains(flag);
        } else {
            return false;
        }
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
