package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DataViewController {

    @FXML
    private TableView tvDataView;

    @FXML
    private TableColumn tcId, tcAuthor, tcCommentTittle, tcFilmRate, tcCreationDate, tcFilmTittle, tcFilmYear, tcFilmTime;
}
