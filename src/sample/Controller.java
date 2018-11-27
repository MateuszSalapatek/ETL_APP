package sample;


import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;

import static javafx.collections.FXCollections.observableArrayList;
import static sample.OracleConn.*;

public class Controller  {

    private static final String allFilmsLink = "https://www.filmweb.pl/films/search?orderBy=popularity&descending=true&page=";
    private static final String authors = "by Mateusz Sałapatek, Dariusz Lurka, Piotr Hereda";
    private static final String progressing = "Please wait...";
    private static final String comboBoxText = "Type movie name or choose from list";
    private ArrayList<Comment> extractedCommentsList;
    private ArrayList<Comment> transformedCommentsList;
    private ObservableList<Film> allFilms;

    @FXML
    private Button bETL, bExtract, bTransform, bLoad, bCancelExtracted, bDownload, bUpdateLoadedData, bClearDB, bETLall, bTableView;

    @FXML
    private ComboBox cbPickFilm;

    @FXML
    private RadioButton rbExportFiles, rbExportCSV;

    private String matchingString = "";

    @FXML
    private Label lMatchingString, lAuthors_Progress, lProcessData, lETL, lExtract;

    @FXML
    public void initialize() throws IOException, SQLException, InterruptedException {

        try {
            extractedCommentsList = null;
            extractedCommentsList = null;
            new OracleConn();
            allFilms = getFilmsLOV();
            fillFilmsComboBox(allFilms);
            ObservableList<Film> filteredFilms = observableArrayList();
            lAuthors_Progress.setText(authors);

            cbPickFilm.setOnKeyReleased(e -> {
                if (e.getCode().equals(KeyCode.BACK_SPACE)) {
                    if (matchingString.length() > 0) {
                        matchingString = matchingString.substring(0, matchingString.length() - 1);
                    }
                } else if (e.getCode().equals(KeyCode.ENTER)) {
                    //do nothing
                } else if (e.getText().toUpperCase().equals("N") && e.isAltDown()) {
                    matchingString = matchingString + "Ń";
                } else if (e.getText().toUpperCase().equals("O") && e.isAltDown()) {
                    matchingString = matchingString + "Ó";
                } else if (e.getText().toUpperCase().equals("E") && e.isAltDown()) {
                    matchingString = matchingString + "Ę";
                } else if (e.getText().toUpperCase().equals("Z") && e.isAltDown()) {
                    matchingString = matchingString + "Ż";
                } else if (e.getText().toUpperCase().equals("X") && e.isAltDown()) {
                    matchingString = matchingString + "Ź";
                } else if (e.getText().toUpperCase().equals("S") && e.isAltDown()) {
                    matchingString = matchingString + "Ś";
                } else if (e.getText().toUpperCase().equals("C") && e.isAltDown()) {
                    matchingString = matchingString + "Ć";
                } else if (e.getText().toUpperCase().equals("L") && e.isAltDown()) {
                    matchingString = matchingString + "Ł";
                } else {
                    matchingString = matchingString + String.valueOf(e.getText().toUpperCase());
                }
                for (int i = 0; i < allFilms.size(); i++) {
                    if (allFilms.get(i).getTittle().toUpperCase().startsWith(matchingString)) {
                        filteredFilms.add(allFilms.get(i));
                    }
                }
                fillFilmsComboBox(filteredFilms);
                filteredFilms.clear();
                lMatchingString.setText(matchingString);
            });
        }catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public ArrayList<Comment> getComments(String url) throws IOException {

        try {
            int pageCount = (int) Math.ceil(Integer.parseInt(Jsoup.connect(url).get().getElementsByClass("s-20").text().replaceAll("[^0-9]", "")) / 30.0);

            ExecutorService exec = Executors.newFixedThreadPool(pageCount);
            ArrayList<Future<ArrayList<Comment>>> call_list = new ArrayList<Future<ArrayList<Comment>>>();
            for (int i = 1; i <= pageCount; i++) {
                call_list.add(exec.submit(new CommentsThread(url + i)));
            }

            ArrayList<Comment> commentsList = new ArrayList<Comment>();
            for (Future<ArrayList<Comment>> str : call_list) {
                try {
                    commentsList.addAll(str.get());
                } catch (InterruptedException e) {
                    System.out.println(e);
                } catch (ExecutionException e) {
                    System.out.println(e);
                } finally {
                    exec.shutdown();
                }
            }
            return commentsList;
        }catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return null;
        }
    }

    public ArrayList<Comment> getCommentsFromPage (Document doc, Elements pageContent, String commentUrl){
        try {
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
        }catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return null;
        }
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
        try {
            for (int i = 0; i < commentList.size(); i++) {
                commentList.get(i).setIdTransformed(Integer.parseInt(commentList.get(i).getId().replaceAll("[^0-9]", ""))); //to delete chars
                commentList.get(i).setFilmRateTransformed(commentList.get(i).getFilmRate().replaceAll("[^0-9]", ""));
                commentList.get(i).setFilmYearTransformed(Integer.parseInt(commentList.get(i).getFilmYear().replaceAll("[^0-9]", "")));
                commentList.get(i).setCommentAnswersCountTransformed(commentList.get(i).getCommentAnswersCount().replaceAll("[^0-9]", ""));
                commentList.get(i).setFilmTimeTransformed(commentList.get(i).getFilmTime().replaceAll("[^0-9]", ""));

                //because ';' is special symbol, during export to CSV
                commentList.get(i).setUser(commentList.get(i).getUser().replaceAll(";", "."));
                commentList.get(i).setCommentTitle(commentList.get(i).getCommentTitle().replaceAll(";", "."));
                commentList.get(i).setCommentContent(commentList.get(i).getCommentContent().replaceAll(";", "."));
            }
            return commentList;
        }catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return null;
        }
    }

    @FXML
    public void clickETLButton(ActionEvent event) throws SQLException, IOException {
        try {
            if (cbPickFilm.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Select film");
                alert.setHeaderText("Please, choose the film tittle");
                alert.showAndWait();
            } else {

                useProgressingWindow(false);
                showConfirmationWindow("ETL procedure will start, please confirm");

                ArrayList<Comment> extractedList = Controller.this.getComments(cbPickFilm.getValue().toString());
                ArrayList<Comment> tranformedList = Controller.this.transformComments(extractedList);
                Integer deleteCounter = 0;
                for (int i = 0; i < tranformedList.size(); i++) {
                    Boolean load = loadCommentToDB(tranformedList.get(i));
                    if (!load) {
                        deleteCounter++;
                    }
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ETL procedure");
                alert.setHeaderText("ETL procedure finished successfully");
                alert.setContentText("Quantity of extracted comments: " + extractedList.size() + "\n" +
                        "Quantity of loaded comments: " + (tranformedList.size() - deleteCounter));
                alert.showAndWait();

                clearFilmLOVAfterLoading();

            }
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }finally {
            useProgressingWindow(true);
        }
    }

    @FXML
    public void clickExtractButton(ActionEvent event) throws SQLException {
        if (cbPickFilm.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Select film");
            alert.setHeaderText("Please, choose the film tittle");
            alert.showAndWait();
        } else {
            try {
                useProgressingWindow(false);
                showConfirmationWindow("Extract procedure will start, please confirm");

                extractedCommentsList = Controller.this.getComments(cbPickFilm.getValue().toString());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Extract procedure");
                alert.setHeaderText("Extract procedure finished successfully");
                alert.setContentText("Quantity of extracted comments: " + extractedCommentsList.size());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unexpected error");
                alert.setHeaderText("Unexpected error - contact with administrator");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }finally {
                bTransform.setDisable(false);
                bCancelExtracted.setDisable(false);
                cbPickFilm.setDisable(true);
                bExtract.setDisable(true);
                bETL.setDisable(true);
                useProgressingWindow(true);
            }
        }
    }

    @FXML
    public void clickTransformButton(ActionEvent event) throws SQLException {
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
                useProgressingWindow(false);
                showConfirmationWindow("Transform procedure will start, please confirm");

                transformedCommentsList = transformComments(extractedCommentsList);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Transform procedure");
                alert.setHeaderText("Transform procedure finished successfully");
                alert.showAndWait();
                bTransform.setDisable(true);
                bLoad.setDisable(false);
                bCancelExtracted.setDisable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }finally {
            useProgressingWindow(true);
        }
    }

    @FXML
    public void clickLoadButton(ActionEvent event) throws SQLException {
        try {
            if (!(transformedCommentsList.size() > 0)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Load procedure");
                alert.setHeaderText("Load is not possible, because no data found after transforming");
                alert.showAndWait();
            } else {
                useProgressingWindow(false);
                showConfirmationWindow("Load procedure will start, please confirm");

                Integer deleteCounter = 0;
                for (int i = 0; i < transformedCommentsList.size(); i++) {
                    Boolean load = loadCommentToDB(transformedCommentsList.get(i));
                    if (!load) {
                        deleteCounter++;
                    }
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Load procedure");
                alert.setHeaderText("Load procedure finished successfully");
                alert.setContentText("Quantity of loaded comments: " + (transformedCommentsList.size() - deleteCounter));
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }finally {
            transformedCommentsList.clear();
            extractedCommentsList.clear();
            bTransform.setDisable(true);
            bLoad.setDisable(true);
            bExtract.setDisable(false);
            cbPickFilm.setDisable(false);
            bETL.setDisable(false);
            bCancelExtracted.setDisable(true);
            clearFilmLOVAfterLoading();
            useProgressingWindow(true);
        }
    }

    @FXML
    public void clickCancelExtracted(ActionEvent event){
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
            clearFilmLOVAfterLoading();
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
            ExecutorService exec = Executors.newFixedThreadPool(50);
            ArrayList<Future<ArrayList<Film>>> call_list=  new ArrayList<Future<ArrayList<Film>>>();
            for(int i =1;i<=1000;i++) {
                call_list.add(exec.submit(new AllFilmsThread(allFilmsLink + i)));
            }

            ObservableList<Film> filmsList = observableArrayList();
            for(Future<ArrayList<Film>> str:call_list){
                try{
                    filmsList.addAll(str.get());
                }catch(InterruptedException e){
                    System.out.println(e);
                }catch(ExecutionException e){
                    System.out.println(e);
                }finally {
                    exec.shutdown();
                }
            }
            return filmsList;
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return null;
        }
    }

    public ArrayList<Film> getFilmsFromPage( Document doc ){
        try {
            Elements fbFilms = doc.getElementsByClass("hits__item");
            ArrayList<Film> filmsList = new ArrayList<Film>();

            for (Element filmCategory : fbFilms) {
                Film filmObject = new Film();

                filmObject.setTittle(filmCategory.select(".filmPreview__title").html()); //film tittle
                filmObject.setUrl("https://www.filmweb.pl" + filmCategory.select(".filmPreview__link").attr("href") + "/discussion?plusMinus=false&page=");
                filmsList.add(filmObject);
            }
            return filmsList;
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return null;
        }
    }

    @FXML
    public void clickClearDatabase() throws SQLException {
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
        }finally {
            try {
                if (stat != null)
                    stat.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    @FXML
    public void clickOpenTableView() throws SQLException {

        try {
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

            root = loader.load(getClass().getResource("dataView.fxml").openStream());

            dataViewController trDataView = (dataViewController) loader.getController();
            stage.setTitle("Data View");
            root.getStylesheets().add("Resources/style.css");
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
    public void clickETLButtonAll(ActionEvent event) throws SQLException {
        try {
            useProgressingWindow(false);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"The procedure will take several hours, are you sure?",ButtonType.YES,ButtonType.NO);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {

                ObservableList<Film> filmsList;
                filmsList = allFilms;
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
                    System.out.println("Line: " + i + ", get " + String.valueOf(extractedList.size() - deleteCounter) + " comments");
                }
                clearFilmLOVAfterLoading();
            }else{
                useProgressingWindow(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            useProgressingWindow(true);
        }
    }

    public void clickExportCSV() throws SQLException, IOException {

        try {
            Comment com = new Comment();
            ObservableList<Comment> commentList;
            commentList = com.getViewComment();

            Stage currentStage = new Stage();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose the export place");
            File selectedDirectory = directoryChooser.showDialog(currentStage);

            if (selectedDirectory == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Folder choosing");
                alert.setHeaderText("Folder has not been choosen");
                alert.showAndWait();
            } else {
                if(commentList.size()>0) {
                    File f = new File(selectedDirectory.getAbsolutePath() + "\\Comments.csv");
                    if(f.isFile()){
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Export comments");
                        alert.setHeaderText("Export is not possible - "+selectedDirectory.getAbsolutePath() + "\\Comments.csv" + " is already exists");
                        alert.showAndWait();
                    }else {
                        useProgressingWindow(false);
                        showConfirmationWindow("Export CSV procedure will start, please confirm");

                        ExecutorService exec = Executors.newFixedThreadPool(commentList.size());
                        BufferedWriter writer = Files.newBufferedWriter(Paths.get(selectedDirectory.getAbsolutePath() + "\\Comments.csv"));

                        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';')
                                .withHeader("ID", "AUTHOR", "COMMENT TITTLE", "COMMENT", "FILM RATE", "CREATION DATE", "FILM TITTLE",
                                        "FILM YEAR", "FILM TIME", "COMMENT RATE", "COMMENT ANSWER COUNT", "COMMENT ANSWER LAST USER", "COMMENT ANSWER LAST DATE"));

                        for (int i = 0; i < commentList.size(); i++) {
                            csvPrinter.printRecord(commentList.get(i).getIdTransformed(), commentList.get(i).getUser(), commentList.get(i).getCommentTitle(),
                                    commentList.get(i).getCommentContent(), commentList.get(i).getFilmRateTransformed(), commentList.get(i).getCreationDate(),
                                    commentList.get(i).getTitle(), commentList.get(i).getFilmYearTransformed(), commentList.get(i).getFilmTimeTransformed(),
                                    commentList.get(i).getCommentRate(), commentList.get(i).getCommentAnswersCountTransformed(), commentList.get(i).getCommentAnswersLastUser(),
                                    commentList.get(i).getCommentAnswersLastDate());
                        }
                        csvPrinter.flush();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Export procedure");
                        alert.setHeaderText("Export procedure finished successfully");
                        alert.setContentText("Path for exported csv file is: " + selectedDirectory.getAbsolutePath() + "\\Comments.csv");
                        alert.showAndWait();
                    }
                }else{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Export comments");
                    alert.setHeaderText("Export is not possible - Database is empty");
                    alert.showAndWait();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }finally {
            useProgressingWindow(true);
        }
    }

    public void clickExportFiles()  {

        try {
            Comment com = new Comment();
            Integer countExistingFiles = 0;

            ObservableList<Comment> commentList;
            commentList = com.getViewComment();

            Stage currentStage = new Stage();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose the export place");
            File selectedDirectory = directoryChooser.showDialog(currentStage);

            if (selectedDirectory == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Folder choosing");
                alert.setHeaderText("Folder has not been choosen");
                alert.showAndWait();
            } else {
                useProgressingWindow(false);
                showConfirmationWindow("Export file procedure will start, please confirm");

                if(commentList.size()>0) {
                    ExecutorService exec = Executors.newFixedThreadPool(commentList.size());
                    for (int i = 0; i < commentList.size(); i++) {
                        File f = new File(selectedDirectory.getAbsolutePath() + "/" + commentList.get(i).getIdTransformed() + ".txt");
                        if (f.isFile()) {
                            countExistingFiles++;
                        } else {
                            exec.submit(new FileExportThreads(selectedDirectory, i, commentList));
                        }
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export procedure");
                    alert.setHeaderText("Export procedure finished successfully");
                    alert.setContentText("Path for exported files is: " + selectedDirectory.getAbsolutePath() +
                            "\nQuantity of exported files: " + String.valueOf(com.getViewComment().size() - countExistingFiles));
                    alert.showAndWait();
                }else{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Export comments");
                    alert.setHeaderText("Export is not possible - Database is empty");
                    alert.showAndWait();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }finally {
            useProgressingWindow(true);
        }
    }

    @FXML
    public void clickUpdateLoadedData(ActionEvent event) {
        try {
            ArrayList<String> commentsLinks = new ArrayList<>();
            Comment comment = new Comment();
            commentsLinks = comment.getFilmsCommentsLinks();
            Integer commentsQty = 0;
            if(commentsLinks.size()>0){
                if(clearDataBase()){
                    useProgressingWindow(false);
                    showConfirmationWindow("Update DB procedure will start, please confirm");

                    for ( int j = 0; j<commentsLinks.size(); j++ ) {
                        ArrayList<Comment> extractedList = Controller.this.getComments(commentsLinks.get(j));
                        ArrayList<Comment> tranformedList = Controller.this.transformComments(extractedList);
                        commentsQty = commentsQty + tranformedList.size();
                        for (int i = 0; i < tranformedList.size(); i++) {
                            loadCommentToDB(tranformedList.get(i));
                        }
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
        }finally {
            useProgressingWindow(true);
        }
    }

    @FXML
    public void clickDownloadButton(ActionEvent event) {
        try {
            if (rbExportCSV.isSelected()){
                clickExportCSV();
            }
            else if (rbExportFiles.isSelected()){
                clickExportFiles();
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
            public String toString(Film film) {
                return film.getTittle();
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
            useProgressingWindow(false);

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
        }finally {
            useProgressingWindow(true);
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    public void clearFilmLOVAfterLoading(){
        try {
            matchingString = "";
            lMatchingString.setText(matchingString);
            fillFilmsComboBox(allFilms);
            cbPickFilm.setPromptText(comboBoxText);
        }catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    public void useProgressingWindow(Boolean flag){

            bETL.setVisible(flag);
            bExtract.setVisible(flag);
            bTransform.setVisible(flag);
            bLoad.setVisible(flag);
            bCancelExtracted.setVisible(flag);
            cbPickFilm.setVisible(flag);
            rbExportFiles.setVisible(flag);
            rbExportCSV.setVisible(flag);
            bDownload.setVisible(flag);
            bUpdateLoadedData.setVisible(flag);
            bClearDB.setVisible(flag);
            bETLall.setVisible(flag);
            bTableView.setVisible(flag);
            lETL.setVisible(flag);
            lProcessData.setVisible(flag);
            lExtract.setVisible(flag);
            lMatchingString.setVisible(flag);
            if(!flag){
                lAuthors_Progress.setText(progressing);
            }else{
                lAuthors_Progress.setText(authors);
            }
    }
    public void showConfirmationWindow(String headetText){
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText(headetText);
        info.showAndWait();
    }
}
class CommentsThread implements Callable {

    private String  url;
    private Elements el;
    private Document doc;

    CommentsThread(String link){
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

class FileExportThreads implements Runnable {

    private File file;
    private Integer i;
    private ObservableList<Comment> com;

    public FileExportThreads(File file, Integer i, ObservableList<Comment> com) {
        this.file = file;
        this.i = i;
        this.com = com;

        Thread thread = new Thread();
        thread.start();
    }

    @Override
    public void run() {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath() + "/" + com.get(i).getIdTransformed() + ".txt"));
            writer.append("ID: " + com.get(i).getIdTransformed());
            writer.newLine();
            writer.append("AUTHOR: " + com.get(i).getUser());
            writer.newLine();
            writer.append("COMMENT TITTLE: " + com.get(i).getCommentTitle());
            writer.newLine();
            writer.append("COMMENT: " + com.get(i).getCommentContent());
            writer.newLine();
            writer.append("FILM RATE: " + com.get(i).getFilmRateTransformed());
            writer.newLine();
            writer.append("CREATION DATE: " + com.get(i).getCreationDate());
            writer.newLine();
            writer.append("FILM TITTLE: " + com.get(i).getTitle());
            writer.newLine();
            writer.append("FILM YEAR: " + com.get(i).getFilmYearTransformed());
            writer.newLine();
            writer.append("FILM TIME: " + com.get(i).getFilmTimeTransformed());
            writer.newLine();
            writer.append("COMMENT RATE: " + com.get(i).getCommentRate());
            writer.newLine();
            writer.append("COMMENT ANSWER COUNT: " + com.get(i).getCommentAnswersCountTransformed());
            writer.newLine();
            writer.append("COMMENT ANSWER LAST USER: " + com.get(i).getCommentAnswersLastUser());
            writer.newLine();
            writer.append("COMMENT ANSWER LAST DATE: " + com.get(i).getCommentAnswersLastDate());
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
class AllFilmsThread implements Callable {

    private String  url;
    private Elements el;
    private Document doc;

    AllFilmsThread(String link){
        this.url = link;
    }

    @Override
    public ArrayList<Film> call() throws Exception {
        this.doc = Jsoup.connect(url ).get();
        this.el = Jsoup.connect(url).get().getElementsByClass("hits");
        Controller con = new Controller();
        ArrayList<Film> threadFilms = new ArrayList<>();
        threadFilms.addAll(con.getFilmsFromPage(doc));
        return threadFilms;
    }
}
