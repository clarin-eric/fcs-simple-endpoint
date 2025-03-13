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
package eu.clarin.sru.server.fcs;

import java.net.URI;

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


    // query types
    public static final String FCS_QUERY_TYPE_FCS          = "fcs";
    public static final String FCS_QUERY_TYPE_LEX          = "lex";
    public static final String FCS_QUERY_TYPE_CQL          =
            SRUConstants.SRU_QUERY_TYPE_CQL;
    public static final String FCS_QUERY_TYPE_SEARCH_TERMS =
            SRUConstants.SRU_QUERY_TYPE_SEARCH_TERMS;

    // FCS advanced layer type identifiers
    public static final String FCS_LAYER_TYPE_TEXT = "text";
    public static final String FCS_LAYER_TYPE_LEMMA = "lemma";
    public static final String FCS_LAYER_TYPE_POS = "pos";
    public static final String FCS_LAYER_TYPE_ORTH = "orth";
    public static final String FCS_LAYER_TYPE_NORM = "norm";
    public static final String FCS_LAYER_TYPE_PHONETIC = "phonetic";

    public static final String[] FCS_FIELD_TYPES = {
            FCS_LAYER_TYPE_TEXT,
            FCS_LAYER_TYPE_LEMMA,
            FCS_LAYER_TYPE_POS,
            FCS_LAYER_TYPE_ORTH,
            FCS_LAYER_TYPE_NORM,
            FCS_LAYER_TYPE_PHONETIC
    };

    // FCS lexical field type identifiers
    public static final String LEX_FIELD_TYPE_ENTRYID = "entryId";
    public static final String LEX_FIELD_TYPE_LEMMA = "lemma";
    public static final String LEX_FIELD_TYPE_TRANSLATION = "translation";
    public static final String LEX_FIELD_TYPE_TRANSCRIPTION = "transcription";
    public static final String LEX_FIELD_TYPE_PHONETIC = "phonetic";
    public static final String LEX_FIELD_TYPE_DEFINITION = "definition";
    public static final String LEX_FIELD_TYPE_ETYMOLOGY = "etymology";
    public static final String LEX_FIELD_TYPE_CASE = "case";
    public static final String LEX_FIELD_TYPE_NUMBER = "number";
    public static final String LEX_FIELD_TYPE_GENDER = "gender";
    public static final String LEX_FIELD_TYPE_POS = "pos";
    public static final String LEX_FIELD_TYPE_BASEFORM = "baseform";
    public static final String LEX_FIELD_TYPE_SEGMENTATION = "segmentation";
    public static final String LEX_FIELD_TYPE_SENTIMENT = "sentiment";
    public static final String LEX_FIELD_TYPE_FREQUENCY = "frequency";
    public static final String LEX_FIELD_TYPE_ANTONYM = "antonym";
    public static final String LEX_FIELD_TYPE_HYPONYM = "hyponym";
    public static final String LEX_FIELD_TYPE_HYPERNYM = "hypernym";
    public static final String LEX_FIELD_TYPE_MERONYM = "meronym";
    public static final String LEX_FIELD_TYPE_HOLONYM = "holonym";
    public static final String LEX_FIELD_TYPE_SYNONYM = "synonym";
    public static final String LEX_FIELD_TYPE_RELATED = "related";
    public static final String LEX_FIELD_TYPE_REF = "ref";
    public static final String LEX_FIELD_TYPE_SENSEREF = "senseRef";
    public static final String LEX_FIELD_TYPE_CITATION = "citation";

    public static final String[] LEX_FIELD_TYPES = {
            LEX_FIELD_TYPE_ENTRYID,
            LEX_FIELD_TYPE_LEMMA,
            LEX_FIELD_TYPE_TRANSLATION,
            LEX_FIELD_TYPE_TRANSCRIPTION,
            LEX_FIELD_TYPE_PHONETIC,
            LEX_FIELD_TYPE_DEFINITION,
            LEX_FIELD_TYPE_ETYMOLOGY,
            LEX_FIELD_TYPE_CASE,
            LEX_FIELD_TYPE_NUMBER,
            LEX_FIELD_TYPE_GENDER,
            LEX_FIELD_TYPE_POS,
            LEX_FIELD_TYPE_BASEFORM,
            LEX_FIELD_TYPE_SEGMENTATION,
            LEX_FIELD_TYPE_SENTIMENT,
            LEX_FIELD_TYPE_FREQUENCY,
            LEX_FIELD_TYPE_ANTONYM,
            LEX_FIELD_TYPE_HYPONYM,
            LEX_FIELD_TYPE_HYPERNYM,
            LEX_FIELD_TYPE_MERONYM,
            LEX_FIELD_TYPE_HOLONYM,
            LEX_FIELD_TYPE_SYNONYM,
            LEX_FIELD_TYPE_RELATED,
            LEX_FIELD_TYPE_REF,
            LEX_FIELD_TYPE_SENSEREF,
            LEX_FIELD_TYPE_CITATION
    };

    // FCS capability URLs
    public static final URI CAP_BASIC_SEARCH =
            URI.create("http://clarin.eu/fcs/capability/basic-search");
    public static final URI CAP_ADVANCED_SEARCH =
            URI.create("http://clarin.eu/fcs/capability/advanced-search");
    public static final URI CAP_LEX_SEARCH =
            URI.create("http://clarin.eu/fcs/capability/lex-search");
    public static final URI CAP_AUTHENTICATED_SEARCH =
            URI.create("http://clarin.eu/fcs/capability/authenticated-search");

    // FCS data view mime types
    public static final String MIMETYPE_HITS = "application/x-clarin-fcs-hits+xml";
    public static final String MIMETYPE_ADV = "application/x-clarin-fcs-adv+xml";
    public static final String MIMETYPE_LEX = "application/x-clarin-fcs-lex+xml";

    // FCS data view namespaces
    public static final String NS_HITS = "http://clarin.eu/fcs/dataview/hits";
    public static final String NS_ADV = "http://clarin.eu/fcs/dataview/advanced";
    public static final String NS_LEX = "http://clarin.eu/fcs/dataview/lex";
    // default xml namespace prefixes
    public static final String XML_PREFIX_HITS = "hits";
    public static final String XML_PREFIX_ADV = "adv";
    public static final String XML_PREFIX_LEX = "lex";

    // FCS request parameters to extract Resource PIDs
    public static final String X_FCS_CONTEXT_KEY = "x-fcs-context";
    public static final String X_FCS_CONTEXT_SEPARATOR = ",";

    // FCS request parameters to extract Data Views
    public static final String X_FCS_DATAVIEWS_KEY = "x-fcs-dataviews";
    public static final String X_FCS_DATAVIEWS_SEPARATOR = ",";

    public static final String CLARIN_FCS_RECORD_SCHEMA = "http://clarin.eu/fcs/resource";

    /* hide constructor */
    private Constants() {
    }

} // final class Constants
