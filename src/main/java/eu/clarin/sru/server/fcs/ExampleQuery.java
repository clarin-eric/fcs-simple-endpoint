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

import java.util.Collections;
import java.util.Map;

/**
 * This class is used to store information about a single example query,
 * bundling query string, query type and multilingual description strings.
 */
public class ExampleQuery {
    private final String query;
    private final String queryType;
    private final Map<String, String> description;


    /**
     * Constructor.
     * 
     * @param query
     *                    the query string of this example query
     * @param queryType
     *                    the query type for this example query
     * @param description
     *                    the description of the example query represented as a map
     *                    with pairs of language code and description
     */
    public ExampleQuery(String query, String queryType, Map<String, String> description) {
        if (query == null) {
            throw new NullPointerException("query == null");
        }
        this.query = query;

        if (queryType == null) {
            throw new NullPointerException("queryType == null");
        }
        this.queryType = queryType;

        if (description == null) {
            throw new NullPointerException("description == null");
        }
        if (description.isEmpty()) {
            throw new IllegalArgumentException("description is empty");
        }
        this.description = Collections.unmodifiableMap(description);
    }


    /**
     * Get the query string for this example query.
     *
     * @return the query string
     */
    public String getQuery() {
        return query;
    }


    /**
     * Get the query type for this example query.
     *
     * @return the query type
     */
    public String getQueryType() {
        return queryType;
    }


    /**
     * Get the description of this example query.
     *
     * @return a Map of descriptions keyed by language code
     */
    public Map<String, String> getDescription() {
        return description;
    }


    /**
     * Get the description of the example query for a specific language code.
     *
     * @param language
     *                 the language code
     * @return the description for the language code or <code>null</code> if no
     *         title for this language code exists
     */
    public String getDescription(String language) {
        return (description != null) ? description.get(language) : null;
    }

}
