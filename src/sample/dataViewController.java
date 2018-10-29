package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.awt.event.KeyListener;
import java.sql.SQLException;

public class dataViewController {

    @FXML
    private TableView tvDataView;

    @FXML
    private TableColumn tcId, tcAuthor, tcCommentTittle, tcFilmRate, tcCreationDate, tcFilmTittle, tcFilmYear, tcFilmTime, tcCommentContent, tcCommentRate,
            tcCommentAnswersCount, tcCommentAnswerUser, tcCommentsAnswerDate;

    @FXML
    private TextField tfAuthor;


    public void showDataView() throws SQLException {

        tcId.setCellValueFactory(new PropertyValueFactory<Comment, Integer>("idTransformed"));
        tcAuthor.setCellValueFactory(new PropertyValueFactory<Comment, String>("user"));
        tcCommentTittle.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentTitle"));
        tcCommentContent.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentContent"));
        tcFilmRate.setCellValueFactory(new PropertyValueFactory<Comment, String>("filmRateTransformed"));
        tcCreationDate.setCellValueFactory(new PropertyValueFactory<Comment, String>("creationDate"));
        tcFilmTittle.setCellValueFactory(new PropertyValueFactory<Comment, String>("title"));
        tcFilmYear.setCellValueFactory(new PropertyValueFactory<Comment, Integer>("filmYearTransformed"));
        tcFilmTime.setCellValueFactory(new PropertyValueFactory<Comment, String>("filmTimeTransformed"));
        tcCommentRate.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentRate"));
        tcCommentAnswersCount.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentAnswersCountTransformed"));
        tcCommentAnswerUser.setCellValueFactory(new PropertyValueFactory<Comment, Integer>("commentAnswersLastUser"));
        tcCommentsAnswerDate.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentAnswersLastDate"));


        Comment comment = new Comment();
        tvDataView.setItems(comment.getViewComment());


        tfAuthor.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {

                String lAuthor = tfAuthor.getText().toUpperCase()+event.getText().toUpperCase();
                if (event.getCode().equals(KeyCode.BACK_SPACE)  && tfAuthor.getText().length()>0) {
                    lAuthor = lAuthor.substring(0,lAuthor.length()-1);
                }

                tcId.setCellValueFactory(new PropertyValueFactory<Comment, Integer>("idTransformed"));
                tcAuthor.setCellValueFactory(new PropertyValueFactory<Comment, String>("user"));
                tcCommentTittle.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentTitle"));
                tcCommentContent.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentContent"));
                tcFilmRate.setCellValueFactory(new PropertyValueFactory<Comment, String>("filmRateTransformed"));
                tcCreationDate.setCellValueFactory(new PropertyValueFactory<Comment, String>("creationDate"));
                tcFilmTittle.setCellValueFactory(new PropertyValueFactory<Comment, String>("title"));
                tcFilmYear.setCellValueFactory(new PropertyValueFactory<Comment, Integer>("filmYearTransformed"));
                tcFilmTime.setCellValueFactory(new PropertyValueFactory<Comment, String>("filmTimeTransformed"));
                tcCommentRate.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentRate"));
                tcCommentAnswersCount.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentAnswersCountTransformed"));
                tcCommentAnswerUser.setCellValueFactory(new PropertyValueFactory<Comment, Integer>("commentAnswersLastUser"));
                tcCommentsAnswerDate.setCellValueFactory(new PropertyValueFactory<Comment, String>("commentAnswersLastDate"));
                try {
                    tvDataView.setItems(comment.getViewCommentWithConditions(lAuthor));

                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Nieoczekiwany błąd");
                    alert.setHeaderText("Wystąpił nieoczekiwany błąd. Prosze o kontakt z administratorem");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }
}
