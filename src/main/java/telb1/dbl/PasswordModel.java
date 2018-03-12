package telb1.dbl;

import java.sql.Date;

/**
 * Created by User on 12.03.2018.
 */
public class PasswordModel {

   private Integer car_id;
   private String password;
   private java.sql.Date datetime;

    public PasswordModel(Integer car_id, String password, Date datetime) {
        this.car_id = car_id;
        this.password = password;
        this.datetime = datetime;
    }

    public Integer getCar_id() {
        return car_id;
    }

    public void setCar_id(Integer car_id) {
        this.car_id = car_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
}
