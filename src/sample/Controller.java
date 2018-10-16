package sample;


import com.sun.net.httpserver.Authenticator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import static sample.OracleConn.conn;
import static sample.OracleConn.pstmt;
import static sample.OracleConn.stat;

public class Controller {

    private static String html = "https://www.filmweb.pl/Shrek/discussion?plusMinus=false&page=";

    @FXML
    private TextArea tAContentText;

    @FXML
    private void initialize() throws IOException, SQLException {
        OracleConn Oracle = new OracleConn();

        ArrayList<Comment> list = getComments();
        System.out.println(list.size());
        for (int i = 0; i<list.size(); i++){
            loadCommentToDB(list.get(i));
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zakończono procedurę ETL");
        alert.setHeaderText("Procedura ETL została zakończona pomyślnie");
        alert.showAndWait();

//            tAContentText.setText(tAContentText.getText()+ "Autor: "+ list.get(i).getUser() + "---- Komentarz: " + list.get(i).getCommentContent()+
//                    "---- Data utworzenia: "+ list.get(i).getCreationDate()+"\n");
//
//        }

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

    public Integer loadCommentToDB(Comment comment) throws SQLException {
        try {
            pstmt = conn.prepareStatement("INSERT INTO COMMENTS VALUES (id_seq.nextval,?,?,?,SYSDATE)");
//            pstmt.setInt(1, comment.getId());
            pstmt.setString(1, comment.getUser());
            pstmt.setString(2, comment.getCommentContent());
            pstmt.setString(3, comment.getCreationDate());
            pstmt.execute();
            pstmt.close();
            return 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            //duplicate id
            return 1;
        } catch (NullPointerException e){
            e.printStackTrace();
            return 2;
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
}
