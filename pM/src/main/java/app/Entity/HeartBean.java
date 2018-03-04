package app.Entity;

import java.util.List;
import java.util.Objects;

/**
 * Created by dell on 2017/9/12.
 */
public class HeartBean {

    private int user_id;
    private String time_point;
    private int heart_rate;
    private String user_name;
    private float longitude;
    private float latitude;

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    private List<Object> data;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getTime_point() {
        return time_point;
    }

    public void setTime_point(String time_point) {
        this.time_point = time_point;
    }

    public int getHeart_rate() {
        return heart_rate;
    }

    public void setHeart_rate(int heart_rate) {
        this.heart_rate = heart_rate;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String toString(){

        return "{\"user_id\":" + user_id + "," +
                "\"time_point\":" + time_point + "," +
                "\"heart_rate\":" + heart_rate + "," +
                "\"user_name\":" + user_name + "}";
    }

}
