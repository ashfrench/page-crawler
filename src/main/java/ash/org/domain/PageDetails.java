package ash.org.domain;

import java.util.Set;

public class PageDetails {
    private final Set<String> assets;
    private final Set<String> links;

    public PageDetails(Set<String> links, Set<String> assets) {
        this.links = links;
        this.assets = assets;
    }

    public Set<String> getAssets() {
        return assets;
    }

    public Set<String> getLinks() {
        return links;
    }

    @Override
    public String toString() {
        return "PageDetails{" +
                "assets=" + assets +
                ", links=" + links +
                '}';
    }
}
