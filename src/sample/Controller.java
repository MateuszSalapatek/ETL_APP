package sample;


import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.csv.CSVFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.server.ExportException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static javafx.collections.FXCollections.observableArrayList;
import static sample.OracleConn.*;



public class Controller  {

    //TODO export
    //TODO poprawić alerty, dodać wszędzie try catche
    //TODO plik z językiem
    //TODO jak zrobić progress bar??
    //TODO bug CSV - polskie znaki
    //TODO bug CSV - niepełne dane
    //TODO sprawdzić poprawnośc angielskeigo
    //TODO dorobić licznik wierszy w tabeli
    //TODO czy dodać link do kolumny?

    private static String top500html = "https://www.filmweb.pl/ranking/film";
    private ArrayList<Comment> extractedCommentsList;
    private ArrayList<Comment> transformedCommentsList;

    @FXML
    private Button bETL, bExtract, bTransform, bLoad, bCancelExtracted;

    @FXML
    private ComboBox cbPickFilm;

    @FXML
    private void initialize() throws IOException, SQLException {
        extractedCommentsList = null;
        extractedCommentsList = null;
        OracleConn Oracle = new OracleConn();
        fillFilmsComboBox(getFilmsLOV());
    }

    public ArrayList<Comment> getComments(String url) throws IOException {

        int pageCount = (int) Math.ceil( Integer.parseInt(Jsoup.connect(url).get().getElementsByClass("s-20").text().replaceAll("[^0-9]", ""))/30.0);

        ExecutorService exec = Executors.newFixedThreadPool(pageCount);
        ArrayList<Future<ArrayList<Comment>>> call_list=  new ArrayList<Future<ArrayList<Comment>>>();
        for(int i =1;i<=pageCount;i++) {
            call_list.add(exec.submit(new NewThread(url + i)));
        }

        ArrayList<Comment> commentsList = new ArrayList<Comment>();
        for(Future<ArrayList<Comment>> str:call_list){
            try{
                commentsList.addAll(str.get());
            }catch(InterruptedException e){
                System.out.println(e);
            }catch(ExecutionException e){
                System.out.println(e);
            }finally {
                exec.shutdown();
            }
        }
        return commentsList;
    }

    public ArrayList<Comment> getCommentsFromPage (Document doc, Elements pageContent, String commentUrl){
        Elements fbComments = doc.getElementsByClass("filmCategory");
        ArrayList<Comment> commentsList = new ArrayList<Comment>();

        for (Element filmCategory : fbComments) {
            Comment commentObject = new Comment();

            commentObject.setId(filmCategory.id());  //id
            commentObject.setCreationDate(filmCategory.select(".topicInfo .cap").attr("title")); //date
            commentObject.setUser(filmCategory.getElementsByClass("userNameLink").html()); //user
            commentObject.setCommentContent(filmCategory.getElementsByClass("text").html()); //comment
            commentObject.setTitle(pageContent.select(".hdr h1 a").html()); //film tittle
            commentObject.setCommentTitle(filmCategory.select(".s-16 a").html());
            commentObject.setFilmRate(filmCategory.select(".topicInfo li:nth-child(3)").html());
            commentObject.setFilmYear(pageContent.select(".halfSize").html());
            commentObject.setFilmTime(pageContent.select(".filmTime").attr("datetime"));
            commentObject.setCommentRate(filmCategory.select(".plusCount").html());
            commentObject.setCommentAnswersCount(filmCategory.getElementsByClass("topicAnswers").text());
            commentObject.setCommentAnswersLastUser(filmCategory.getElementsByClass("userLink").text());
            commentObject.setCommentAnswersLastDate(filmCategory.select("ul.inline li:nth-child(2) a:nth-child(2) span").attr("title"));
            commentObject.setCommentLink(commentUrl);
            if (commentObject.getCommentContent().equals("")) {
                commentObject.setCommentContent(filmCategory.getElementsByClass("italic").html());
            }
            commentsList.add(commentObject);
        }
        return commentsList;
    }

    public Boolean loadCommentToDB(Comment comment) throws SQLException {
        try {
            pstmt = conn.prepareStatement("INSERT INTO COMMENTS VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE)");
            pstmt.setInt(1, comment.getIdTransformed());
            pstmt.setString(2, comment.getUser());
            pstmt.setString(3, comment.getCommentTitle());
            pstmt.setString(4, comment.getCommentContent());
            pstmt.setString(5, comment.getFilmRateTransformed());
            pstmt.setString(6, comment.getCreationDate());
            pstmt.setString(7, comment.getCommentRate());
            pstmt.setString(8, comment.getCommentAnswersCountTransformed());
            pstmt.setString(9, comment.getCommentAnswersLastUser());
            pstmt.setString(10, comment.getCommentAnswersLastDate());
            pstmt.setString(11, comment.getTitle());
            pstmt.setInt(12, comment.getFilmYearTransformed());
            pstmt.setString(13, comment.getFilmTimeTransformed());
            pstmt.setString(14, comment.getCommentLink());
            pstmt.execute();
            pstmt.close();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            //duplicate id
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public ArrayList<Comment> transformComments(ArrayList<Comment> commentList) {

        for (int i = 0; i < commentList.size(); i++) {
            commentList.get(i).setIdTransformed(Integer.parseInt(commentList.get(i).getId().replaceAll("[^0-9]", ""))); //to delete chars
            commentList.get(i).setFilmRateTransformed(commentList.get(i).getFilmRate().replaceAll("[^0-9]", ""));
            commentList.get(i).setFilmYearTransformed(Integer.parseInt(commentList.get(i).getFilmYear().replaceAll("[^0-9]", "")));
            commentList.get(i).setCommentAnswersCountTransformed(commentList.get(i).getCommentAnswersCount().replaceAll("[^0-9]", ""));
            commentList.get(i).setFilmTimeTransformed(commentList.get(i).getFilmTime().replaceAll("[^0-9]", ""));
        }

        return commentList;
    }

    @FXML
    private void clickETLButton(ActionEvent event) throws SQLException, IOException {
        if (cbPickFilm.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Select film");
            alert.setHeaderText("Please, choose the film tittle");
            alert.showAndWait();
        } else {
            ArrayList<Comment> extractedList = Controller.this.getComments(cbPickFilm.getValue().toString());
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
    private void clickExtractButton(ActionEvent event) throws SQLException {
        if (cbPickFilm.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Select film");
            alert.setHeaderText("Please, choose the film tittle");
            alert.showAndWait();
        } else {
            try {
                extractedCommentsList = Controller.this.getComments(cbPickFilm.getValue().toString());

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Extract procedure");
                alert.setHeaderText("Extract procedure finished successfully");
                alert.setContentText("Quantity of extracted comments: " + extractedCommentsList.size());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }

            bTransform.setDisable(false);
            bCancelExtracted.setDisable(false);
            cbPickFilm.setDisable(true);
            bExtract.setDisable(true);
            bETL.setDisable(true);
        }
    }

    @FXML
    private void clickTransformButton(ActionEvent event) throws SQLException {
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
                bCancelExtracted.setDisable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clickLoadButton(ActionEvent event) throws SQLException {
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
                    if (!load) {
                        deleteCounter++;
                    }
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Load procedure");
                alert.setHeaderText("Load procedure finished successfully");
                alert.setContentText("Quantity of loaded comments: " + (transformedCommentsList.size() - deleteCounter));
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        transformedCommentsList.clear();
        extractedCommentsList.clear();
        bTransform.setDisable(true);
        bLoad.setDisable(true);
        bExtract.setDisable(false);
        cbPickFilm.setDisable(false);
        bETL.setDisable(false);
        bCancelExtracted.setDisable(true);
    }

    @FXML
    private void clickCancelExtracted(ActionEvent event){
        try {
            bCancelExtracted.setDisable(true);
            bExtract.setDisable(false);
            bTransform.setDisable(true);
            bLoad.setDisable(true);
            cbPickFilm.setDisable(false);
            bETL.setDisable(false);
            if(extractedCommentsList.size()>0) {
                extractedCommentsList.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public ObservableList<Film> getFilmsLOV() throws IOException {

        try {
            Document doc = Jsoup.connect(top500html).get();
            Elements rankingListPage = doc.getElementsByClass("item");
            ObservableList<Film> filmsTitles = observableArrayList();

            for (Element rankingList : rankingListPage) {
                Film filmObject = new Film();

                filmObject.setTittle(rankingList.select(".film__link").html()); //film tittle
                filmObject.setUrl("https://www.filmweb.pl" + rankingList.select(".film__link").attr("href") + "/discussion?plusMinus=false&page=");

                if (!filmObject.getTittle().equals("")) {
                    filmsTitles.add(filmObject);
                }
            }
            return filmsTitles;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void clickClearDatabase() throws SQLException {
        try {
            int count = 0;

            stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM COMMENTS");
            while (rs.next()) {
                count = rs.getInt(1);
            }
            if (count == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Database");
                alert.setHeaderText("The database is empty");
                alert.showAndWait();
            } else {
                if(clearDataBase()){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Database");
                    alert.setHeaderText("Database has been cleared");
                    alert.setContentText(count + " rows has been removed");
                    alert.showAndWait();
                }else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Unexpected error");
                    alert.setHeaderText("Unexpected error - contact with administrator");
                    alert.showAndWait();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void clickOpenTableView() throws SQLException {

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        Pane root = null;

        ////////////////////////////////
        //to maximalize window
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        /////////////////////////////////

        try {
            root = loader.load(getClass().getResource("dataView.fxml").openStream());

            dataViewController trDataView = (dataViewController) loader.getController();
            stage.setTitle("Data View");
            stage.setScene(new Scene(root));
            stage.show();

            trDataView.showDataView();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    private void clickETLButtonAll(ActionEvent event) throws SQLException {
        try {

            ObservableList<Film> filmsList;
            filmsList = getFilmsLOV();

            for (int i = 0; i < filmsList.size(); i++) {
                ArrayList<Comment> extractedList = Controller.this.getComments(filmsList.get(i).getUrl());
                ArrayList<Comment> tranformedList = Controller.this.transformComments(extractedList);
                Integer deleteCounter = 0;
                for (int j = 0; j < tranformedList.size(); j++) {
                    Boolean load = loadCommentToDB(tranformedList.get(j));
                    if (!load) {
                        deleteCounter++;
                    }
                }
                System.out.println("Linia: " + i + ", pobrano " + extractedList.size() + "komentarzy");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clickExportCSV(ActionEvent event) throws SQLException, IOException {

        try {
            Comment com = new Comment();
            Stage currentStage = new Stage();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose the export place");
            File selectedDirectory = directoryChooser.showDialog(currentStage);

            if (selectedDirectory == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Folder choosing");
                alert.setHeaderText("Foler has not been choosen");
                alert.showAndWait();
            } else {
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(selectedDirectory.getAbsolutePath() + "\\Comments.csv"));

                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("ID", "AUTHOR", "COMMENT TITTLE", "COMMENT", "FILM RATE", "CREATION DATE", "FILM TITTLE",
                                "FILM YEAR", "FILM TIME", "COMMENT RATE", "COMMENT ANSWER COUNT", "COMMENT ANSWER LAST USER", "COMMENT ANSWER LAST DATE"));

                for (int i = 0; i < com.getViewComment().size(); i++) {
                    csvPrinter.printRecord(com.getViewComment().get(i).getIdTransformed(), com.getViewComment().get(i).getUser(), com.getViewComment().get(i).getCommentTitle(),
                            com.getViewComment().get(i).getCommentContent(), com.getViewComment().get(i).getFilmRateTransformed(), com.getViewComment().get(i).getCreationDate(),
                            com.getViewComment().get(i).getTitle(), com.getViewComment().get(i).getFilmYearTransformed(), com.getViewComment().get(i).getFilmTimeTransformed(),
                            com.getViewComment().get(i).getCommentRate(), com.getViewComment().get(i).getCommentAnswersCountTransformed(), com.getViewComment().get(i).getCommentAnswersLastUser(),
                            com.getViewComment().get(i).getCommentAnswersLastDate());
                }
                csvPrinter.flush();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Export procedure");
                alert.setHeaderText("Export procedure finished successfully");
                alert.setContentText("Path for exported csv file is: " + selectedDirectory.getAbsolutePath() + "\\Comments.csv");
                alert.showAndWait();
            }
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void clickUpdateLoadedData(ActionEvent event) {
        try {
            ArrayList<String> commentsLinks = new ArrayList<>();
            Comment comment = new Comment();
            commentsLinks = comment.getFilmsCommentsLinks();
            Integer commentsQty = 0;
            if(commentsLinks.size()>0){
                if(clearDataBase()){
                    for ( int j = 0; j<commentsLinks.size(); j++ ) {
                        ArrayList<Comment> extractedList = Controller.this.getComments(commentsLinks.get(j));
                        ArrayList<Comment> tranformedList = Controller.this.transformComments(extractedList);
                        commentsQty = commentsQty + tranformedList.size();
                        for (int i = 0; i < tranformedList.size(); i++) {
                            loadCommentToDB(tranformedList.get(i));
                        }
                    }
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Updating procedure");
                    alert.setHeaderText("Updating procedure finished successfully");
                    alert.setContentText("Quantity of updated comments: " + commentsQty);
                    alert.showAndWait();
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Unexpected error");
                    alert.setHeaderText("Unexpected error - contact with administrator");
                    alert.showAndWait();
                }
            }else{
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Database");
                alert.setHeaderText("The database is empty");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void fillFilmsComboBox(ObservableList<Film> films){
        cbPickFilm.getItems().setAll(films);

        //function to convert url to name of film
        cbPickFilm.setConverter(new StringConverter<Film>() {
            @Override
            public String toString(Film uni) {
                return uni.getTittle();
            }

            @Override
            // not used...
            public Film fromString(String s) {
                return null;
            }
        });
    }

    public Boolean clearDataBase() throws SQLException {
        try {
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement("DELETE FROM COMMENTS WHERE 1=1");
            pstmt.execute();
            pstmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        }catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return false;
        }
    }
}
class NewThread implements Callable {

    private String  url;
    private Elements el;
    private Document doc;


    NewThread(String link){
        this.url = link;
    }

    @Override
    public ArrayList<Comment> call() throws Exception {
        this.doc = Jsoup.connect(url ).get();
        this.el = Jsoup.connect(url).get().getElementsByClass("filmPage");
        Controller con = new Controller();
        ArrayList<Comment> threadComments = new ArrayList<>();
        threadComments.addAll(con.getCommentsFromPage(doc,el,url));
        return threadComments;
    }
}
