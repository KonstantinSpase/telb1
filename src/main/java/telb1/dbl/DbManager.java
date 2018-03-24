package telb1.dbl;

import org.joda.time.DateTime;
import org.joda.time.YearMonth;

import telb1.Config;

import java.sql.*;

import java.util.LinkedList;
import java.util.List;
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

            preparedStatement.setString(1, carNumber.toUpperCase());
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                carId = rs.getInt("car_id");
            }

            return carId;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public String getCarNumber(Integer carId) {
        String carNumber = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM cars where car_id=?");

            preparedStatement.setInt(1, carId);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                carNumber = rs.getString("gos_num");
            }

            return carNumber;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String createCarPass(Integer carId, DateTime requestDateTime) throws SQLException {
        String carPass = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO passwords(car_id,password,datetime) values(?,?,?)");
            carPass = String.format("%04d", new Random().nextInt(10000));
            preparedStatement.setInt(1, carId);
            preparedStatement.setString(2, "p" + carPass);
            preparedStatement.setDate(3, new java.sql.Date(requestDateTime.getMillis()));

            preparedStatement.executeUpdate();
            return carPass;
        }
    }

    public void cleanExpiredPasswords(DateTime checkDateTime) {
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM passwords WHERE datetime < ? OR datetime<?");
            int passExpiredTime = Integer.parseInt(Config.INSTANCE.CAR_PASSWORD_EXPIRED) * 60000;
            DateTime startCurrentMonth = DateTime.now().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();

            preparedStatement.setDate(1,
                    new java.sql.Date(checkDateTime.getMillis() - passExpiredTime));
            preparedStatement.setDate(2,
                    new java.sql.Date(startCurrentMonth.getMillis()));
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public String washerRegistration(String washerPassword, Long chatId, String washerName) {
        String point = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM washers WHERE password=?");

            preparedStatement.setString(1, washerPassword);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                if (rs.getObject("chat_id") != null) {
                    throw (new RuntimeException("пароль уже активирован"));
                }
                point = rs.getString("point");
            } else {
                throw (new RuntimeException("неправильный пароль"));
            }
            preparedStatement = connection.prepareStatement(
                    "UPDATE washers SET chat_id = ?,washer_name=? WHERE password=?");
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, washerName);
            preparedStatement.setString(3, washerPassword);
            preparedStatement.executeUpdate();
            return point;
        } catch (SQLException e) {
            throw new RuntimeException("данный телефон активирован на другой объект");
        }
    }

    public Integer getCarIdByPassword(String carPassword) {
        Integer carId = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT car_id FROM passwords where password=?");

            preparedStatement.setString(1, "p" + carPassword);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                carId = rs.getInt("car_id");
            }
            return carId;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Integer getWasherId(Long chatId) {
        Integer washerId = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT washer_id FROM washers where chat_id=?");
            preparedStatement.setLong(1, chatId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                washerId = rs.getInt("washer_id");
            }
            return washerId;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void washing(Integer carId, Integer washerId,
                        DateTime washingDateTime) throws SQLException {
        Connection connection = null;
        //  Integer washingId = null;
        try {
            connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL);
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO main(datetime,car_id,washer_id) values(?,?,?)");
            preparedStatement.setDate(1,
                    new java.sql.Date(washingDateTime.getMillis()));
            preparedStatement.setInt(2, carId);
            preparedStatement.setInt(3, washerId);
            preparedStatement.executeUpdate();
          /*  ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                washingId = rs.getInt(1);
            }*/
            preparedStatement = connection.prepareStatement(
                    "DELETE FROM passwords WHERE car_id = ?");
            preparedStatement.setLong(1, carId);
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

    public int getCarWashingsInCurrentMonth(Integer carId, DateTime checkDateTime) {
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS total FROM main where car_id=? AND datetime BETWEEN ? AND ?");
            DateTime startCurrentMonth = checkDateTime.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime startNextMonth = startCurrentMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();

            preparedStatement.setInt(1, carId);
            preparedStatement.setDate(2, new java.sql.Date(startCurrentMonth.getMillis()));
            preparedStatement.setDate(3, new java.sql.Date(startNextMonth.getMillis() - 1));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            return rs.getInt("total");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public int getPointWashingsMonth(Integer washer_id, DateTime checkDateTime) {
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS total FROM main where washer_id=? AND datetime BETWEEN ? AND ?");
            DateTime startCurrentMonth = checkDateTime.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime startNextMonth = startCurrentMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();

            preparedStatement.setInt(1, washer_id);
            preparedStatement.setDate(2, new java.sql.Date(startCurrentMonth.getMillis()));
            preparedStatement.setDate(3, new java.sql.Date(startNextMonth.getMillis() - 1));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            return rs.getInt("total");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<String> getMonthReport1(YearMonth yearMonth) {
        List<String> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT (m.washing_id) as total, w.point " +
                            "FROM main m " +
                            "INNER JOIN washers w ON m.washer_id=w.washer_id " +
                            "WHERE datetime BETWEEN ? AND ? " +
                            "GROUP BY w.point"

            );
            DateTime startCurrentMonth = yearMonth.toDateTime(null).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime startNextMonth = startCurrentMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            preparedStatement.setDate(1, new java.sql.Date(startCurrentMonth.getMillis()));
            preparedStatement.setDate(2, new java.sql.Date(startNextMonth.getMillis() - 1));

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("point") + " - " + rs.getInt("total"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public List<WashingModel> getMonthReport(YearMonth yearMonth) {
        List<WashingModel> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT m.washing_id,m.datetime,c.gos_num,c.fz,w.point,w.washer_name " +
                            "FROM main m " +
                            "INNER JOIN cars c ON m.car_id=c.car_id " +
                            "INNER JOIN washers w ON m.washer_id=w.washer_id " +
                            "WHERE datetime BETWEEN ? AND ?"

            );
            DateTime startCurrentMonth = yearMonth.toDateTime(null).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime startNextMonth = startCurrentMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            preparedStatement.setDate(1, new java.sql.Date(startCurrentMonth.getMillis()));
            preparedStatement.setDate(2, new java.sql.Date(startNextMonth.getMillis() - 1));

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                DateTime dateTime = new DateTime(rs.getDate("datetime"));
                WashingModel washingModel = new WashingModel(
                        rs.getInt("washing_id"),
                        dateTime,
                        rs.getString("gos_num"),
                        rs.getString("fz"),
                        rs.getString("point"),
                        rs.getString("washer_name")
                );
                result.add(washingModel);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public int getTotalCurrentMonth() {

        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS total FROM main where datetime BETWEEN ? AND ?");
            DateTime startCurrentMonth = DateTime.now().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime startNextMonth = startCurrentMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            preparedStatement.setDate(1, new java.sql.Date(startCurrentMonth.getMillis()));
            preparedStatement.setDate(2, new java.sql.Date(startNextMonth.getMillis() - 1));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            return rs.getInt("total");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public List<Integer[]> getWashingsByFz(YearMonth yearMonth) {
        List<Integer[]> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT (m.washing_id) as total, c.fz " +
                            "FROM main m " +
                            "INNER JOIN cars c ON m.car_id=c.car_id " +
                            "WHERE datetime BETWEEN ? AND ? " +
                            "GROUP BY c.fz"

            );
            DateTime startCurrentMonth = yearMonth.toDateTime(null).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime startNextMonth = startCurrentMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            preparedStatement.setDate(1, new java.sql.Date(startCurrentMonth.getMillis()));
            preparedStatement.setDate(2, new java.sql.Date(startNextMonth.getMillis() - 1));

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
               Integer[] fz={rs.getInt("fz"), rs.getInt("total")};
                result.add( fz);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
}
