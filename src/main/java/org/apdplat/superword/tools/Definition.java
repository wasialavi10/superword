package org.apdplat.superword.tools;

import org.apache.commons.lang.StringUtils;
import org.apdplat.superword.tools.WordLinker.Dictionary;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Definition class retrieves word definitions from various online dictionaries.
 * It uses Jsoup library to parse HTML content.
 */
public class Definition {
    private static final Logger LOGGER = LoggerFactory.getLogger(Definition.class);

    // CSS paths for different dictionaries
    public static final String ICIBA_CSS_PATH = "ul.base-list li";
    public static final String YOUDAO_CSS_PATH = "div#phrsListTab.trans-wrapper.clearfix div.trans-container ul li" +
            " | div.trans-container ul p.wordGroup";
    // Add more CSS paths for other dictionaries here...

    // Constants for HTTP headers
    private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String ENCODING = "gzip, deflate";
    private static final String LANGUAGE = "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3";
    private static final String CONNECTION = "keep-alive";
    private static final String HOST = "www.iciba.com";
    private static final String REFERER = "http://www.iciba.com/";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:36.0) Gecko/20100101 Firefox/36.0";

    /**
     * Get a formatted definition string by joining the definitions with a separator.
     *
     * @param dictionary  The dictionary to use for definition retrieval.
     * @param word        The word for which the definition is needed.
     * @param joinString  The separator to join multiple definitions.
     * @return The formatted definition string.
     */
    public static String getDefinitionString(Dictionary dictionary, String word, String joinString) {
        return concat(getDefinition(dictionary, word), joinString);
    }

    // Other methods...

    public static void main(String[] args) {
        // Example usage:
        // getDefinitionForOXFORD("make").forEach(System.out::println);
        getDefinitionForWEBSTER("make").forEach(System.out::println);
    }
}
