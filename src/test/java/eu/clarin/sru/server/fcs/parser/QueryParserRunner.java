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
package eu.clarin.sru.server.fcs.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryParserRunner {
    private static final Logger logger = LoggerFactory
            .getLogger(QueryParserRunner.class);


    public static void main(String[] args) {
        if ((args != null) && (args.length > 0)) {
            File file = new File(args[0]);
            if (file.exists() || file.isFile()) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) {
                            continue;
                        }
                        try {
                            logger.info("PARSING QUERY: >>>{}<<<", line);
                            QueryParser parser = new QueryParser();
                            QueryNode tree = parser.parse(line);
                            logger.info("{}", tree);
                            logger.info("... PARSED OK");
                        } catch (QueryParserException e) {
                            logger.error(
                                    "error parsing query: " + e.getMessage(),
                                    e);
                        }

                    }
                } catch (IOException e) {
                    logger.error("error reading file", e);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            /* IGNORE */
                        }
                    }
                }
            } else {
                logger.error("DOES NOT EXIST OR IS NOT A FILE: {}", args[0]);
            }
        } else {
            logger.error("NEED QUERY-FILE");
        }
    }

    static {
        org.apache.log4j.BasicConfigurator
                .configure(new org.apache.log4j.ConsoleAppender(
                        new org.apache.log4j.PatternLayout("%-5p [%t] %m%n"),
                        org.apache.log4j.ConsoleAppender.SYSTEM_ERR));
        org.apache.log4j.Logger logger = org.apache.log4j.Logger
                .getRootLogger();
        logger.setLevel(org.apache.log4j.Level.INFO);
        logger.getLoggerRepository().getLogger("eu.clarin")
                .setLevel(org.apache.log4j.Level.DEBUG);
    }

}
