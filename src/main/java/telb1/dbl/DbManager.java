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

              ReadDB();

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ReadDB() throws ClassNotFoundException, SQLException
    {
        resSet = statmt.executeQuery("SELECT * FROM users");

        while(resSet.next())
        {
            int chat_id = resSet.getInt("chat_id");
            String  name = resSet.getString("password");
            //String  phone = resSet.getString("phone");
            System.out.println( "ID = " + chat_id );
            System.out.println( "name = " + name );
           // System.out.println( "phone = " + phone );
            System.out.println();
        }

        System.out.println("Таблица выведена");//dfgh
    }



}
