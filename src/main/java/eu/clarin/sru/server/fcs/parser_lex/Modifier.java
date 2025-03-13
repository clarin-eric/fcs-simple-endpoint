/**
 * This software is copyright (c) 2013-2025 by
 *  - Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.parser_lex;

/**
 * A LexCQL expression tree modifier node.
 */
public class Modifier extends QueryNode {
    private final String name;
    private final String relation;
    private final String value;

    /**
     * Constructor.
     *
     * @param name     the modifier name
     * @param relation the modifier relation symbol or <code>null</code> if none
     * @param value    the modifier relation value or <code>null</code> if none
     */
    Modifier(String name, String relation, String value) {
        super(QueryNodeType.MODIFIER);
        this.name = name;
        this.relation = relation;
        this.value = value;
    }

    /**
     * Get the modifier name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the modifier relation.
     *
     * @return the relation or <code>null</code> if none
     */
    public String getRelation() {
        return relation;
    }

    /**
     * Get the modifier value.
     *
     * @return the value or <code>null</code> if none
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(nodeType.toDisplayString());
        sb.append(" ");
        sb.append("/");
        sb.append(name);
        if (relation != null && value != null) {
            sb.append(" ").append(relation);
            sb.append(" ").append(value);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }
} // class Modifier