package eu.clarin.sru.server.fcs.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.fcs.qlparser.FCSLexer;
import eu.clarin.sru.fcs.qlparser.FCSParser;
import eu.clarin.sru.fcs.qlparser.FCSParserBaseVisitor;
import eu.clarin.sru.fcs.qlparser.FCSParser.AttributeContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Expression_andContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Expression_basicContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Expression_groupContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Expression_notContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Expression_orContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.IdentifierContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Main_queryContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.QualifierContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.QuantifierContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.QueryContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Query_disjunctionContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Query_groupContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Query_implicitContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Query_segmentContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Query_sequenceContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Query_simpleContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.RegexpContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Regexp_flagContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Regexp_patternContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Within_partContext;
import eu.clarin.sru.fcs.qlparser.FCSParser.Within_part_simpleContext;


public class QueryParser {
    private static final int[] REP_ZERO_OR_MORE =
            new int[] { 0, Constants.OCCURS_UNBOUNDED };
    private static final int[] REP_ONE_OR_MORE =
            new int[] { 1, Constants.OCCURS_UNBOUNDED };
    private static final int[] REP_ZERO_OR_ONE =
            new int[] { 0, 1 };
    private static final String EMPTY_STRING = "";
    private static final int DEFAULT_INITIAL_STACK_SIZE = 32;
    private static final Logger logger =
            LoggerFactory.getLogger(ExpressionTreeBuilder.class);
    private static final String DEFAULT_IDENTIFIER = "text";
    private static final Operator DEFAULT_OPERATOR = Operator.EQUALS;
    private final String defaultIdentifier;
    private final Operator defaultOperator;


    public QueryParser() {
        this(DEFAULT_IDENTIFIER);
    }


    public QueryParser(String defaultIdentifier) {
        this.defaultIdentifier = defaultIdentifier;
        this.defaultOperator = DEFAULT_OPERATOR;
    }


    public QueryNode parse(String query) throws QueryParserException {
        final ErrorListener errorListener = new ErrorListener(query);
        try {
            ANTLRInputStream input = new ANTLRInputStream(query);
            FCSLexer lexer = new FCSLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FCSParser parser = new FCSParser(tokens);
            /*
             * clear (possible) default error listeners and set our own!
             */
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            /*
             * commence parsing ...
             */
            ParseTree tree = parser.query();
            if (parser.isMatchedEOF() &&
                    !errorListener.hasErrors() &&
                    (parser.getNumberOfSyntaxErrors() == 0)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("ANTLR parse tree: {}",
                            tree.toStringTree(parser));
                }
                ExpressionTreeBuilder v =
                        new ExpressionTreeBuilder(DEFAULT_INITIAL_STACK_SIZE);
                v.visit(tree);
                return (QueryNode) v.stack.pop();
            } else {
                if (logger.isTraceEnabled()) {
                    for (String msg : errorListener.getErrors()) {
                        logger.trace("ERROR: {}", msg);
                    }
                }

                /*
                 * FIXME: (include additional error information)
                 */
                String message = errorListener.hasErrors()
                        ? errorListener.getErrors().get(0)
                        : "unspecified error";
                throw new QueryParserException(message);
            }
        } catch (ExpressionTreeBuilderException e) {
            throw new QueryParserException(e.getMessage(), e.getCause());
        } catch (Throwable t) {
            throw new QueryParserException(
                    "an unexpcected occured exception while parsing", t);
        }
    }


    /*
     * hide the expression tree builder implementation in nested private class ...
     */
    private final class ExpressionTreeBuilder
            extends FCSParserBaseVisitor<Void> {
        private final Deque<Object> stack;


        private ExpressionTreeBuilder(int initStackSize) {
            if (initStackSize < 1) {
                throw new IllegalArgumentException("initStackSize < 1");
            }
            this.stack = new ArrayDeque<Object>(initStackSize);
        }


        @Override
        public Void visitQuery(QueryContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            super.visitQuery(ctx);

            Within_partContext w_ctx = ctx.getChild(Within_partContext.class, 0);
            if (w_ctx != null) {
                QueryNode within = (QueryNode) stack.pop();
                QueryNode query  = (QueryNode) stack.pop();;
                stack.push(new QueryWithWithin(query, within));
            }
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitMain_query(Main_queryContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitMain_query/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            super.visitMain_query(ctx);
            if (logger.isTraceEnabled()) {
                logger.trace("visitMain_query/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitQuery_disjunction(Query_disjunctionContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_disjunction/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            final int pos = stack.size();
            super.visitQuery_disjunction(ctx);
            if (stack.size() > pos) {
                List<QueryNode> items = new ArrayList<QueryNode>();
                while (stack.size() > pos) {
                    items.add(0, (QueryNode) stack.pop());
                }
                stack.push(new QueryDisjunction(items));
            } else {
                throw new ExpressionTreeBuilderException(
                        "visitQuery_disjunction is empty!");
            }
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_disjunction/exit: stack={}", stack);
            }
            return null;
        }



        @Override
        public Void visitQuery_sequence(Query_sequenceContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_sequence/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            final int pos = stack.size();
            super.visitQuery_sequence(ctx);
            if (stack.size() > pos) {
                List<QueryNode> items = new ArrayList<QueryNode>();
                while (stack.size() > pos) {
                    items.add(0, (QueryNode) stack.pop());
                }
                stack.push(new QuerySequence(items));
            } else {
                throw new ExpressionTreeBuilderException(
                        "visitQuery_sequence is empty!");
            }
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_sequence/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitQuery_group(Query_groupContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_group/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            super.visitQuery_group(ctx);

            /*
             * handle repetition (if any)
             */
            int min = 1;
            int max = 1;

            // fetch *first* child of type QuantifierContext, therefore idx=0
            final QuantifierContext q_ctx =
                    ctx.getChild(QuantifierContext.class, 0);
            if (q_ctx != null) {
                int[] r = processRepetition(q_ctx);
                min = r[0];
                max = r[1];
            }

            QueryNode content = (QueryNode) stack.pop();
            stack.push(new QueryGroup(content, min, max));
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_group/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitQuery_simple(Query_simpleContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_simple/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            super.visitQuery_simple(ctx);

            /*
             * handle repetition (if any)
             */
            int min = 1;
            int max = 1;

            // fetch *first* child of type QuantifierContext, therefore idx=0
            final QuantifierContext q_ctx =
                    ctx.getChild(QuantifierContext.class, 0);
            if (q_ctx != null) {
                int[] r = processRepetition(q_ctx);
                min = r[0];
                max = r[1];
            }

            QueryNode expression = (QueryNode) stack.pop();
            stack.push(new QuerySegment(expression, min, max));
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_simple/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitQuery_implicit(Query_implicitContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_implicit/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            stack.push(defaultOperator);
            stack.push(defaultIdentifier);
            stack.push(EMPTY_STRING);
            super.visitQuery_implicit(ctx);

            @SuppressWarnings("unchecked")
            Set<RegexFlag> regex_flags = (Set<RegexFlag>) stack.pop();
            String regex_value         = (String) stack.pop();
            String qualifier           = (String) stack.pop();
            String identifier          = (String) stack.pop();
            Operator operator          = (Operator) stack.pop();

            Expression exp = new Expression(qualifier, identifier, operator,
                    regex_value, regex_flags);
            stack.push(exp);
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_implicit/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitQuery_segment(Query_segmentContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_segment/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            /*
             * if the context contains only two children, they must be
             * '[' and ']' thus we are dealing with a wildcard segment
             */
            if (ctx.getChildCount() == 2) {
                stack.push(new ExpressionWildcard());
            } else {
                super.visitQuery_segment(ctx);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery_segment/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitExpression_basic(Expression_basicContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_basic/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            final Token tok_op = ((TerminalNode) ctx.getChild(1)).getSymbol();
            switch (tok_op.getType()) {
            case FCSLexer.OPERATOR_EQ:
                stack.push(Operator.EQUALS);
                break;
            case FCSLexer.OPERATOR_NE:
                stack.push(Operator.NOT_EQUALS);
                break;
            default:
                throw new ExpressionTreeBuilderException(
                        "invalid operator type: " + tok_op.getText());
            }
            super.visitExpression_basic(ctx);

            @SuppressWarnings("unchecked")
            Set<RegexFlag> regex_flags = (Set<RegexFlag>) stack.pop();
            String regex_value         = (String) stack.pop();
            String qualifier           = (String) stack.pop();
            String identifer           = (String) stack.pop();
            Operator operator          = (Operator) stack.pop();

            Expression exp = new Expression(qualifier, identifer, operator,
                    regex_value, regex_flags);
            stack.push(exp);
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_basic/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitExpression_not(Expression_notContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_not/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            super.visitExpression_not(ctx);

            QueryNode expression = (QueryNode) stack.pop();
            stack.push(new ExpressionNot(expression));
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_not/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitExpression_group(Expression_groupContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_group/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            super.visitExpression_group(ctx);

            QueryNode expression = (QueryNode) stack.pop();
            stack.push( new ExpressionGroup(expression));
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_group/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitExpression_or(Expression_orContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_or/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            final int pos = stack.size();
            super.visitExpression_or(ctx);
            if (stack.size() > pos) {
                final List<QueryNode> children = new ArrayList<QueryNode>();
                while (stack.size() > pos) {
                    children.add(0, (QueryNode) stack.pop());
                }
                stack.push( new ExpressionOr(children));
            } else {
                throw new ExpressionTreeBuilderException(
                        "visitExpression_or is empty!");
            }
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_or/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitExpression_and(Expression_andContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_and/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            final int pos = stack.size();
            super.visitExpression_and(ctx);
            if (stack.size() > pos) {
                final List<QueryNode> children = new ArrayList<QueryNode>();
                while (stack.size() > pos) {
                    children.add(0, (QueryNode) stack.pop());
                }
                stack.push(new ExpressionAnd(children));
            } else {
                throw new ExpressionTreeBuilderException(
                        "visitExpression_and is empty!");
            }
            if (logger.isTraceEnabled()) {
                logger.trace("visitExpression_and/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitAttribute(AttributeContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitAttribute/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            // check for optional qualifier
            QualifierContext q_ctx = ctx.getChild(QualifierContext.class, 0);
            String qualifier = (q_ctx != null) ? q_ctx.getText() : EMPTY_STRING;

            stack.push(ctx.getChild(IdentifierContext.class, 0).getText());
            stack.push(qualifier);
            if (logger.isTraceEnabled()) {
                logger.trace("visitAttribute/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitRegexp(RegexpContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitRegexp/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            final Regexp_patternContext p_ctx =
                    ctx.getChild(Regexp_patternContext.class, 0);
            String regex = stripQuotes(p_ctx.getText());
            // FIXME: validate regex?
            // FIXME: translate unicode escape sequences! (if they ever work)
            stack.push(regex);

            // handle regex flags, if any
            final Regexp_flagContext f_ctx =
                    ctx.getChild(Regexp_flagContext.class, 0);
            if (f_ctx != null) {
                final String s = f_ctx.getText();

                Set<RegexFlag> flags = new HashSet<RegexFlag>();
                for (int i = 0; i < s.length(); i++) {
                    switch (s.charAt(i)) {
                    case 'i':
                        /* $FALL-THROUGH$ */
                    case 'c':
                        flags.add(RegexFlag.CASE_INSENSITVE);
                        break;
                    case 'I':
                        /* $FALL-THROUGH$ */
                    case 'C':
                        flags.add(RegexFlag.CASE_SENSITVE);
                        break;
                    case 'l':
                        flags.add(RegexFlag.LITERAL_MATCHING);
                    case 'd':
                        flags.add(RegexFlag.IGNORE_DIACRITICS);
                        break;
                    default:
                        throw new ExpressionTreeBuilderException(
                                "unexpected regex flag: " + s.charAt(i));
                    } // switch
                }
                // FIXME: validate flags? most combinations are mutually exclusive
                stack.push(flags);
            } else {
                // regex without flags, so push 'empty' flags on stack
                stack.push(Collections.emptySet());
            }
            if (logger.isTraceEnabled()) {
                logger.trace("visitRegexp/exit: stack={}", stack);
            }
            return null;
        }


        @Override
        public Void visitWithin_part_simple(Within_part_simpleContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("Within_part_simpleContext/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            SimpleWithin.Scope scope = null;
            String s = ctx.getChild(0).getText();
            if ("sentence".equals(s) || "s".equals(s)) {
                scope = SimpleWithin.Scope.SENTENCE;
            } else if ("utterance".equals(s) || "u".equals(s)) {
                scope = SimpleWithin.Scope.UTTERANCE;
            } else if ("paragraph".equals(s) || "p".equals(s)) {
                scope = SimpleWithin.Scope.PARAGRAPH;
            } else if ("turn".equals(s)|| "t".equals(s)) {
                scope = SimpleWithin.Scope.TURN;
            } else if ("text".equals(s)) {
                scope = SimpleWithin.Scope.TEXT;
            } else if ("session".equals(s)) {
                scope = SimpleWithin.Scope.SESSION;
            } else {
                throw new ExpressionTreeBuilderException(
                        "invalid scope for simple 'within' clause: " + s);
            }
            stack.push(new SimpleWithin(scope));
            if (logger.isTraceEnabled()) {
                logger.trace("Within_part_simpleContext/exit: stack={}", stack);
            }
            return null;
        }
    }


    private static int[] processRepetition(QuantifierContext ctx) {
        final Token tok =
                ctx.getChild(TerminalNode.class, 0).getSymbol();
        switch (tok.getType()) {
        case FCSParser.Q_ZERO_OR_MORE: /* "*" */
            return REP_ZERO_OR_MORE;
        case FCSParser.Q_ONE_OR_MORE:  /* "+" */
            return REP_ONE_OR_MORE;
        case FCSParser.Q_ZERO_OR_ONE:  /* "?" */
            return REP_ZERO_OR_ONE;
        case FCSParser.L_CURLY_BRACKET: /* "{x, y}" variants */
            return processRepetition2(ctx);
        default:
            throw new ExpressionTreeBuilderException(
                    "unexpected symbol in repetition quantifier: " +
                            tok.getText());
        } // switch
    }


    private static int[] processRepetition2(QuantifierContext ctx) {
        int commaIdx = getChildIndex(ctx, FCSParser.Q_COMMA, 0);
        int int1Idx = getChildIndex(ctx, FCSParser.INTEGER, 0);
        int int2Idx = getChildIndex(ctx, FCSParser.INTEGER, int1Idx + 1);
        int min = 0;
        int max = Constants.OCCURS_UNBOUNDED;
        if (commaIdx != -1) {
            if (int1Idx < commaIdx) {
                min = parseInteger(ctx.getChild(int1Idx).getText());
            }
            if (commaIdx < int1Idx) {
                max = parseInteger(ctx.getChild(int1Idx).getText());
            } else if (commaIdx < int2Idx) {
                max = parseInteger(ctx.getChild(int2Idx).getText());
            }
        } else {
            if (int1Idx == -1) {
                throw new ExpressionTreeBuilderException("int1Idx == -1");
            }
            min = parseInteger(ctx.getChild(int1Idx).getText());
            max = min;
        }
        if ((max != Constants.OCCURS_UNBOUNDED) && min > max) {
            throw new ExpressionTreeBuilderException(
                    "bad qualifier: min > max (" + min + " > " + max + ")");
        }
        return new int[] { min, max };
    }


    private static int getChildIndex(ParserRuleContext ctx,
            int tokenType, int start) {
        if ((start >= 0) && (start < ctx.getChildCount())) {
            for (int i = start; i < ctx.getChildCount(); i++) {
                final ParseTree o = ctx.getChild(i);
                if (o instanceof TerminalNode) {
                    if (((TerminalNode) o).getSymbol().getType() == tokenType) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }


    private static int parseInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new ExpressionTreeBuilderException(
                    "invalid integer: " + s, e);
        }
    }


    private static String stripQuotes(String s) {
        if (s.startsWith("\"")) {
            if (s.endsWith("\"")) {
                s = s.substring(1, s.length() - 1);
            } else {
                throw new ExpressionTreeBuilderException(
                        "value not properly quoted; wrong closing quote");
            }
        } else if (s.startsWith("'")) {
            if (s.endsWith("'")) {
                s = s.substring(1, s.length() - 1);
            } else {
                throw new ExpressionTreeBuilderException(
                        "value not properly quoted; wrong closing quote");
            }
        } else {
            throw new ExpressionTreeBuilderException(
                    "value not properly quoted; expected \" (double quote) " +
                            "or ' (singe qoute) character");
        }
        return s;
    }


    private static final class ErrorListener extends BaseErrorListener {
        private final String query;
        private List<String> errors = null;


        private ErrorListener(String query) {
            this.query = query;
        }


        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                Object offendingSymbol, int line, int charPositionInLine,
                String msg, RecognitionException e) {
            if (errors == null) {
                errors = new ArrayList<String>();
            }

            /*
             * FIXME: additional information of error should not be logged
             * but added to the list of errors; that list probably needs
             * to be enhanced to store supplementary information
             * Furthermore, a sophisticated errorlist implementation could
             * also be used by the QueryVistor to add addition query error
             * information
             */
            if (offendingSymbol instanceof Token) {
                final Token t = (Token) offendingSymbol;
                int pos = t.getStartIndex();
                if (pos != -1) {
                    StringBuilder x = new StringBuilder();
                    while (pos-- > 0) {
                        x.append(" ");
                    }
                    x.append("^- ").append(msg);
                    logger.error("query: {}", query);
                    logger.error("       {}", x.toString());
                }
            }

            errors.add(msg);
        }


        public boolean hasErrors() {
            return (errors != null) && !errors.isEmpty();
        }


        public List<String> getErrors() {
            if (errors != null) {
                return errors;
            } else {
                return Collections.emptyList();
            }
        }
    }


    @SuppressWarnings("serial")
    private static final class ExpressionTreeBuilderException
            extends RuntimeException {
        private ExpressionTreeBuilderException(String message,
                Throwable cause) {
            super(message, cause);
        }


        private ExpressionTreeBuilderException(String message) {
            this(message, null);
        }
    }

} // class QueryParser
