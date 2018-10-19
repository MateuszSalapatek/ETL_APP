package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import static sample.OracleConn.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static sample.OracleConn.stat;

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
    private String commentRate;
    private String commentAnswersCount;
    private Integer commentAnswersCountTransformed;

    public String getUser() {
        return user;
    }

    public String getCommentAnswersCount() { return commentAnswersCount; }

    public Integer getCommentAnswersCountTransformed() { return commentAnswersCountTransformed; }

    public String getCommentRate() { return commentRate; }

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

    public void setCommentRate(String commentRate) { this.commentRate = commentRate; }

    public void setCommentAnswersCount(String commentAnswersCount) { this.commentAnswersCount = commentAnswersCount; }

    public void setCommentAnswersCountTransformed(Integer commentAnswersCountTransformed) { this.commentAnswersCountTransformed = commentAnswersCountTransformed; }

    @Override
    public String toString() {
        return title +"  "+id;
    }

    public ObservableList<Comment> getViewComment() throws SQLException {
        ObservableList<Comment> commentViewList = FXCollections.observableArrayList();
        stat = conn.createStatement();
        try {
            ResultSet rs = stat.executeQuery("SELECT ID,\n" +
                                            "       AUTHOR,\n" +
                                            "       COMMENTTITLE,\n" +
                                            "       COMMENTCONTENT,\n" +
                                            "       FILMRATE,\n" +
                                            "       CREATIONDATE,\n" +
                                            "       FILMTITTLE,\n" +
                                            "       FILMYEAR,\n" +
                                            "       FILMTIME FROM COMMENTS");
            while (rs.next()) {
                Comment com = new Comment();
                com.setIdTransformed(rs.getInt(1));
                com.setUser(rs.getString(2));
                com.setCommentTitle(rs.getString(3));
                com.setCommentContent(rs.getString(4));
                com.setFilmRateTransformed(rs.getString(5));
                com.setCreationDate(rs.getString(6));
                com.setTitle(rs.getString(7));
                com.setFilmYearTransformed(rs.getInt(8));
                com.setFilmTimeTransformed(rs.getString(9));
                commentViewList.add(com);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } finally{
            try{
                if(stat!=null)
                    stat.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return commentViewList;
    }
}
