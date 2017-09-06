package ash.org.crawler;

import ash.org.domain.PageDetails;
import com.google.common.collect.Sets;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static ash.org.crawler.HTMLScraper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HTMLScraperTest {

    @Mock
    private HTMLParser htmlParser;
    @Mock
    private Document document;
    @Mock
    private Elements links;
    private List<Element> linksElements;
    @Mock
    private Elements css;
    private List<Element> cssElements;
    @Mock
    private Elements js;
    private List<Element> jsElements;
    @Mock
    private Elements images;
    private List<Element> imageElements;

    private HTMLScraper htmlScraper;
    private URL url;

    @Before
    public void setup() throws IOException {
        url = new URL("https://www.google.co.uk/");
        htmlScraper = new HTMLScraper(htmlParser);

        when(htmlParser.parse(any())).thenReturn(document);

        when(document.getElementsByTag(ANCHOR_TAG)).thenReturn(links);
        when(document.getElementsByTag(IMG_TAG)).thenReturn(images);
        when(document.getElementsByTag(SCRIPT_TAG)).thenReturn(js);
        when(document.getElementsByTag(LINK_TAG)).thenReturn(css);

        linksElements = new ArrayList<>();
        imageElements = new ArrayList<>();
        jsElements = new ArrayList<>();
        cssElements = new ArrayList<>();

        when(links.stream()).thenReturn(linksElements.stream());
        when(images.stream()).thenReturn(imageElements.stream());
        when(js.stream()).thenReturn(jsElements.stream());
        when(css.stream()).thenReturn(cssElements.stream());
    }

    @After
    public void tearDown() throws IOException {
        verify(htmlParser).parse(any());
        verify(document).getElementsByTag(ANCHOR_TAG);
        verify(document).getElementsByTag(IMG_TAG);
        verify(document).getElementsByTag(SCRIPT_TAG);
        verify(document).getElementsByTag(LINK_TAG);

        verify(links).stream();
        verify(images).stream();
        verify(js).stream();
        verify(css).stream();

        verifyNoMoreInteractions(htmlParser, document, links, css, js, images);
    }

    @Test
    public void testNoLinks() throws IOException {
        PageDetails pageAssets = htmlScraper.getPageAssets(url);
        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertTrue(assets.isEmpty());
    }

    @Test
    public void test1LinkNoFiltering() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink");
        assertEquals(expectedLinks, links);

        Element element = linkMocks.get(0);
        verify(element).attr(HREF_ATTR);
        verifyNoMoreInteractions(element);
    }

    @Test
    public void testMultipleLinksNoFiltering() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        Element element1 = linkMocks.get(0);
        Element element2 = linkMocks.get(1);

        verify(element1).attr(HREF_ATTR);
        verify(element2).attr(HREF_ATTR);
        verifyNoMoreInteractions(element1, element2);
    }

    @Test
    public void testMultipleLinksFilteringExternal() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2", "http://www.facebook.com");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksFilteringMultipleExternal() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "http://amazon.com","/validLink2", "http://www.facebook.com");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksFilteringMultipleInternal() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2", "/validLink2");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksFilteringFullyDomainLink() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2", "https://www.google.co.uk/validLink2");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksFilteringFullyDomainLinkWithDifferingProtocols() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2", "https://www.google.co.uk/validLink2", "http://www.google.co.uk/validLink2");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksStrippingOffQueryParams() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2?queryParam=123");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksStrippingOffQueryParamsDuplicate() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2?queryParam=123", "/validLink2");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksStrippingOffHash() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2#pageAnchor");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testMultipleLinksStrippingOffHashDuplicate() throws IOException {
        List<Element> linkMocks = getLinksElements("/validLink", "/validLink2#pageAnchor", "/validLink2");
        linksElements.addAll(linkMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertFalse(links.isEmpty());
        assertTrue(assets.isEmpty());

        Set<String> expectedLinks = Sets.newHashSet("/validLink", "/validLink2");
        assertEquals(expectedLinks, links);

        verifyLinkMocks(linkMocks);
    }

    @Test
    public void testJSAssets() throws IOException {
        List<Element> jsMocks = getJSElements("/internal.js");
        jsElements.addAll(jsMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.js"), assets);

        verifyJsMocks(jsMocks);
    }

    @Test
    public void testMultipleJSAssets() throws IOException {
        List<Element> jsMocks = getJSElements("/internal.js", "/internal1.js");
        jsElements.addAll(jsMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.js", "/internal1.js"), assets);

        verifyJsMocks(jsMocks);
    }

    @Test
    public void testExternalJSAssets() throws IOException {
        List<Element> jsMocks = getJSElements("/internal.js", "/internal1.js", "http://www.google.com/external.js");
        jsElements.addAll(jsMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.js", "/internal1.js", "http://www.google.com/external.js"), assets);

        verifyJsMocks(jsMocks);
    }

    @Test
    public void testMultipleExternalJSAssets() throws IOException {
        List<Element> jsMocks = getJSElements("/internal.js", "/internal1.js", "http://www.google.com/external.js", "http://www.google.com/external1.js");
        jsElements.addAll(jsMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.js", "/internal1.js", "http://www.google.com/external.js", "http://www.google.com/external1.js"), assets);

        verifyJsMocks(jsMocks);
    }

    @Test
    public void testJSElementsWithNoSource() throws IOException {
        List<Element> jsMocks = getJSElementsWithNoSrc(1);
        jsElements.addAll(jsMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertTrue(assets.isEmpty());

        verifyJsMocksWithNoSource(jsMocks);
    }

    @Test
    public void testJSMultipleElementsWithNoSource() throws IOException {
        List<Element> jsMocks = getJSElementsWithNoSrc(3);
        jsElements.addAll(jsMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertTrue(assets.isEmpty());

        verifyJsMocksWithNoSource(jsMocks);
    }

    @Test
    public void testJSMultipleElementsWithNoSourceAndMultipleWithSource() throws IOException {
        List<Element> jsMocksWithNoSource = getJSElementsWithNoSrc(3);
        List<Element> jsMocks = getJSElements("/internal.js", "/internal1.js", "http://www.google.com/external.js", "http://www.google.com/external1.js");
        jsElements.addAll(jsMocksWithNoSource);
        jsElements.addAll(jsMocks);
        Collections.shuffle(jsElements);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.js", "/internal1.js", "http://www.google.com/external.js", "http://www.google.com/external1.js"), assets);

        verifyJsMocks(jsMocks);
        verifyJsMocksWithNoSource(jsMocksWithNoSource);
    }

    @Test
    public void testCSSElements() throws IOException {
        List<Element> cssMocks = getCSSElements("/internal.css");
        cssElements.addAll(cssMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.css"), assets);

        verifyCSSMocks(cssMocks);
    }

    @Test
    public void testCSSExternalElements() throws IOException {
        List<Element> cssMocks = getCSSElements("/internal.css", "http://google.com/external.css");
        cssElements.addAll(cssMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.css", "http://google.com/external.css"), assets);

        verifyCSSMocks(cssMocks);
    }

    @Test
    public void testCSSElementsAndNonCSSType() throws IOException {
        List<Element> cssMocks = getCSSElements("/internal.css", "http://google.com/external.css");
        List<Element> nonCSSTypeMocks = getNonCssElement(1);
        cssElements.addAll(cssMocks);
        cssElements.addAll(nonCSSTypeMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.css", "http://google.com/external.css"), assets);

        verifyCSSMocks(cssMocks);
        verifyNoCssTypeMocks(nonCSSTypeMocks);
    }

    @Test
    public void testCSSElementsAndNoTypeAttr() throws IOException {
        List<Element> cssMocks = getCSSElements("/internal.css", "http://google.com/external.css");
        List<Element> noTypeAttr = getNonCssNoTypeAttr(1);
        cssElements.addAll(cssMocks);
        cssElements.addAll(noTypeAttr);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.css", "http://google.com/external.css"), assets);

        verifyCSSMocks(cssMocks);
        verifyCSSNoTypeAttr(noTypeAttr);
    }

    @Test
    public void testCSSElementsAndNonCSSTypeAndNoTypeAttr() throws IOException {
        List<Element> cssMocks = getCSSElements("/internal.css", "http://google.com/external.css");
        List<Element> nonCSSTypeMocks = getNonCssElement(2);
        List<Element> noTypeAttr = getNonCssNoTypeAttr(2);
        cssElements.addAll(cssMocks);
        cssElements.addAll(nonCSSTypeMocks);
        cssElements.addAll(noTypeAttr);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.css", "http://google.com/external.css"), assets);

        verifyCSSMocks(cssMocks);
        verifyNoCssTypeMocks(nonCSSTypeMocks);
        verifyCSSNoTypeAttr(noTypeAttr);
    }

    @Test
    public void testImageElements() throws IOException {
        List<Element> imgMocks = getImageElements("/internal.png");
        imageElements.addAll(imgMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.png"), assets);

        verifyImageMocks(imgMocks);
    }

    @Test
    public void testImageMultipleElements() throws IOException {
        List<Element> imgMocks = getImageElements("/internal.png", "/internal1.png");
        imageElements.addAll(imgMocks);

        PageDetails pageAssets = htmlScraper.getPageAssets(url);

        Set<String> links = pageAssets.getLinks();
        Set<String> assets = pageAssets.getAssets();

        assertTrue(links.isEmpty());
        assertFalse(assets.isEmpty());

        assertEquals(Sets.newHashSet("/internal.png", "/internal1.png"), assets);

        verifyImageMocks(imgMocks);
    }

    private List<Element> getImageElements(String... links){
        List<Element> elements = new ArrayList<>();
        for(String link : links){
            Element element = mock(Element.class);
            when(element.attr(SRC_ATTR)).thenReturn(link);
            elements.add(element);
        }
        return elements;
    }

    private void verifyImageMocks(List<Element> imageMocks) {
        for (Element linkMock : imageMocks) {
            verify(linkMock).attr(SRC_ATTR);
        }

        verifyNoMoreInteractions(imageMocks.toArray());
    }

    private void verifyLinkMocks(List<Element> linkMocks) {
        for (Element linkMock : linkMocks) {
            verify(linkMock).attr(HREF_ATTR);
        }

        verifyNoMoreInteractions(linkMocks.toArray());
    }

    private void verifyJsMocks(List<Element> jsMocks) {
        for (Element linkMock : jsMocks) {
            verify(linkMock).attr(SRC_ATTR);
            verify(linkMock).hasAttr(SRC_ATTR);
        }

        verifyNoMoreInteractions(jsMocks.toArray());
    }

    private void verifyJsMocksWithNoSource(List<Element> jsMocksWithNoSource) {
        for (Element linkMock : jsMocksWithNoSource) {
            verify(linkMock).hasAttr(SRC_ATTR);
        }

        verifyNoMoreInteractions(jsMocksWithNoSource.toArray());
    }

    private void verifyCSSMocks(List<Element> cssMocks) {
        for (Element cssMock : cssMocks) {
            verify(cssMock).hasAttr(TYPE_ATTR);
            verify(cssMock).attr(TYPE_ATTR);
            verify(cssMock).attr(HREF_ATTR);
        }
        verifyNoMoreInteractions(cssMocks.toArray());
    }

    private void verifyNoCssTypeMocks(List<Element> nonCSSTypeMocks) {
        for (Element cssMock : nonCSSTypeMocks) {
            verify(cssMock).hasAttr(TYPE_ATTR);
            verify(cssMock).attr(TYPE_ATTR);
        }

        verifyNoMoreInteractions(nonCSSTypeMocks.toArray());
    }

    private void verifyCSSNoTypeAttr(List<Element> noTypeAttr) {
        for (Element cssMock : noTypeAttr) {
            verify(cssMock).hasAttr(TYPE_ATTR);
        }
        verifyNoMoreInteractions(noTypeAttr.toArray());
    }

    private List<Element> getCSSElements(String... links){
        List<Element> elements = new ArrayList<>();
        for(String link : links){
            Element element = mock(Element.class);
            when(element.hasAttr(TYPE_ATTR)).thenReturn(true);
            when(element.attr(TYPE_ATTR)).thenReturn(TYPE_CSS);
            when(element.attr(HREF_ATTR)).thenReturn(link);
            elements.add(element);
        }
        return elements;
    }

    private List<Element> getNonCssElement(int count){
        List<Element> elements = new ArrayList<>();
        for(int x = 0; x < count; x++){
            Element element = mock(Element.class);
            when(element.hasAttr(TYPE_ATTR)).thenReturn(true);
            when(element.attr(TYPE_ATTR)).thenReturn("application/xml");
            elements.add(element);
        }
        return elements;
    }

    private List<Element> getNonCssNoTypeAttr(int count){
        List<Element> elements = new ArrayList<>();
        for(int x = 0; x < count; x++){
            Element element = mock(Element.class);
            when(element.hasAttr(TYPE_ATTR)).thenReturn(false);
            elements.add(element);
        }
        return elements;
    }

    private List<Element> getJSElements(String... links){
        List<Element> elements = new ArrayList<>();
        for(String link : links){
            Element element = mock(Element.class);
            when(element.hasAttr(SRC_ATTR)).thenReturn(true);
            when(element.attr(SRC_ATTR)).thenReturn(link);
            elements.add(element);
        }
        return elements;
    }

    private List<Element> getJSElementsWithNoSrc(int count){
        List<Element> elements = new ArrayList<>();
        for(int x = 0; x < count; x++){
            Element element = mock(Element.class);
            when(element.hasAttr(SRC_ATTR)).thenReturn(false);
            elements.add(element);
        }
        return elements;
    }

    private List<Element> getLinksElements(String... links){
        List<Element> elements = new ArrayList<>();
        for(String link : links){
            Element element = mock(Element.class);
            when(element.attr(HREF_ATTR)).thenReturn(link);
            elements.add(element);
        }
        return elements;
    }

}