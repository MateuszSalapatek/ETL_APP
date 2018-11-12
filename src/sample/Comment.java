package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import static sample.OracleConn.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
    private String commentAnswersCountTransformed;
    private String commentAnswersLastUser;
    public String commentAnswersLastDate;
    public String commentLink;

    public void setCommentLink(String commentLink) {
        this.commentLink = commentLink;
    }

    public String getCommentLink() {
        return commentLink;
    }

    public String getUser() {
        return user;
    }

    public String getCommentAnswersLastDate() { return commentAnswersLastDate; }

    public String getCommentAnswersLastUser() { return commentAnswersLastUser; }

    public String getCommentAnswersCount() { return commentAnswersCount; }

    public String getCommentAnswersCountTransformed() { return commentAnswersCountTransformed; }

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

    public void setCommentAnswersCountTransformed(String commentAnswersCountTransformed) { this.commentAnswersCountTransformed = commentAnswersCountTransformed; }

    public void setCommentAnswersLastUser(String commentAnswersLastUser) { this.commentAnswersLastUser = commentAnswersLastUser; }

    public void setCommentAnswersLastDate(String commentAnswersLastDate) { this.commentAnswersLastDate = commentAnswersLastDate; }

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
                                            "       FILMTIME," +
                                            "       COMMENTRATE,\n" +
                                            "       COMMENTANSWERSCOUNT,\n" +
                                            "       COMMENTANSWERSLASTUSER,\n" +
                                            "       COMMENTANSWERSLASTDATE FROM COMMENTS");
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
                com.setCommentRate(rs.getString(10));
                com.setCommentAnswersCountTransformed(rs.getString(11));
                com.setCommentAnswersLastUser((rs.getString(12)));
                com.setCommentAnswersLastDate(rs.getString(13));
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

    public ObservableList<Comment> getViewCommentWithConditions(String author, String commentTittle, String commentContent, String filmRate,
                                                                String creationDate, String filmTittle, String filmYear, String filmTime,
                                                                String commentRate, String commentAnswersCount, String commentAnswersLasUser,
                                                                String commentAnswersLastDate) throws SQLException {
        ObservableList<Comment> commentViewList = FXCollections.observableArrayList();
        stat = conn.createStatement();
        try {
            ResultSet rs = stat.executeQuery("SELECT\n" +
                                            "    id,\n" +
                                            "    author,\n" +
                                            "    commenttitle,\n" +
                                            "    commentcontent,\n" +
                                            "    filmrate,\n" +
                                            "    creationdate,\n" +
                                            "    filmtittle,\n" +
                                            "    filmyear,\n" +
                                            "    filmtime,\n" +
                                            "    commentrate,\n" +
                                            "    commentanswerscount,\n" +
                                            "    commentanswerslastuser,\n" +
                                            "    commentanswerslastdate\n" +
                                            "FROM comments\n" +
                                            "WHERE upper(nvl(author,'x0x0x')) LIKE '"+author+"%'\n" +
                                            "AND upper(nvl(COMMENTTITLE,'x0x0x')) LIKE '"+commentTittle+"%'\n" +
                                            "AND upper(nvl(COMMENTCONTENT,'x0x0x')) LIKE '"+commentContent+"%'" +
                                            "AND upper(nvl(FILMRATE,'x0x0x'))  LIKE '"+filmRate+"%'\n" +
                                            "AND upper(nvl(CREATIONDATE,'x0x0x'))  LIKE '"+creationDate+"%'\n" +
                                            "AND upper(nvl(FILMTITTLE,'x0x0x'))  LIKE '"+filmTittle+"%'\n" +
                                            "AND upper(nvl(FILMYEAR,'00000')) LIKE '"+filmYear+"%'\n" +
                                            "AND upper(nvl(FILMTIME,'x0x0x'))  LIKE '"+filmTime+"%'\n" +
                                            "AND upper(nvl(COMMENTRATE,'x0x0x')) LIKE '"+commentRate+"%'\n" +
                                            "AND upper(nvl(COMMENTANSWERSCOUNT,'x0x0x'))  LIKE '"+commentAnswersCount+"%'\n" +
                                            "AND UPPER(nvl(COMMENTANSWERSLASTUSER,'x0x0x'))  LIKE '"+commentAnswersLasUser+"%'\n" +
                                            "AND UPPER(nvl(COMMENTANSWERSLASTDATE,'x0x0x'))  LIKE '"+commentAnswersLastDate+"%'"
            );
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
                com.setCommentRate(rs.getString(10));
                com.setCommentAnswersCountTransformed(rs.getString(11));
                com.setCommentAnswersLastUser((rs.getString(12)));
                com.setCommentAnswersLastDate(rs.getString(13));
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

    public ArrayList<String> getFilmsCommentsLinks() throws SQLException {
        stat = conn.createStatement();
        ArrayList<String> commentsLink = new ArrayList<>();
        try {
            ResultSet rs = stat.executeQuery("SELECT DISTINCT SUBSTR(COMMENTLINK, 1, REGEXP_INSTR(COMMENTLINK,'page=[[:digit:]]')+4 ) FROM COMMENTS");
            while (rs.next()) {
                commentsLink.add(rs.getString(1));
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
        return commentsLink;
    }
}
