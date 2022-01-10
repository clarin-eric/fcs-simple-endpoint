/**
 * This software is copyright (c) 2013-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.parser;


/**
 * A FCS-QL expression tree SIMPLE WITHIN query node.
 */
public class SimpleWithin extends QueryNode {
    /**
     * The within scope.
     */
    public enum Scope {
        /**
         * sentence scope (small).
         */
        SENTENCE {
            @Override
            public String toDisplayString() {
                return "Sentence";
            }

        },
        /**
         * utterance scope (small).
         */
        UTTERANCE {
            @Override
            public String toDisplayString() {
                return "Utterance";
            }

        },
        /**
         * paragraph scope (medium).
         */
        PARAGRAPH {
            @Override
            public String toDisplayString() {
                return "Paragraph";
            }

        },
        /**
         * turn scope (medium).
         */
        TURN {
            @Override
            public String toDisplayString() {
                return "Turn";
            }
        },
        /**
         * text scope (large).
         */
        TEXT {
            @Override
            public String toDisplayString() {
                return "Text";
            }
        },
        /**
         * session scope (large).
         */
        SESSION {
            @Override
            public String toDisplayString() {
                return "Session";
            }
        };

        abstract String toDisplayString();
    }
    private final Scope scope;


    /**
     * Constructor.
     *
     * @param scope
     *            the scope
     */
    SimpleWithin(Scope scope) {
        super(QueryNodeType.SIMPLE_WITHIN);
        this.scope = scope;
    }


    /**
     * Get the simple within scope
     *
     * @return the simple within scope
     */
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
