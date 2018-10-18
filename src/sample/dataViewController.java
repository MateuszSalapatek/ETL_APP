package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class dataViewController {

    @FXML
    private TableView tvDataView;

    @FXML
    private TableColumn tcId, tcAuthor, tcCommentTittle, tcFilmRate, tcCreationDate, tcFilmTittle, tcFilmYear, tcFilmTime, tcCommentContent;


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


        Comment comment = new Comment();
        tvDataView.setItems(comment.getViewComment());
    }
}
