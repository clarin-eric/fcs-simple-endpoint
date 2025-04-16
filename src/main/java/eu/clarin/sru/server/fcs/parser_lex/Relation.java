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

import java.util.List;

/**
 * A LexCQL expression tree relation node.
 */
public class Relation extends QueryNode {
    private final String relation;
    private final List<Modifier> modifiers;

    /**
     * Constructor.
     *
     * @param relation  the relation name or symbol
     * @param modifiers the list of modifiers for this relation or <code>null</code>
     *                  if none
     */
    Relation(String relation, List<Modifier> modifiers) {
        super(QueryNodeType.RELATION);
        this.relation = relation;
        if ((modifiers != null) && !modifiers.isEmpty()) {
            this.modifiers = modifiers;
        } else {
            this.modifiers = null;
        }
    }

    /**
     * Get the relation.
     *
     * @return the relation
     */
    public String getRelation() {
        return relation;
    }

    /**
     * Get the modifiers.
     *
     * @return the modifiers
     */
    public List<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(nodeType.toDisplayString());
        sb.append(' ');
        sb.append(relation);
        if (modifiers != null) {
            for (Modifier modifier : modifiers) {
                sb.append(' ').append(modifier);
            }
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

} // class Relation