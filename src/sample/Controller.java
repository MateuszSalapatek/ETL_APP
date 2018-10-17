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
import org.jsoup.nodes.Element;
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
    ArrayList<Comment> extractedCommentsList;
    ArrayList<Comment> transformedCommentsList;

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

            Elements pageContent = doc.getElementsByClass("filmPage");

            while (doc.getElementsByClass("topics-list").select(".filmCategory").size() > 0) {  // page iterator

                Elements fbComments = doc.getElementsByClass("filmCategory"); //data dodania

                for (Element filmCategory : fbComments) {
                    Comment commentObject = new Comment();

                    commentObject.setId(filmCategory.id());  //id
                    commentObject.setCreationDate(filmCategory.select(".topicInfo .cap").html()); //date
                    commentObject.setUser(filmCategory.getElementsByClass("userNameLink").html()); //user
                    commentObject.setCommentContent(filmCategory.getElementsByClass("text").html()); //comment
                    commentObject.setTitle(pageContent.select(".hdr h1 a").html()); //film tittle

                    if(commentObject.getCommentContent().equals("")){
                        commentObject.setCommentContent(filmCategory.getElementsByClass("italic").html());
                    }
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
            pstmt = conn.prepareStatement("INSERT INTO COMMENTS VALUES (?,?,?,?,?,SYSDATE)");
            pstmt.setString(1, comment.getId());
            pstmt.setString(2, comment.getUser());
            pstmt.setString(3, comment.getCommentContent());
            pstmt.setString(4, comment.getCreationDate());
            pstmt.setString(5, comment.getTitle());
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
        ArrayList<Comment> extractedList = Controller.this.getComments();
        ArrayList<Comment> tranformedList = Controller.this.transformComments(extractedList);
        for (int i = 0; i < tranformedList.size(); i++) {
            Controller.this.loadCommentToDB(tranformedList.get(i));
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ETL procedure");
        alert.setHeaderText("ETL procedure finished successfully");
        alert.setContentText("Quantity of extracted comments: " + extractedList.size()+"\n"+
                "Quantity of loaded comments: " + tranformedList.size());
        alert.showAndWait();
    }

    @FXML
    private void clickExtractButton (ActionEvent event) throws SQLException {
        extractedCommentsList = Controller.this.getComments();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Extract procedure");
        alert.setHeaderText("Extract procedure finished successfully");
        alert.setContentText("Quantity of extracted comments: " + extractedCommentsList.size());
        alert.showAndWait();

        bTransform.setDisable(false);
        bExtract.setDisable(true);
    }

    @FXML
    private void clickTransformButton (ActionEvent event) throws SQLException {
        if(!(extractedCommentsList.size()>0)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Transform procedure");
            alert.setHeaderText("Transform is not possible, because no data hes been extracted");
            alert.showAndWait();
        }else{
            transformedCommentsList = transformComments(extractedCommentsList);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Transform procedure");
            alert.setHeaderText("Transform procedure finished successfully");
            alert.showAndWait();
        }
        bTransform.setDisable(true);
        bLoad.setDisable(false);
    }

    @FXML
    private void clickLoadButton (ActionEvent event) throws SQLException {
        if(!(transformedCommentsList.size()>0)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Load procedure");
            alert.setHeaderText("Load is not possible, because no data found after transforming");
            alert.showAndWait();
        }else{
            for (int i = 0; i < transformedCommentsList.size(); i++) {
                Controller.this.loadCommentToDB(transformedCommentsList.get(i));
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Load procedure");
            alert.setHeaderText("Load procedure finished successfully");
            alert.setContentText("Quantity of loaded comments: " + transformedCommentsList.size());
            alert.showAndWait();
        }
        transformedCommentsList.clear();
        extractedCommentsList.clear();
        bTransform.setDisable(true);
        bLoad.setDisable(true);
        bExtract.setDisable(false);
    }
}
