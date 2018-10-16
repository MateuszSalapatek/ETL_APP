package sample;

public class Comment {

    private Integer id;
    private String user;
    private String commentContent;
    private String creationDate;

    public String getUser() {
        return user;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public Integer getId() {
        return id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
