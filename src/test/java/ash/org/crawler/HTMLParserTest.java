package ash.org.crawler;

import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class HTMLParserTest {

    @Test(expected = SocketTimeoutException.class)
    public void testTimeout() throws IOException {
        HTMLParser htmlParser = new HTMLParser(1);
        htmlParser.parse(new URL("http://www.google.com"));
    }

    @Test
    public void testParse() throws IOException {
        HTMLParser htmlParser = new HTMLParser(10000);
        Document parse = htmlParser.parse(new URL("http://www.google.com"));
        assertNotNull(parse);
        assertNotNull(parse.body());
    }

    @Test(expected = NullPointerException.class)
    public void testNullParse() throws IOException {
        HTMLParser htmlParser = new HTMLParser(1);
        htmlParser.parse(null);
    }

}