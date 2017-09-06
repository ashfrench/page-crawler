package ash.org;

import ash.org.crawler.Crawler;
import ash.org.domain.Site;

import java.io.IOException;
import java.net.URL;

public class App {
    public static void main(String[] args) throws IOException {


        Crawler crawler = new Crawler(5000);

        if(args.length > 0) {
            for (String arg : args) {
                crawl(crawler, arg);
            }
        } else {
            crawl(crawler, "http://tomblomfield.com/");
        }
    }

    private static void crawl(Crawler crawler, String url) throws IOException {
        Site site = crawler.crawlDomain(new URL(url));
        System.out.println(site);
    }
}
