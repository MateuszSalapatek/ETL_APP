package sample;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import static sample.OracleConn.conn;
import static sample.OracleConn.pstmt;

public class Controller {

    private static String html = "https://www.filmweb.pl/Shrek/discussion?plusMinus=false&page=";

    @FXML
    private Button bELT, bExtract, bTransform, bLoad;

    @FXML
    private void initialize() throws IOException, SQLException {
        OracleConn Oracle = new OracleConn();


        // TODO jak pobrać wartość id?
        // TODO niektórzy nie oceniają
        // TODO uzytkownik może być usunięty
        // TODO na dacie 0200 i 0100 to chyba strefy czasowe, więc będzie trzeba dodać przy transformie

    }
    public ArrayList<Comment> getComments() {

        ArrayList<Comment> commentsList = new ArrayList<Comment>();
        try {
            int pageIterator = 1;
            Document doc = Jsoup.connect(html + pageIterator).get();
            while (doc.getElementsByClass("topics-list").select(".filmCategory").size() > 0) {

                for (int i = 0; i < doc.getElementsByClass("topics-list").select(".filmCategory").size(); i++) {
                    Elements fbBody = doc.getElementsByClass("topics-list").select(".filmCategory");

                    Comment commentObject = new Comment();

                    commentObject.setCommentContent(fbBody.attr("id", "topic").get(i).select(".text").html()); //  comment
                    if ( commentObject.getCommentContent().equals("")){
                        commentObject.setCommentContent(fbBody.attr("id","topic").get(i).select(".italic").html()); //content dla niezgosności z regulaminem);
                    }
                    commentObject.setUser(fbBody.attr("id", "topic").get(i).select(".userNameLink").html()); // user
                    commentObject.setCreationDate(fbBody.attr("id", "topic").get(i).select(".topicInfo .cap").html()); // creation date

                    commentsList.add(commentObject);
                }
                pageIterator++;
                doc = Jsoup.connect(html + pageIterator).get();
            }
        } catch (ExportException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return commentsList;
    }

    public Boolean loadCommentToDB(Comment comment) throws SQLException {
        try {
            pstmt = conn.prepareStatement("INSERT INTO COMMENTS VALUES (id_seq.nextval,?,?,?,SYSDATE)");
//            pstmt.setInt(1, comment.getId());
            pstmt.setString(1, comment.getUser());
            pstmt.setString(2, comment.getCommentContent());
            pstmt.setString(3, comment.getCreationDate());
            pstmt.execute();
            pstmt.close();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            //duplicate id
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        finally{
            try{
                if(pstmt!=null)
                    pstmt.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    public ArrayList<Comment> transformComments(ArrayList<Comment> commentList){

        return commentList;
    }

    @FXML
    private void clickETLButton (ActionEvent event) throws SQLException {
        ArrayList<Comment> list = Controller.this.getComments();
        list = Controller.this.transformComments(list);
        for (int i = 0; i < list.size(); i++) {
            Controller.this.loadCommentToDB(list.get(i));
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zakończono procedurę ETL");
        alert.setHeaderText("Procedura ETL została zakończona pomyślnie");
        alert.showAndWait();
    }
}
