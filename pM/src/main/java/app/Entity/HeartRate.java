package app.Entity;

import java.text.SimpleDateFormat;

import app.utils.DBConstants;
import nl.qbusict.cupboard.annotation.Column;

/**
 * Created by I332329 on 8/8/2017.
 */

public class HeartRate {
//    db.execSQL("CREATE TABLE IF NOT EXISTS banddata(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
//            "username STRING," +
//            "heartrate INTEGER," +
//            "time LONG," +
//            "gps STRING," +
//            "isupload INTEGER," +
//            "isdelete INTEGER);");
    @Column(DBConstants.DB_MetaData.HEARTRATE_ID)
    private Long _id;
    @Column(DBConstants.DB_MetaData.HEARTRATE_USERID)
    private Integer userid;
    @Column(DBConstants.DB_MetaData.HEARTRATE_USERNAME)
    private String username;
    @Column(DBConstants.DB_MetaData.HEARTRATE_HEARTRATE)
    private Integer heartrate;
    @Column(DBConstants.DB_MetaData.HEARTRATE_TIME)
    private Long time;
    @Column(DBConstants.DB_MetaData.HEARTRATE_GPS)
    private String gps;
    @Column(DBConstants.DB_MetaData.HEARTRATE_ISUPLOAD)
    private Integer isupload;
    @Column(DBConstants.DB_MetaData.HEARTRATE_ISDELETE)
    private Integer isdelete;

    public HeartRate(){

    }


    public HeartRate(Long id, Integer user_id,String username, Integer heartrates, Long time, String gps, Integer isupload, Integer isdelete) {
        this._id = id;
        this.userid = user_id;
        this.username = username;
        this.heartrate = heartrates;
        this.time = time;
        this.gps = gps;
        this.isupload = isupload;
        this.isdelete = isdelete;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(Integer heartrate) {
        this.heartrate = heartrate;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public Integer getIsupload() {
        return isupload;
    }

    public void setIsupload(Integer isupload) {
        this.isupload = isupload;
    }

    public Integer getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }

    public String toJsonString(){

        return "{\"user_id\":" + "\"" + userid + "\"" + "," +
                "\"time_point\":" + "\"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)  + "\"" + "," +
                "\"heart_rate\":" + "\"" + heartrate + "\"" +"," +
                "\"user_name\":" + "\"" + username + "\"" + "}";
    }
}
