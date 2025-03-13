/**
 * This software is copyright (c) 2013-2025 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *  - Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * @copyright Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.parser_lex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LexCQLQueryParserRunner {
    private static final Logger logger = LoggerFactory.getLogger(LexCQLQueryParserRunner.class);

    public static void main(String[] args) {
        String line = "test is /bad=worse apple or (\"not\" and lemma = hi)";
        try {
            logger.info("PARSING QUERY: >>>{}<<<", line);
            QueryParser parser = new QueryParser();
            QueryNode tree = parser.parse(line);
            logger.info("{}", tree);
            logger.info("... PARSED OK");
        } catch (QueryParserException e) {
            logger.error("error parsing query: " + e.getMessage(), e);
        }

    }

    static {
        org.apache.log4j.BasicConfigurator
                .configure(new org.apache.log4j.ConsoleAppender(
                        new org.apache.log4j.PatternLayout("%-5p [%t] %m%n"),
                        org.apache.log4j.ConsoleAppender.SYSTEM_ERR));
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
        logger.setLevel(org.apache.log4j.Level.INFO);
        logger.getLoggerRepository().getLogger("eu.clarin")
                .setLevel(org.apache.log4j.Level.DEBUG);
    }

}
