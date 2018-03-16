package telb1.dbl;

/**
 * Created by User on 16.03.2018.
 */
public class WasherModel {
    private Integer washerId;
    private String point;
    private String washerName;

    public WasherModel(Integer washerId, String point,String washerName) {
        this.washerId = washerId;
        this.point = point;
        this.washerName = washerName;
    }

    public String getWasherName() {
        return washerName;
    }

    public void setWasherName(String washerName) {
        this.washerName = washerName;
    }

    public Integer getWasherId() {
        return washerId;
    }

    public void setWasherId(Integer washerId) {
        this.washerId = washerId;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
