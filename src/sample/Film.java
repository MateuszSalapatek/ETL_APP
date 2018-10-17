package sample;

public class Film {
    private String tittle;
    private String url;

    public String getTittle() {
        return tittle;
    }

    public String getUrl() {
        return url;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return url;
    }

}
