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
package eu.clarin.sru.server.fcs.parser;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
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


/**
 * A FCS-QL query parser that produces FCS-QL expression trees.
 */
public class QueryParser {
    private static final int[] REP_ZERO_OR_MORE =
            new int[] { 0, QueryNode.OCCURS_UNBOUNDED };
    private static final int[] REP_ONE_OR_MORE =
            new int[] { 1, QueryNode.OCCURS_UNBOUNDED };
    private static final int[] REP_ZERO_OR_ONE =
            new int[] { 0, 1 };
    private static final String EMPTY_STRING = "";
    private static final int DEFAULT_INITIAL_STACK_SIZE = 32;
    private static final Logger logger =
            LoggerFactory.getLogger(ExpressionTreeBuilder.class);
    private static final String DEFAULT_IDENTIFIER = "text";
    private static final Operator DEFAULT_OPERATOR = Operator.EQUALS;
    private static final Normalizer.Form DEFAULT_UNICODE_NORMALIZATION_FORM =
            Normalizer.Form.NFC;
    private final String defaultIdentifier;
    private final Operator defaultOperator;
    private final Normalizer.Form unicodeNormalizationForm;


    /**
     * Constructor.
     */
    public QueryParser() {
        this(DEFAULT_IDENTIFIER, DEFAULT_UNICODE_NORMALIZATION_FORM);
    }


    /**
     * Constructor.
     *
     * @param defaultIdentifier
     *            the default identifier to be used for simple expressions
     */
    public QueryParser(String defaultIdentifier) {
        this.defaultIdentifier = defaultIdentifier;
        this.defaultOperator = DEFAULT_OPERATOR;
        this.unicodeNormalizationForm = DEFAULT_UNICODE_NORMALIZATION_FORM;
    }


    /**
     * Constructor.
     *
     * @param defaultIdentifier
     *            the default identifier to be used for simple expressions
     * @param unicodeNormaliztionForm
     *            the Unicode normalization form to be used or <code>null</code>
     *            to not perform normalization
     */
    public QueryParser(String defaultIdentifier,
            Normalizer.Form unicodeNormaliztionForm) {
        this.defaultIdentifier = defaultIdentifier;
        this.defaultOperator = DEFAULT_OPERATOR;
        this.unicodeNormalizationForm = unicodeNormaliztionForm;
    }


    /**
     * Parse query.
     *
     * @param query
     *            the FCS-QL query
     * @return a FCS-QL expression tree
     * @throws QueryParserException
     *             if an error occurred
     */
    public QueryNode parse(String query) throws QueryParserException {
        final ErrorListener errorListener = new ErrorListener(query);
        try {
//            ANTLRInputStream input = new ANTLRInputStream(query);
            CharStream input = CharStreams.fromString(query);
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
        } catch (QueryParserException e) {
            throw e;
        } catch (Throwable t) {
            throw new QueryParserException(
                    "an unexpected exception occured while parsing", t);
        }
    }


    /*
     * hide the expression tree builder implementation in nested private class ...
     */
    private final class ExpressionTreeBuilder
            extends FCSParserBaseVisitor<Void> {
        private final Deque<Object> stack;
        /*
         * pre-allocate buffer to store chars returned by Character.toChars in
         * unescapeString()
         */
        private final char[] buf = new char[2];


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

            // handle optional qualifier
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

            /* process escape sequences, if present */
            if (regex.indexOf('\\') != -1) {
                regex = unescapeString(regex, buf);
            }

            /* perform unicode normalization, if requested */
            if (unicodeNormalizationForm != null) {
                regex = Normalizer.normalize(regex, unicodeNormalizationForm);
            }

            // FIXME: validate regex?
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
                        flags.add(RegexFlag.CASE_INSENSITIVE);
                        break;
                    case 'I':
                        /* $FALL-THROUGH$ */
                    case 'C':
                        flags.add(RegexFlag.CASE_SENSITIVE);
                        break;
                    case 'l':
                        flags.add(RegexFlag.LITERAL_MATCHING);
                        break;
                    case 'd':
                        flags.add(RegexFlag.IGNORE_DIACRITICS);
                        break;
                    default:
                        throw new ExpressionTreeBuilderException(
                                "unknown regex modifier flag: " + s.charAt(i));
                    } // switch
                }

                // validate regex flags
                if (flags.contains(RegexFlag.CASE_SENSITIVE) &&
                        flags.contains(RegexFlag.CASE_INSENSITIVE)) {
                    throw new ExpressionTreeBuilderException(
                            "invalid combination of regex modifier flags: " +
                            "'i' or 'c' and 'I' or 'C' are mutually exclusive");
                }
                if (flags.contains(RegexFlag.LITERAL_MATCHING) &&
                            (flags.contains(RegexFlag.CASE_SENSITIVE) ||
                             flags.contains(RegexFlag.CASE_INSENSITIVE) ||
                             flags.contains(RegexFlag.IGNORE_DIACRITICS))) {
                    throw new ExpressionTreeBuilderException(
                            "invalid combination of regex modifier flags: 'l' " +
                            "is mutually exclusive with 'i', 'c', 'I', 'C' or 'd'");
                }

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
        int max = QueryNode.OCCURS_UNBOUNDED;
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
        if ((max != QueryNode.OCCURS_UNBOUNDED) && (min > max)) {
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
                        "value not properly quoted; invalid closing quote");
            }
        } else if (s.startsWith("'")) {
            if (s.endsWith("'")) {
                s = s.substring(1, s.length() - 1);
            } else {
                throw new ExpressionTreeBuilderException(
                        "value not properly quoted; invalid closing quote");
            }
        } else {
            throw new ExpressionTreeBuilderException(
                    "value not properly quoted; expected \" (double quote) " +
                            "or ' (single qoute) character");
        }
        return s;
    }


    private static String unescapeString(String s, char[] buf) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int cp = s.codePointAt(i);
            if (cp == '\\') {
                i++; // skip slash
                cp = s.codePointAt(i);

                switch (cp) {
                case '\\': /* slash */
                    sb.append("\\");
                    break;
                case '"':   /* double quote */
                    sb.append("\"");
                    break;
                case '\'':  /* single quote */
                    sb.append("'");
                    break;
                case 'n':   /* new line */
                    sb.append("\n");
                    break;
                case 't':   /* tabulator */
                    sb.append("\t");
                    break;
                case '.':   /* regex: dot */
                    sb.append("\\.");
                    break;
                case '^':   /* regex: caret */
                    sb.append("\\^");
                    break;
                case '$':   /* regex: dollar */
                    sb.append("\\$");
                    break;
                case '*':   /* regex: asterisk */
                    sb.append("\\*");
                    break;
                case '+':   /* regex: plus */
                    sb.append("\\+");
                    break;
                case '?':   /* regex: question mark */
                    sb.append("\\?");
                    break;
                case '(':   /* regex: opening parenthesis */
                    sb.append("\\(");
                    break;
                case ')':   /* regex: closing parenthesis */
                    sb.append("\\)");
                    break;
                case '{':   /* regex: opening curly brace */
                    sb.append("\\{");
                    break;
                case '[':   /* regex: opening square bracket */
                    sb.append("\\[");
                    break;
                case '|':   /* regex: vertical bar */
                    sb.append("\\|");
                    break;
                case 'x':   /* x HEX HEX */
                    i = unescapeUnicode(s, i, 2, sb, buf);
                    break;
                case 'u':   /* u HEX HEX HEX HEX */
                    i = unescapeUnicode(s, i, 4, sb, buf);
                    break;
                case 'U':   /* U HEX HEX HEX HEX HEX HEX HEX HEX */
                    i = unescapeUnicode(s, i, 8, sb, buf);
                    break;
                default:
                    throw new ExpressionTreeBuilderException(
                            "invalid escape sequence: \\" +
                                    new String(Character.toChars(cp)));
                }
            } else {
                try {
                    final int len = Character.toChars(cp, buf, 0);
                    sb.append(buf, 0, len);
                } catch (IllegalArgumentException e) {
                    throw new ExpressionTreeBuilderException(
                            "invalid codepoint: U+" +
                                    Integer.toHexString(cp).toUpperCase());
                }
            }
        }
        return sb.toString();
    }


    private static final int unescapeUnicode(String s, int i, int size,
            StringBuilder sb, char[] buf) {
        if ((s.length() - i - 1) >= size) {
            int cp = 0;
            for (int j = 0; j < size; j++) {
                i++;
                if (j > 0) {
                    cp = cp << 4;
                }
                cp |= parseHexString(s.codePointAt(i));
            }
            try {
                final int len = Character.toChars(cp, buf, 0);
                sb.append(buf, 0, len);
            } catch (IllegalArgumentException e) {
                throw new ExpressionTreeBuilderException(
                        "invalid codepoint: U+" +
                                Integer.toHexString(cp).toUpperCase());
            }
            return i;
        } else {
            throw new ExpressionTreeBuilderException(
                    "truncated escape sequence: \\" + s.substring(i));
        }
    }


    private static final int parseHexString(int c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        case 'a':
            /* FALL-THROUGH */
        case 'A':
            return 10;
        case 'b':
            /* FALL-THROUGH */
        case 'B':
            return 11;
        case 'c':
            /* FALL-THROUGH */
        case 'C':
            return 12;
        case 'd':
            /* FALL-THROUGH */
        case 'D':
            return 13;
        case 'e':
            /* FALL-THROUGH */
        case 'E':
            return 14;
        case 'f':
            /* FALL-THROUGH */
        case 'F':
            return 15;
        default:
            /*
             * actually, this should never happen, as ANTLR's lexer should catch
             * illegal HEX characters
             */
            throw new ExpressionTreeBuilderException(
                    "invalud hex character: " +
                            new String(Character.toChars(c)));
        }
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
            if (logger.isDebugEnabled()) {
                if (offendingSymbol instanceof Token) {
                    final Token t = (Token) offendingSymbol;
                    int pos = t.getStartIndex();
                    if (pos != -1) {
                        StringBuilder x = new StringBuilder();
                        while (pos-- > 0) {
                            x.append(" ");
                        }
                        x.append("^- ").append(msg);
                        logger.debug("query: {}", query);
                        logger.debug("       {}", x.toString());
                    }
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
