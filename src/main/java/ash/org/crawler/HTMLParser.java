package ash.org.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

//Helper class to allow mocking of static method
public class HTMLParser {

    private final int timeout;

    public HTMLParser(int timeout) {
        this.timeout = timeout;
    }

    public Document parse(URL url) throws IOException {
        Objects.requireNonNull(url);
        return Jsoup.parse(url, timeout);
    }
}
