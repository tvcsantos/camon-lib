package pt.unl.fct.di.tsantos.util.download;

public abstract class SearchResult {

    protected final String name;

    protected SearchResult() {
        this.name = null;
    }

    public SearchResult(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
