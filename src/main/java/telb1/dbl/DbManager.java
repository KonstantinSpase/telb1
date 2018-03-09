package telb1.dbl;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by User on 10.03.2018.
 */
public class DbManager {
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
