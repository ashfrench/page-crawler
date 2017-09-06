package ash.org.domain;

import java.net.URL;
import java.util.Objects;
import java.util.Set;

public class Site {

    private final URL domain;
    private final Set<Page> pages;

    public Site(URL domain, Set<Page> pages) {
        this.domain = domain;
        this.pages = pages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Site site = (Site) o;
        return Objects.equals(domain, site.domain) &&
                Objects.equals(pages, site.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, pages);
    }

    @Override
    public String toString() {
        return "Site{" +
                "domain='" + domain + '\'' +
                ", pages=" + pages +
                '}';
    }

}
