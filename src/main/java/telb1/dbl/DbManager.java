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

   /* public boolean checkWasherPassword(String password) {

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
    }*/

    public String washerRegistration(String washerPassword, Long chatId) {

        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM washers WHERE password=?");

            preparedStatement.setString(1, washerPassword);
            ResultSet rs = preparedStatement.executeQuery();
            String washerName;
            if (rs.next()) {
                if (rs.getObject("chat_id") != null) {
                    throw (new RuntimeException("пользователь уже активирован"));
                }
                washerName = rs.getString("washer_name");
            } else {
                throw (new RuntimeException("неправильный пароль"));
            }
            preparedStatement = connection.prepareStatement(
                    "UPDATE washers SET chat_id = ? WHERE password=?");
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, washerPassword);
            preparedStatement.executeUpdate();
            return washerName;
        } catch (SQLException e) {
            throw new RuntimeException("на данный телефон активирован другой пользователь");
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
    public String getCarNumberByCarId(Integer carId) {
        String gosNum = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT gos_num FROM cars where car_id=?");

            preparedStatement.setString(1,"" + carId);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                gosNum = rs.getString("gos_num");
            }
            return gosNum;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }



    public WasherModel getWasher(Long chatId) {
        WasherModel washer = null;
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM washers where chat_id=?");
            preparedStatement.setLong(1, chatId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                washer = new WasherModel(rs.getInt("washer_id"),
                        rs.getString("point"),
                        rs.getString("washer_name"));
            }
            return washer;
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

    public int getUserWashingsMonth(Integer washer_id, DateTime checkDateTime) {
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

    public int getPointWashingsMonth(String point, DateTime checkDateTime) {
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS total FROM main where washer_id IN " +
                            "(SELECT washer_id FROM washers WHERE point=?)" +
                            " AND datetime BETWEEN ? AND ?");
            DateTime startCurrentMonth = checkDateTime.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            DateTime startNextMonth = startCurrentMonth.plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            preparedStatement.setString(1, point);
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
               result.add(rs.getString("point")+" - "+rs.getInt("total"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public List<WashingModel> getFzMonthReport(YearMonth yearMonth) {
        List<WashingModel> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection(Config.INSTANCE.DATABASE_URL)) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT summ.washing_id,m.datetime,c.gos_num,c.fz,w.point,w.washer_name " +
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
}
