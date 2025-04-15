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
package eu.clarin.sru.server.fcs.parser_lex;

import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

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

import eu.clarin.sru.fcs.qlparser.LexLexer;
import eu.clarin.sru.fcs.qlparser.LexParser;
import eu.clarin.sru.fcs.qlparser.LexParserBaseVisitor;
import eu.clarin.sru.fcs.qlparser.LexParser.Boolean_modifiedContext;
import eu.clarin.sru.fcs.qlparser.LexParser.Boolean_queryContext;
import eu.clarin.sru.fcs.qlparser.LexParser.IndexContext;
import eu.clarin.sru.fcs.qlparser.LexParser.ModifierContext;
import eu.clarin.sru.fcs.qlparser.LexParser.Modifier_listContext;
import eu.clarin.sru.fcs.qlparser.LexParser.Modifier_relationContext;
import eu.clarin.sru.fcs.qlparser.LexParser.QueryContext;
import eu.clarin.sru.fcs.qlparser.LexParser.R_booleanContext;
import eu.clarin.sru.fcs.qlparser.LexParser.RelationContext;
import eu.clarin.sru.fcs.qlparser.LexParser.Relation_modifiedContext;
import eu.clarin.sru.fcs.qlparser.LexParser.Search_clauseContext;
import eu.clarin.sru.fcs.qlparser.LexParser.Search_termContext;
import eu.clarin.sru.fcs.qlparser.LexParser.SubqueryContext;

/**
 * A LexCQL query parser that produces LexCQL expression trees.
 */
public class QueryParser {
    private static final int DEFAULT_INITIAL_STACK_SIZE = 32;
    private static final Logger logger = LoggerFactory.getLogger(ExpressionTreeBuilder.class);

    /**
     * Constructor.
     */
    public QueryParser() {
    }

    /**
     * Parse query.
     *
     * @param query the LexCQL query
     * @return a LexCQL expression tree
     * @throws QueryParserException if an error occurred
     */
    public QueryNode parse(String query) throws QueryParserException {
        final ErrorListener errorListener = new ErrorListener(query);
        try {
            CharStream input = CharStreams.fromString(query);
            LexLexer lexer = new LexLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LexParser parser = new LexParser(tokens);
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
            if (parser.isMatchedEOF() && !errorListener.hasErrors() && (parser.getNumberOfSyntaxErrors() == 0)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("ANTLR parse tree: {}", tree.toStringTree(parser));
                }
                ExpressionTreeBuilder v = new ExpressionTreeBuilder(DEFAULT_INITIAL_STACK_SIZE);
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
                String message = errorListener.hasErrors() ? errorListener.getErrors().get(0) : "unspecified error";
                throw new QueryParserException(message);
            }
        } catch (ExpressionTreeBuilderException e) {
            throw new QueryParserException(e.getMessage(), e.getCause());
        } catch (QueryParserException e) {
            throw e;
        } catch (Throwable t) {
            throw new QueryParserException("an unexpected exception occured while parsing", t);
        }
    }

    /*
     * hide the expression tree builder implementation in nested private class ...
     */
    private final class ExpressionTreeBuilder
            extends LexParserBaseVisitor<Void> {
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
            this.stack = new ArrayDeque<>(initStackSize);
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

            if (logger.isTraceEnabled()) {
                logger.trace("visitQuery/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitBoolean_query(Boolean_queryContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitBoolean_query/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            final int pos = stack.size();
            super.visitBoolean_query(ctx);
            if (stack.size() > pos) {
                if ((stack.size() - pos) == 1) {
                    QueryNode node = (QueryNode) stack.pop();
                    stack.push(node);
                } else {
                    final List<Object> children = new ArrayList<>();
                    while (stack.size() > pos) {
                        children.add(0, stack.pop());
                    }

                    // build tree
                    QueryNode node = (QueryNode) children.remove(0);
                    while (children.size() >= 2) {
                        RBoolean rBoolean = (RBoolean) children.remove(0);
                        QueryNode other = (QueryNode) children.remove(0);
                        node = new SearchClauseGroup(node, rBoolean, other);
                    }
                    if (!children.isEmpty()) {
                        throw new ExpressionTreeBuilderException(
                                "visitBoolean_query children length does not match into tree structure!");
                    }
                    // "return" the tree
                    stack.push(node);
                }
            } else {
                throw new ExpressionTreeBuilderException("visitBoolean_query is empty!");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("visitBoolean_query/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitSubquery(SubqueryContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitSubquery/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            super.visitSubquery(ctx);
            if (ctx.boolean_query() != null) {
                QueryNode node = (QueryNode) stack.pop();
                stack.push(new Subquery(node, true));
            } else if (ctx.search_clause() != null) {
                SearchClause searchClause = (SearchClause) stack.pop();
                stack.push(searchClause);
            }

            if (logger.isTraceEnabled()) {
                logger.trace("visitSubquery/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitBoolean_modified(Boolean_modifiedContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitBoolean_modified/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            R_booleanContext b_ctx = ctx.r_boolean();
            if (b_ctx.AND() != null) {
                stack.push(RBoolean.AND);
            } else if (b_ctx.OR() != null) {
                stack.push(RBoolean.OR);
            } else if (b_ctx.NOT() != null) {
                stack.push(RBoolean.NOT);
            } else {
                throw new ExpressionTreeBuilderException("invalid boolean for boolean_modified: " + b_ctx.getText());
            }

            if (ctx.modifier_list() != null) {
                throw new ExpressionTreeBuilderException(
                        "boolean_modified does not support modifiers on booleans in LexCQL: "
                                + ctx.modifier_list().getText());
            }

            if (logger.isTraceEnabled()) {
                logger.trace("visitBoolean_modified/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitSearch_clause(Search_clauseContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitSearch_clause/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }
            super.visitSearch_clause(ctx);

            String searchTerm = (String) stack.pop();
            Relation relation = null;
            String index = null;
            if (ctx.index() != null) {
                relation = (Relation) stack.pop();
                index = (String) stack.pop();
            }

            stack.push(new SearchClause(index, relation, searchTerm));

            if (logger.isTraceEnabled()) {
                logger.trace("visitSearch_clause/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitSearch_term(Search_termContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitSearch_term/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            final String searchTerm;
            if (ctx.SIMPLE_STRING() != null) {
                searchTerm = ctx.SIMPLE_STRING().getSymbol().getText();
            } else if (ctx.QUOTED_STRING() != null) {
                searchTerm = unquoteString(ctx.QUOTED_STRING().getSymbol().getText(), buf);
            } else {
                throw new ExpressionTreeBuilderException("Invalid state in visitSearch_term! No string?");
            }
            stack.push(searchTerm);

            if (logger.isTraceEnabled()) {
                logger.trace("visitSearch_term/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitIndex(IndexContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitIndex/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            String name = ctx.getChild(0).getText();
            stack.push(name);

            if (logger.isTraceEnabled()) {
                logger.trace("visitIndex/exit: stack={}", stack);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Void visitRelation_modified(Relation_modifiedContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitRelation_modified/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            super.visitRelation_modified(ctx);

            final Modifier_listContext m_ctx = ctx.getChild(Modifier_listContext.class, 0);

            List<Modifier> modifiers = null;
            if (m_ctx != null) {
                modifiers = (List<Modifier>) stack.pop();
            }
            String name = (String) stack.pop();
            stack.push(new Relation(name, modifiers));

            if (logger.isTraceEnabled()) {
                logger.trace("visitRelation_modified/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitRelation(RelationContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitRelation/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            // TODO: validate supported relations only?
            String relation = ctx.getText();
            stack.push(relation);

            if (logger.isTraceEnabled()) {
                logger.trace("visitRelation/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitModifier_list(Modifier_listContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitModifier_list/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            final int pos = stack.size();
            super.visitModifier_list(ctx);
            if (stack.size() > pos) {
                final List<Modifier> modifiers = new ArrayList<>();
                while (stack.size() > pos) {
                    modifiers.add(0, (Modifier) stack.pop());
                }
                stack.push(modifiers);
            } else {
                throw new ExpressionTreeBuilderException("visitModifier_list is empty!");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("visitModifier_list/exit: stack={}", stack);
            }
            return null;
        }

        @Override
        public Void visitModifier(ModifierContext ctx) {
            if (logger.isTraceEnabled()) {
                logger.trace("visitModifier/enter: children={} / cnt={} / text={}",
                        ctx.children,
                        ctx.getChildCount(),
                        ctx.getText());
            }

            String name = ctx.modifier_name().simple_name().SIMPLE_STRING().getSymbol().getText();

            String relation = null;
            String value = null;
            Modifier_relationContext r_ctx = ctx.modifier_relation();
            if (r_ctx != null) {
                relation = r_ctx.relation_symbol().getText();
                value = r_ctx.modifier_value().SIMPLE_STRING().getSymbol().getText();
            }

            stack.push(new Modifier(name, relation, value));

            if (logger.isTraceEnabled()) {
                logger.trace("visitModifier/exit: stack={}", stack);
            }
            return null;
        }

    }

    private static int getChildIndex(ParserRuleContext ctx, int tokenType, int start) {
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

    private static String unquoteString(String s, char[] buf) {
        // strip quotes
        if (s.startsWith("\"")) {
            if (s.endsWith("\"")) {
                s = s.substring(1, s.length() - 1);
            } else {
                throw new ExpressionTreeBuilderException("value not properly quoted; invalid closing quote");
            }
        } else {
            throw new ExpressionTreeBuilderException("value not properly quoted; expected \" (double quote) character");
        }

        // unescape characters
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
                    case '"': /* double quote */
                        sb.append("\"");
                        break;
                    default:
                        throw new ExpressionTreeBuilderException(
                                "invalid escape sequence: \\" + new String(Character.toChars(cp)));

                }
            } else {
                try {
                    final int len = Character.toChars(cp, buf, 0);
                    sb.append(buf, 0, len);
                } catch (IllegalArgumentException e) {
                    throw new ExpressionTreeBuilderException(
                            "invalid codepoint: U+" + Integer.toHexString(cp).toUpperCase());
                }
            }
        }

        return sb.toString();
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
                errors = new ArrayList<>();
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
