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
package eu.clarin.sru.server.fcs;

/**
 * This class is used to information about a Lex Fields that is available by the
 * Endpoint.
 */
public class LexField {
    private final String id;
    private final String type;

    /**
     * Constructor.
     *
     * @param id
     *             the identifier of the lexical field
     * @param type
     *             the type identifier for the lexical field
     */
    public LexField(String id, String type) {
        if (id == null) {
            throw new NullPointerException("id == null");
        }
        this.id = id;

        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.type = type;
    }

    /**
     * Get the identifier for the lex field.
     *
     * @return the identifier for the lex field
     */
    public String getId() {
        return id;
    }

    /**
     * Get the type identifier for this lex field.
     *
     * @return the type identifier of the lex field
     */
    public String getType() {
        return type;
    }
}
