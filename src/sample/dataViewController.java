package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class dataViewController {

    @FXML
    private TableView tvDataView;

    @FXML
    private TableColumn tcId, tcAuthor, tcCommentTittle, tcFilmRate, tcCreationDate, tcFilmTittle, tcFilmYear, tcFilmTime, tcCommentContent, tcCommentRate,
            tcCommentAnswersCount, tcCommentAnswerUser, tcCommentsAnswerDate;


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
    }
}
