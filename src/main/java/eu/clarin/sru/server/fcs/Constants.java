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
package eu.clarin.sru.server.fcs;

import eu.clarin.sru.server.SRUConstants;

/**
 * Various useful constants for CLARIN-FCS endpoints.
 */
public final class Constants {

    /* diagnostics */
    private static final String FCS_DIAGNOSTIC_URI_PREFIX =
            "http://clarin.eu/fcs/diagnostic/";

    public static final String FCS_DIAGNOSTIC_PERSISTENT_IDENTIFIER_INVALID =
            FCS_DIAGNOSTIC_URI_PREFIX + 1;
    public static final String FCS_DIAGNOSTIC_RESOURCE_TOO_LARGE_CONTEXT_ADJUSTED =
            FCS_DIAGNOSTIC_URI_PREFIX + 2;
    public static final String FCS_DIAGNOSTIC_RESOURCE_TOO_LARGE_CANNOT_PERFORM_QUERY =
            FCS_DIAGNOSTIC_URI_PREFIX + 3;
    public static final String FCS_DIAGNOSTIC_REQUESTED_DATA_VIEW_INVALID =
            FCS_DIAGNOSTIC_URI_PREFIX + 4;
    public static final String FCS_DIAGNOSTIC_GENERAL_QUERY_SYNTAX_ERROR =
            FCS_DIAGNOSTIC_URI_PREFIX + 10;
    public static final String FCS_DIAGNOSTIC_GENERAL_QUERY_TOO_COMPLEX_CANNOT_PERFORM_QUERY =
            FCS_DIAGNOSTIC_URI_PREFIX + 11;
    public static final String FCS_DIAGNOSTIC_QUERY_WAS_REWRITTEN =
            FCS_DIAGNOSTIC_URI_PREFIX + 12;
    public static final String FCS_DIAGNOSTIC_GENERAL_PROCESSING_HINT =
            FCS_DIAGNOSTIC_URI_PREFIX + 13;


    public static final String FCS_QUERY_TYPE_FCS          = "fcs";
    public static final String FCS_QUERY_TYPE_CQL          =
            SRUConstants.SRU_QUERY_TYPE_CQL;
    public static final String FCS_QUERY_TYPE_SEARCH_TERMS =
            SRUConstants.SRU_QUERY_TYPE_SEARCH_TERMS;

    /** constant Layer Type identifier for default "text" layer */
    public static final String FCS_LAYER_TYPE_TEXT = "text";
    /** constant Layer Type identifier for default "lemma" layer */
    public static final String FCS_LAYER_TYPE_LEMMA = "lemma";
    /** constant Layer Type identifier for default "pos" layer */
    public static final String FCS_LAYER_TYPE_POS = "pos";
    /** constant Layer Type identifier for default "orth" layer */
    public static final String FCS_LAYER_TYPE_ORTH = "orth";
    /** constant Layer Type identifier for default "norm" layer */
    public static final String FCS_LAYER_TYPE_NORM = "norm";
    /** constant Layer Type identifier for default "phonetic" layer */
    public static final String FCS_LAYER_TYPE_PHONETIC = "phonetic";

    
    /* hide constructor */
    private Constants() {
    }

} // final class Constants
