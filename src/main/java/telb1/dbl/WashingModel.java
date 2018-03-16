package telb1.dbl;

import org.joda.time.DateTime;

/**
 * Created by User on 13.03.2018.
 */
public class WashingModel {
    private Integer washingId;
    private DateTime dateTime;
    private String gosNomer;
    private String fz;
    private String point;
    private String washerName;

    public WashingModel(Integer washingId, DateTime dateTime, String gosNomer, String fz, String point, String washerName) {
        this.washingId = washingId;
        this.dateTime = dateTime;
        this.gosNomer = gosNomer;
        this.fz = fz;
        this.point = point;
        this.washerName = washerName;
    }

    @Override
    public String toString() {
        return
                "washingId=" + washingId +
                        ", dateTime=" + dateTime +
                        ", gosNomer='" + gosNomer + '\'' +
                        ", fz='" + fz + '\'' +
                        ", point='" + point + '\'' +
                        ", chatId=" + washerName;
    }

    public Integer getWashingId() {
        return washingId;
    }

    public void setWashingId(Integer washingId) {
        this.washingId = washingId;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getGosNomer() {
        return gosNomer;
    }

    public void setGosNomer(String gosNomer) {
        this.gosNomer = gosNomer;
    }

    public String getFz() {
        return fz;
    }

    public void setFz(String fz) {
        this.fz = fz;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getWasherName() {
        return washerName;
    }

    public void setWasherName(String washerName) {
        this.washerName = washerName;
    }
}



