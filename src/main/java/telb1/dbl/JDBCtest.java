package telb1.dbl;


import java.sql.*;
import java.util.logging.*;

    public class JDBCtest {

        public static void main() {

            Connection connection = null;
            //URL к базе состоит из протокола:подпротокола://[хоста]:[порта_СУБД]/[БД] и других_сведений
            String url = "jdbc:sqlite:sample.db";
            //Имя пользователя БД
            //String name = "user";
            //Пароль
            //String password = "123456";
            try {


                //Connection connection = DriverManager.getConnection("jdbc:sqlite:sample.db");

                //Загружаем драйвер
                //Class.forName("org.postgresql.Driver");
                Class.forName("org.sqlite.JDBC");
                System.out.println("Драйвер подключен");
                //Создаём соединение
                connection = DriverManager.getConnection(url);
                System.out.println("Соединение установлено");
                //Для использования SQL запросов существуют 3 типа объектов:
                //1.Statement: используется для простых случаев без параметров
                Statement statement = null;

                statement = connection.createStatement();
                //Выполним запрос
                ResultSet result1 = statement.executeQuery(
                        "SELECT * FROM users where id > 0 and id < 10");
                //result это указатель на первую строку с выборки
                //чтобы вывести данные мы будем использовать
                //метод next() , с помощью которого переходим к следующему элементу
                System.out.println("Выводим statement");
                while (result1.next()) {
                    System.out.println("Номер в выборке #" + result1.getRow()
                            + "\t Номер в базе #" + result1.getInt("id")
                            + "\t" + result1.getString("name"));
                }
                // Вставить запись
                statement.executeUpdate(
                        "INSERT INTO users(name) values('Стас')");
                //Обновить запись
                statement.executeUpdate(
                        "UPDATE users SET name = 'admin' where id = 4");


                //2.PreparedStatement: предварительно компилирует запросы,
                //которые могут содержать входные параметры
                PreparedStatement preparedStatement = null;
                // ? - место вставки нашего значеня
                preparedStatement = connection.prepareStatement(
                        "SELECT * FROM users where id > ? and id < ?");
                //Устанавливаем в нужную позицию значения определённого типа
                preparedStatement.setInt(1, 2);
                preparedStatement.setInt(2, 10);
                //выполняем запрос
                ResultSet result2 = preparedStatement.executeQuery();

                System.out.println("Выводим PreparedStatement");
                while (result2.next()) {
                    System.out.println("Номер в выборке #" + result2.getRow()
                            + "\t Номер в базе #" + result2.getInt("id")
                            + "\t" + result2.getString("name"));
                }

                preparedStatement = connection.prepareStatement(
                        "INSERT INTO users(name) values(?)");
                preparedStatement.setString(1, "name");
                //метод принимает значение без параметров
                //темже способом можно сделать и UPDATE
                preparedStatement.executeUpdate();


                //3.CallableStatement: используется для вызова хранимых функций,
                // которые могут содержать входные и выходные параметры
                //CallableStatement callableStatement = null;
                //Вызываем функцию myFunc (хранится в БД)
                //callableStatement = connection.prepareCall(
                       // " { call myfunc(?,?) } ");
                //Задаём входные параметры
                //callableStatement.setString(1, "Dima");
                //callableStatement.setString(2, "Alex");
                //Выполняем запрос
                //ResultSet result3 = callableStatement.executeQuery();
                //Если CallableStatement возвращает несколько объектов ResultSet,
                //то нужно выводить данные в цикле с помощью метода next
                //у меня функция возвращает один объект
                //result3.next();
                //System.out.println(result3.getString("MESSAGE"));
                //если функция вставляет или обновляет, то используется метод executeUpdate()

            } catch (Exception ex) {
                //выводим наиболее значимые сообщения
                Logger.getLogger(JDBCtest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(JDBCtest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
    }




