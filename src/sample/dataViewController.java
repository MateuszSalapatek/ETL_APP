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
    private TextField tfAuthor, tfCommentTittle, tfCommentContent, tfFilmRate, tfCreationDate, tfFilmTittle, tfFilmYear, tfFilmTime, tfCommentRate,
            tfCommentAnswerCount, tfCommentAnswerLastUser, tfCommentAnswerLastDate;


    //this method is called when the table is opening and when onKeyReleased is calling for textfields filter
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
        tvDataView.setItems(comment.getViewCommentWithConditions(tfAuthor.getText().toUpperCase(), tfCommentTittle.getText().toUpperCase(),
                            tfCommentContent.getText().toUpperCase(), tfFilmRate.getText().toUpperCase(), tfCreationDate.getText().toUpperCase(),
                            tfFilmTittle.getText().toUpperCase(), tfFilmYear.getText().toUpperCase(), tfFilmTime.getText().toUpperCase(),
                            tfCommentRate.getText().toUpperCase(), tfCommentAnswerCount.getText().toUpperCase(),
                            tfCommentAnswerLastUser.getText().toUpperCase(), tfCommentAnswerLastDate.getText().toUpperCase()) );
    }
}
