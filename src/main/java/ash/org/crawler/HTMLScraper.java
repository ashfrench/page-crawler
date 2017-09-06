package ash.org.crawler;

import ash.org.domain.PageDetails;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class HTMLScraper {

    public static final String LINK_TAG = "link";
    public static final String IMG_TAG = "img";
    public static final String SCRIPT_TAG = "script";
    public static final String ANCHOR_TAG = "a";

    public static final String TYPE_ATTR = "type";
    public static final String HREF_ATTR = "href";
    public static final String SRC_ATTR = "src";

    public static final String HTTPS_PREFIX = "https://";
    public static final String HTTP_PREFIX = "http://";

    public static final String TYPE_CSS = "text/css";
    private final HTMLParser htmlParser;

    public HTMLScraper(int timeout){
        this(new HTMLParser(timeout));
    }

    public HTMLScraper(HTMLParser htmlParser){
        this.htmlParser = htmlParser;
    }


    public PageDetails getPageAssets(URL url) throws IOException {
        Document parse = htmlParser.parse(url);

        Set<String> links = getLinks(url, parse);
        Set<String> assets = getAssets(parse);
        return new PageDetails(links, assets);
    }

    private Set<String> getAssets(Document parse) {
        //Get CSS, Images, JS

        Set<String> assets = new HashSet<>();
        assets.addAll(getCssLinks(parse));
        assets.addAll(getImgLinks(parse));
        assets.addAll(getJavascript(parse));

        return assets;
    }

    private Set<String> getJavascript(Document parse) {
        return parse.getElementsByTag(SCRIPT_TAG).stream()
                .filter(element -> element.hasAttr(SRC_ATTR))
                .map(element -> element.attr(SRC_ATTR))
                .collect(toSet());
    }

    private Set<String> getImgLinks(Document parse) {
        return parse.getElementsByTag(IMG_TAG).stream()
                .map(element -> element.attr(SRC_ATTR))
                .collect(toSet());
    }

    private Set<String> getCssLinks(Document parse){
        return parse.getElementsByTag(LINK_TAG).stream()
                .filter(element -> element.hasAttr(TYPE_ATTR))
                .filter(element -> TYPE_CSS.equals(element.attr(TYPE_ATTR)))
                .map(element -> element.attr(HREF_ATTR))
                .collect(toSet());
    }

    private Set<String> getLinks(URL url, Document parse) {
        String host = url.getHost();
        Elements linkElements = parse.getElementsByTag(ANCHOR_TAG);

        return linkElements.stream()
                .map(link -> link.attr(HREF_ATTR))
                .filter(href -> checkInternalURl(url, href))
                .map(href -> replaceHrefHostName(href, host))
                .map(this::removeAnchoredLinks)
                .map(this::removeQueryParamtersInLinks)
                .filter(this::isNotHomePageLink)
                .collect(toSet());
    }

    private boolean isNotHomePageLink(String href){
        return !href.equals("/") && !href.equals("");
    }

    private String removeQueryParamtersInLinks(String href){
        return stringStripping(href, "?");
    }

    private String removeAnchoredLinks(String href) {
        return stringStripping(href, "#");
    }

    private String stringStripping(String href, String reference){
        if(href.contains(reference)){
            int i = href.indexOf(reference);
            return href.substring(0, i);
        } else {
            return href;
        }
    }

    private String replaceHrefHostName(String href, String host){
        return href.replace(HTTP_PREFIX + host, "")
                .replace(HTTPS_PREFIX + host, "");
    }

    private boolean checkInternalURl(URL url, String href) {
        String host = url.getHost();

        if(href.startsWith(HTTPS_PREFIX) || href.startsWith(HTTP_PREFIX)){
            if(!href.startsWith(HTTP_PREFIX + host) && !href.startsWith(HTTPS_PREFIX + host)){
                return false;
            }
        }

        return true;
    }
}
