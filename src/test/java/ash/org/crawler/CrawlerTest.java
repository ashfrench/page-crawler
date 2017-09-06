package ash.org.crawler;

import ash.org.domain.Page;
import ash.org.domain.PageDetails;
import ash.org.domain.Site;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerTest {

    @Mock
    private HTMLScraper htmlScraper;

    private Crawler crawler;

    @Before
    public void testSetup(){
        crawler = new Crawler(htmlScraper);
    }

    public void tearDown(){
        verifyNoMoreInteractions(htmlScraper);
    }

    @Test
    public void crawlDomain() throws Exception {
        PageDetails homePage = new PageDetails(Sets.newHashSet("/", "/hello1"), new HashSet<>());
        PageDetails hello1 = new PageDetails(Sets.newHashSet("/", "/hello2"), new HashSet<>());
        PageDetails hello2 = new PageDetails(Sets.newHashSet("/", "/hello1"), new HashSet<>());

        when(htmlScraper.getPageAssets(any())).thenReturn(homePage, hello1, hello2);

        String google = "http://www.google.com";
        URL url = new URL(google + "/");
        Site site = crawler.crawlDomain(url);
        Site expectedSite = new Site(url, Sets.newHashSet(new Page(url, homePage), new Page(new URL(google + "/hello1"), hello1), new Page(new URL(google + "/hello2"), hello2)));
        assertEquals(expectedSite, site);

        verify(htmlScraper, times(3)).getPageAssets(any());
    }

    @Test
    public void testCrawlDomainNoLinks() throws Exception {
        PageDetails homePage = new PageDetails(Sets.newHashSet("/"), new HashSet<>());

        when(htmlScraper.getPageAssets(any())).thenReturn(homePage);

        String google = "http://www.google.com";
        URL url = new URL(google + "/");
        Site site = crawler.crawlDomain(url);
        Site expectedSite = new Site(url, Sets.newHashSet(new Page(url, homePage)));
        assertEquals(expectedSite, site);
        assertEquals(expectedSite.hashCode(), site.hashCode());

        verify(htmlScraper).getPageAssets(any());
    }

    @Test
    public void testSiteAssets() throws IOException {
        PageDetails homePage = new PageDetails(Sets.newHashSet("/"), Sets.newHashSet("/Stuff.js"));

        when(htmlScraper.getPageAssets(any())).thenReturn(homePage);

        String google = "http://www.google.com";
        URL url = new URL(google + "/");
        Site site = crawler.crawlDomain(url);
        Site expectedSite = new Site(url, Sets.newHashSet(new Page(url, homePage)));
        assertEquals(expectedSite, site);

        verify(htmlScraper).getPageAssets(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrawlSubDomain() throws IOException {
        crawler.crawlDomain(new URL("https://www.google.com/sub"));
    }

    @Test(expected = NullPointerException.class)
    public void testCrawlNull() throws IOException {
        crawler.crawlDomain(null);
    }

}