package eu.clarin.sru.server.fcs;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import eu.clarin.sru.fcs.qlparser.FCSLexer;
import eu.clarin.sru.fcs.qlparser.FCSParser;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUQuery;
import eu.clarin.sru.server.SRUQueryBase;
import eu.clarin.sru.server.SRUQueryParser;
import eu.clarin.sru.server.SRUVersion;

public class FCSQueryParser implements SRUQueryParser<ParseTree> {
    private static final String PARAM_QUERY = "query";
    private static final List<String> QUERY_PARAMETER_NAMES =
            Collections.unmodifiableList(Arrays.asList(PARAM_QUERY));

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
    public SRUQuery<ParseTree> parseQuery(SRUVersion version,
            Map<String, String> parameters, SRUDiagnosticList diagnostics) {

        final String rawQuery = parameters.get(PARAM_QUERY);
        if (rawQuery == null) {
            diagnostics.addDiagnostic(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    null, "no query passed to query parser");
            return null;
        }

        try {
            ANTLRInputStream input = new ANTLRInputStream(
                    new ByteArrayInputStream(
                            rawQuery.getBytes("UTF-8")));
            FCSLexer lexer = new FCSLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FCSParser parser = new FCSParser(tokens);
            return new FCSQuery(rawQuery, parser.query());
        } catch (Exception e) {
            diagnostics.addDiagnostic(SRUConstants.SRU_QUERY_SYNTAX_ERROR,
                    null, "error parsing query");
        }
        return null;
    }


    public static final class FCSQuery extends SRUQueryBase<ParseTree> {


        private FCSQuery(String rawQuery, ParseTree parsedQuery) {
            super(rawQuery, parsedQuery);
        }


        @Override
        public String getQueryType() {
            return Constants.FCS_QUERY_TYPE_FCS;
        }
    }

} // class FCSQueryParser
