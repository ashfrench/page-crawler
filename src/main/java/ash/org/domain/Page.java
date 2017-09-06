package ash.org.domain;

import java.net.URL;
import java.util.HashSet;
import java.util.Objects;

public class Page {

    private final URL url;
    private final PageDetails pageDetails;

    public Page(URL url){
        this(url, new PageDetails(new HashSet<>(), new HashSet<>()));
    }

    public Page(URL url, PageDetails pageDetails) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(pageDetails);
        this.url = url;
        this.pageDetails = pageDetails;
    }

    public URL getUrl() {
        return url;
    }

    public PageDetails getPageDetails() {
        return pageDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(url.toString(), page.url.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }


    @Override
    public String toString() {
        return "Page{" +
                "url=" + url +
                ", pageDetails=" + pageDetails +
                '}';
    }
}
