package sample;

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
            System.err.println("Problem z otwarciem polaczenia");
            e.printStackTrace();
        }
    }
}
