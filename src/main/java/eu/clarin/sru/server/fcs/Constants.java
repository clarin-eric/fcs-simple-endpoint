package eu.clarin.sru.server.fcs;

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


    public static final String FCS_QUERY_TYPE = "fcs";


    /* hide constructor */
    private Constants() {
    }

} // final class Constants
