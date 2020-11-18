package olx_utils;

public class Author {

    private String name;
    private String url;

    public Author(String name, String url) {

        this.name = name;
        this.url = url;
    }

    public Author() {}

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {

        return name + " - " + url;
    }
}
