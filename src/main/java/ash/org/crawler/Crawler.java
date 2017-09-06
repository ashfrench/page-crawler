package ash.org.crawler;

import ash.org.domain.Page;
import ash.org.domain.PageDetails;
import ash.org.domain.Site;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Crawler {

    private final HTMLScraper HTMLScraper;

    public Crawler(int timeout) {
        this(new HTMLScraper(timeout));
    }

    public Crawler(HTMLScraper HTMLScraper){
        this.HTMLScraper = HTMLScraper;
    }

    public Site crawlDomain(URL url) throws IOException {
        Objects.requireNonNull(url, "You must supply a url to crawl");
        String host = url.getHost();
        String urlString = url.toString();
        if(!urlString.endsWith(host) && !urlString.endsWith(host + "/") ){
            throw new IllegalArgumentException("Must give main domain as the url to crawl");
        }

        Page homePage = getPage(url);

        Set<String> homePageLinks = homePage.getPageDetails().getLinks();
        System.out.println(homePageLinks);

        Set<Page> allPages = getPagesFromHomePage(homePage);

        return new Site(url, allPages);
    }

    private Set<Page> getPagesFromHomePage(Page homePage) throws IOException {
        Set<Page> pages = new HashSet<>();
        pages.add(homePage);
        return getPages(homePage, pages);
    }

    private Set<Page> getPages(Page page, Set<Page> visitedPages) throws IOException {
        Set<String> links = page.getPageDetails().getLinks();
        for (String link : links) {
            URL url = page.getUrl();
            String host = url.getHost();
            String protocol = url.getProtocol();

            URL linkURL = new URL(protocol + "://" + host + link);
            if (!visitedPages.contains(new Page(linkURL))){
                Page linkPage = getPage(linkURL);
                visitedPages.add(linkPage);
                visitedPages.addAll(getPages(linkPage, visitedPages));
            }
        }

        return visitedPages;
    }

    private Page getPage(URL url) throws IOException {
        Objects.requireNonNull(url, "You must supply a url to crawl");
        System.out.println("Getting Links for URL: " + url);
        PageDetails pageDetails = HTMLScraper.getPageAssets(url);

        return new Page(url, pageDetails);
    }

}
