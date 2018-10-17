package sample;

public class Comment {

    private String id;
    private String user;
    private String commentContent;
    private String creationDate;
    private String title;
    private Integer idTransformed;

    public String getUser() {
        return user;
    }

    public String getCommentContent() {
        return commentContent;
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
}
