package sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import static sample.OracleConn.conn;
import static sample.OracleConn.pstmt;

public class Controller {

    private static String html = "https://www.filmweb.pl/Shrek/discussion?plusMinus=false&page=111";
    ArrayList<Comment> extractedCommentsList;
    ArrayList<Comment> transformedCommentsList;

    @FXML
    private Button bELT, bExtract, bTransform, bLoad;

    @FXML
    private void initialize() throws IOException, SQLException {
        OracleConn Oracle = new OracleConn();

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
            if (e.getMessage().equals("HTTP error fetching URL")){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Wrong page");
                alert.setHeaderText("The page for following URL not found");
                alert.showAndWait();
            }
        }
        return commentsList;
    }

    public Boolean loadCommentToDB(Comment comment) throws SQLException {
        try {
            pstmt = conn.prepareStatement("INSERT INTO COMMENTS VALUES (?,?,?,?,?,SYSDATE)");
            pstmt.setInt(1, comment.getIdTransformed());
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

        for ( int i = 0; i<commentList.size(); i++ ){
            commentList.get(i).setIdTransformed(Integer.parseInt(commentList.get(i).getId().replaceAll("[^0-9]", ""))); //to delete chars
        }

        return commentList;
    }

    @FXML
    private void clickETLButton (ActionEvent event) throws SQLException {
        ArrayList<Comment> extractedList = Controller.this.getComments();
        ArrayList<Comment> tranformedList = Controller.this.transformComments(extractedList);
        Integer deleteCounter = 0;
        for (int i = 0; i < tranformedList.size(); i++) {
            Boolean load = loadCommentToDB(tranformedList.get(i));
            if(!load){
                deleteCounter++;
            }
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ETL procedure");
        alert.setHeaderText("ETL procedure finished successfully");
        alert.setContentText("Quantity of extracted comments: " + extractedList.size()+"\n"+
                "Quantity of loaded comments: " + (tranformedList.size() - deleteCounter));
        alert.showAndWait();
    }

    @FXML
    private void clickExtractButton (ActionEvent event) throws SQLException {
        try {
            extractedCommentsList = Controller.this.getComments();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Extract procedure");
            alert.setHeaderText("Extract procedure finished successfully");
            alert.setContentText("Quantity of extracted comments: " + extractedCommentsList.size());
            alert.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
        }

        bTransform.setDisable(false);
        bExtract.setDisable(true);
    }

    @FXML
    private void clickTransformButton (ActionEvent event) throws SQLException {
        try {
            if (!(extractedCommentsList.size() > 0)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Transform procedure");
                alert.setHeaderText("Transform is not possible, because no data hes been extracted");
                alert.showAndWait();
                bTransform.setDisable(true);
                bExtract.setDisable(false);
                extractedCommentsList.clear();
                transformedCommentsList.clear();
            } else {
                transformedCommentsList = transformComments(extractedCommentsList);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Transform procedure");
                alert.setHeaderText("Transform procedure finished successfully");
                alert.showAndWait();
                bTransform.setDisable(true);
                bLoad.setDisable(false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void clickLoadButton (ActionEvent event) throws SQLException {
        try {
            if (!(transformedCommentsList.size() > 0)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Load procedure");
                alert.setHeaderText("Load is not possible, because no data found after transforming");
                alert.showAndWait();
            } else {
                Integer deleteCounter = 0;
                for (int i = 0; i < transformedCommentsList.size(); i++) {
                    Boolean load = loadCommentToDB(transformedCommentsList.get(i));
                    if(!load){
                        deleteCounter++;
                    }
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Load procedure");
                alert.setHeaderText("Load procedure finished successfully");
                alert.setContentText("Quantity of loaded comments: " + (transformedCommentsList.size() - deleteCounter));
                alert.showAndWait();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        transformedCommentsList.clear();
        extractedCommentsList.clear();
        bTransform.setDisable(true);
        bLoad.setDisable(true);
        bExtract.setDisable(false);
    }
}
