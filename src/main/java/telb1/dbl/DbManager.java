package telb1.dbl;

import telb1.Config;

import java.sql.*;
import java.util.Random;

/**
 * Created by User on 10.03.2018.
 */
public class DbManager {
    public static final DbManager INSTANCE = new DbManager();

    private DbManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public Integer getCarId(String carNumber) {
        Integer carId = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM cars where gos_num=?");

            preparedStatement.setString(1, carNumber);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                carId = rs.getInt("car_id");
            }

            return carId;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public String createCarPass(Integer carId) {
        String carPass = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO passwords(car_id,password,datetime) values(?,?,?)");
            carPass = String.format("%04d", new Random().nextInt(10000));
            preparedStatement.setInt(1, carId);
            preparedStatement.setString(2, "p" + carPass);
            preparedStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            preparedStatement.executeUpdate();
            return carPass;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public void cleanExpiredPasswords() {
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM passwords WHERE datetime < ?");
            preparedStatement.setDate(1, new java.sql.Date(System.currentTimeMillis() - 180000));
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean checkWasherPassword(String password) {

        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS total FROM washers where password=?");

            preparedStatement.setString(1, password);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                if (rs.getInt("total") == 0) return false;
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void washerRegistration(String washerPassword, Long chatId) throws SQLException {

        Connection connection = null;
        try {
            connection=DriverManager.getConnection(Config.INSTANCE.DATABASE_URL);
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE washers SET chat_id = null WHERE chat_id=?");

            preparedStatement.setLong(1, chatId);
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement(
                    "UPDATE washers SET chat_id = ? WHERE password=?");
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, washerPassword);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }

            throw new RuntimeException(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public Integer getCarIdFromPasswords(String carPassword) {
        Integer carId=null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM passwords where password=?");

            preparedStatement.setString(1, "p"+carPassword);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                carId = rs.getInt("car_id");
            }

            return carId;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
