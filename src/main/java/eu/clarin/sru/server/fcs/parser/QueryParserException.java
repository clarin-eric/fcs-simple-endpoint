package eu.clarin.sru.server.fcs.parser;

@SuppressWarnings("serial")
public class QueryParserException extends Exception {
    public QueryParserException(String message, Throwable cause) {
        super(message, cause);
    }


    public QueryParserException(String message) {
        this(message, null);
    }

}
