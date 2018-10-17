package sample;

public class Comment {

    private String id;
    private String user;
    private String commentContent;
    private String creationDate;
    private String title;
    private Integer idTransformed;
    private String commentTitle;
    private String filmRate;
    private String filmRateTransformed;
    private String filmYear;
    private Integer filmYearTransformed;
    private String filmTime;
    private String filmTimeTransformed;

    public String getUser() {
        return user;
    }

    public String getFilmTimeTransformed() {
        return filmTimeTransformed;
    }

    public String getFilmTime() {
        return filmTime;
    }

    public Integer getFilmYearTransformed() {
        return filmYearTransformed;
    }

    public String getFilmYear() {
        return filmYear;
    }

    public String getFilmRateTransformed() {
        return filmRateTransformed;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public String getFilmRate() {
        return filmRate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getId() {
        return id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getIdTransformed() {
        return idTransformed;
    }

    public String getCommentTitle() {
        return commentTitle;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIdTransformed(Integer idTransformed) {
        this.idTransformed = idTransformed;
    }

    public void setCommentTitle(String commentTitle) {
        this.commentTitle = commentTitle;
    }

    public void setFilmRate(String filmRate) {
        this.filmRate = filmRate;
    }

    public void setFilmYear(String filmYear) {
        this.filmYear = filmYear;
    }

    public void setFilmRateTransformed(String filmRateTransformed) {
        this.filmRateTransformed = filmRateTransformed;
    }

    public void setFilmYearTransformed(Integer filmYearTransformed) {
        this.filmYearTransformed = filmYearTransformed;
    }

    public void setFilmTime(String filmTime) {
        this.filmTime = filmTime;
    }

    public void setFilmTimeTransformed(String filmTitleTransformed) {
        this.filmTimeTransformed = filmTitleTransformed;
    }
}
