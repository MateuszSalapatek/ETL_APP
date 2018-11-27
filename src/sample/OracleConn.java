package sample;

import javafx.scene.control.Alert;

import java.sql.*;

public class OracleConn {

    public static final String user = "system";
    public static final String password = "admin1";
    public static final String db_url = "jdbc:oracle:thin:@localhost:1521:xe";
    public static Connection conn = null;
    public static Statement stat = null;
    public static PreparedStatement pstmt = null;
    public static CallableStatement call = null;

    public OracleConn() {
        try {
            conn = DriverManager.getConnection(db_url,user,password);
            stat = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Unexpected error - contact with administrator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
