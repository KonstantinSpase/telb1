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


    public String connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:sample.db")){


                 Statement stmt  = connection.createStatement();
                 ResultSet rs    = stmt.executeQuery("SELECT * FROM users");

                // loop through the result set
            StringBuilder res=new StringBuilder();
                while (rs.next()) {
                    res.append(rs.getInt("chat_id")+" "+rs.getString("password")+"\n");

                }
                return res.toString();

        }
    }

   /* public static void ReadDB(), SQLException
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
    }*/



}
