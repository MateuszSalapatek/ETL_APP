package sample;


import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
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

import static javafx.collections.FXCollections.observableArrayList;
import static sample.OracleConn.conn;
import static sample.OracleConn.pstmt;
import static sample.OracleConn.stat;

public class Controller {

    //TODO przeszukiwanie listy filmów
    //TODO aktualizacja danych ....filmy czy all?
    //TODO wyświetlanie danych
    //TODO export
    //TODO extract na - ilośc odpowiedzi na komentarz, kto ostatni odpowiedział, kiedy, liczba plusów i minusów pod komentarzem
    //TODO poprawić alerty, dodać wszędzie try catche
    //TODO plik z językiem



    private static String top500html = "https://www.filmweb.pl/ranking/film";
    private ArrayList<Comment> extractedCommentsList;
    private ArrayList<Comment> transformedCommentsList;

    @FXML
    private Button bELT, bExtract, bTransform, bLoad;

    @FXML
    private ChoiceBox cbFilmsTitle;

    @FXML
    private void initialize() throws IOException, SQLException {
        OracleConn Oracle = new OracleConn();

        cbFilmsTitle.getItems().setAll(getFilmsLOV());

        //function to convert url to name of film
        cbFilmsTitle.setConverter(new StringConverter<Film>() {
            @Override
            public String toString(Film uni) {
                return uni.getTittle();
            }
            @Override
            // not used...
            public Film fromString(String s) {
                return null ;
            }
        });

    }
    public ArrayList<Comment> getComments(String url) {

        ArrayList<Comment> commentsList = new ArrayList<Comment>();
        try {
            int pageIterator = 1;
            Document doc = Jsoup.connect(url + pageIterator).get();

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
                    commentObject.setCommentTitle(filmCategory.select(".s-16 a").html());
                    commentObject.setFilmRate(filmCategory.select(".topicInfo li:nth-child(3)").html());
                    commentObject.setFilmYear(pageContent.select(".halfSize").html());
                    commentObject.setFilmTime(pageContent.select(".filmTime").html().substring(0,15));

                    if(commentObject.getCommentContent().equals("")){
                        commentObject.setCommentContent(filmCategory.getElementsByClass("italic").html());
                    }
                    commentsList.add(commentObject);
                }
                pageIterator++;
                doc = Jsoup.connect(url + pageIterator).get();
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
            pstmt = conn.prepareStatement("INSERT INTO COMMENTS VALUES (?,?,?,?,?,?,?,?,?,SYSDATE)");
            pstmt.setInt(1, comment.getIdTransformed());
            pstmt.setString(2, comment.getUser());
            pstmt.setString(3, comment.getCommentTitle());
            pstmt.setString(4, comment.getCommentContent());
            pstmt.setString(5, comment.getFilmRateTransformed());
            pstmt.setString(6, comment.getCreationDate());
            pstmt.setString(7, comment.getTitle());
            pstmt.setInt(8, comment.getFilmYearTransformed());
            pstmt.setString(9, comment.getFilmTime());
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
            commentList.get(i).setFilmRateTransformed(commentList.get(i).getFilmRate().replaceAll("[^0-9]", ""));
            commentList.get(i).setFilmYearTransformed(Integer.parseInt(commentList.get(i).getFilmYear().replaceAll("[^0-9]","")));

        }

        return commentList;
    }

    @FXML
    private void clickETLButton (ActionEvent event) throws SQLException {
        if ( cbFilmsTitle.getValue() == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Select film");
            alert.setHeaderText("Please, choose the film tittle");
            alert.showAndWait();
        }else {
            ArrayList<Comment> extractedList = Controller.this.getComments(cbFilmsTitle.getValue().toString());
            ArrayList<Comment> tranformedList = Controller.this.transformComments(extractedList);
            Integer deleteCounter = 0;
            for (int i = 0; i < tranformedList.size(); i++) {
                Boolean load = loadCommentToDB(tranformedList.get(i));
                if (!load) {
                    deleteCounter++;
                }
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("ETL procedure");
            alert.setHeaderText("ETL procedure finished successfully");
            alert.setContentText("Quantity of extracted comments: " + extractedList.size() + "\n" +
                    "Quantity of loaded comments: " + (tranformedList.size() - deleteCounter));
            alert.showAndWait();
        }
    }

    @FXML
    private void clickExtractButton (ActionEvent event) throws SQLException {
        if ( cbFilmsTitle.getValue() == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Select film");
            alert.setHeaderText("Please, choose the film tittle");
            alert.showAndWait();
        }else {
            try {
                extractedCommentsList = Controller.this.getComments(cbFilmsTitle.getValue().toString());

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Extract procedure");
                alert.setHeaderText("Extract procedure finished successfully");
                alert.setContentText("Quantity of extracted comments: " + extractedCommentsList.size());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }

            bTransform.setDisable(false);
            bExtract.setDisable(true);
        }
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

    public  ObservableList<Film> getFilmsLOV() throws IOException {

        try {
            Document doc = Jsoup.connect(top500html).get();
            Elements rankingListPage = doc.getElementsByClass("item");

            ObservableList<Film> filmsTitles = observableArrayList();

            for (Element rankingList : rankingListPage) {
                Film filmObject = new Film();

                filmObject.setTittle(rankingList.select(".film__link").html()); //film tittle
                filmObject.setUrl("https://www.filmweb.pl" + rankingList.select(".film__link").attr("href") + "/discussion?plusMinus=false&page=");

                if (!filmObject.getTittle().equals("")){
                    filmsTitles.add(filmObject);
                }
            }
            return filmsTitles;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void clickClearDatabase () throws SQLException {
        try {
            int count = 0;

            stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM COMMENTS");
            while (rs.next()) {
                count = rs.getInt(1);
            }
            if (count == 0){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Database");
                alert.setHeaderText("The database is empty");
                alert.showAndWait();
            }else{
                conn.setAutoCommit(false);
                pstmt = conn.prepareStatement("DELETE FROM COMMENTS WHERE 1=1");
                pstmt.execute();
                pstmt.close();
                conn.commit();
                conn.setAutoCommit(true);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Database");
                alert.setHeaderText("Database has been cleared");
                alert.setContentText(count + " rows has been removed");
                alert.showAndWait();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void clickOpenTableView () throws SQLException {

    }
}
