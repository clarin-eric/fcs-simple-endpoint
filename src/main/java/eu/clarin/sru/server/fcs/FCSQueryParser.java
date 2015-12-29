package eu.clarin.sru.server.fcs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUQuery;
import eu.clarin.sru.server.SRUQueryBase;
import eu.clarin.sru.server.SRUQueryParser;
import eu.clarin.sru.server.SRUVersion;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QueryParser;
import eu.clarin.sru.server.fcs.parser.QueryParserException;

public class FCSQueryParser implements SRUQueryParser<QueryNode> {
    private static final String PARAM_QUERY = "query";
    private static final List<String> QUERY_PARAMETER_NAMES =
            Collections.unmodifiableList(Arrays.asList(PARAM_QUERY));
    private final QueryParser parser = new QueryParser();


    @Override
    public String getQueryType() {
        return Constants.FCS_QUERY_TYPE_FCS;
    }


    @Override
    public boolean supportsVersion(SRUVersion version) {
        if (version == null) {
            throw new NullPointerException("version == null");
        }
        /*
         * CQL is supported by all SRU versions ...
         */
        return version.compareTo(SRUVersion.VERSION_2_0) >= 0;
    }


    @Override
    public String getQueryTypeDefintion() {
        return null;
    }


    @Override
    public List<String> getQueryParameterNames() {
        return QUERY_PARAMETER_NAMES;
    }


    @Override
    public SRUQuery<QueryNode> parseQuery(SRUVersion version,
            Map<String, String> parameters, SRUDiagnosticList diagnostics) {
        FCSQuery result = null;

        final String rawQuery = parameters.get(PARAM_QUERY);
        if (rawQuery == null) {
            diagnostics.addDiagnostic(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    null, "no query passed to query parser");
            return null;
        }

        try {
            QueryNode parsedQuery = parser.parse(rawQuery);
            result = new FCSQuery(rawQuery, parsedQuery);
        } catch (QueryParserException e) {
            diagnostics.addDiagnostic(Constants.FCS_DIAGNOSTIC_GENERAL_QUERY_SYNTAX_ERROR,
                    null, e.getMessage());
        } catch (Exception e) {
            diagnostics.addDiagnostic(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    null, "Unexpected error while parsing query.");
        }
        return result;
    }


    public static final class FCSQuery extends SRUQueryBase<QueryNode> {

        private FCSQuery(String rawQuery, QueryNode parsedQuery) {
            super(rawQuery, parsedQuery);
        }


        @Override
        public String getQueryType() {
            return Constants.FCS_QUERY_TYPE_FCS;
        }
    }

} // class FCSQueryParser
