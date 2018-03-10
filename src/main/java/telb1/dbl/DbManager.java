package telb1.dbl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by User on 10.03.2018.
 */
public class DbManager {
    //public static Connection conn;
    public static Statement statmt;
    public static ResultSet resSet;

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:sample.db");



            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}
